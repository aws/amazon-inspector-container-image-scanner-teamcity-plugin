package com.amazon.inspector.teamcity;

import com.amazon.inspector.teamcity.requests.AmazonWebServicesCredentials;
import com.amazon.inspector.teamcity.sbomgen.SbomgenDownloader;
import com.amazon.inspector.teamcity.sbomgen.SbomgenRunner;
import com.amazon.inspector.teamcity.csvconversion.CsvConverter;
import com.amazon.inspector.teamcity.html.HtmlConversionUtils;
import com.amazon.inspector.teamcity.html.HtmlGenerator;
import com.amazon.inspector.teamcity.html.HtmlJarHandler;
import com.amazon.inspector.teamcity.models.html.HtmlData;
import com.amazon.inspector.teamcity.models.html.components.ImageMetadata;
import com.amazon.inspector.teamcity.models.html.components.SeverityValues;
import com.amazon.inspector.teamcity.models.sbom.Sbom;
import com.amazon.inspector.teamcity.models.sbom.SbomData;
import com.amazon.inspector.teamcity.requests.SdkRequests;
import com.amazon.inspector.teamcity.sbomparsing.SbomOutputParser;
import com.amazon.inspector.teamcity.sbomparsing.Severity;
import com.amazon.inspector.teamcity.sbomparsing.SeverityCounts;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

import static com.amazon.inspector.teamcity.ScanConstants.ARCHIVE_PATH;
import static com.amazon.inspector.teamcity.ScanConstants.AWS_ACCESS_KEY_ID;
import static com.amazon.inspector.teamcity.ScanConstants.AWS_PROFILE_NAME;
import static com.amazon.inspector.teamcity.ScanConstants.AWS_SECRET_KEY;
import static com.amazon.inspector.teamcity.ScanConstants.HTML_PATH;
import static com.amazon.inspector.teamcity.ScanConstants.IS_THRESHOLD_ENABLED;
import static com.amazon.inspector.teamcity.ScanConstants.SBOMGEN_PATH;
import static com.amazon.inspector.teamcity.ScanConstants.DOCKER_PASSWORD;
import static com.amazon.inspector.teamcity.ScanConstants.DOCKER_USERNAME;
import static com.amazon.inspector.teamcity.ScanConstants.REGION;
import static com.amazon.inspector.teamcity.ScanConstants.ROLE_ARN;
import static com.amazon.inspector.teamcity.ScanConstants.SBOMGEN_SELECTION;
import static com.amazon.inspector.teamcity.utils.Sanitizer.sanitizeFilePath;
import static com.amazon.inspector.teamcity.utils.Sanitizer.sanitizeText;
import static com.amazon.inspector.teamcity.utils.Sanitizer.sanitizeUrl;


public class ScanBuildProcessAdapter extends AbstractBuildProcessAdapter {
    public static BuildProgressLogger publicProgressLogger;
    
    public ScanBuildProcessAdapter(
            @NotNull final ArtifactsWatcher artifactsWatcher,
            @NotNull final AgentRunningBuild build,
            @NotNull final BuildRunnerContext context,
            @NotNull final BuildProgressLogger progressLogger) throws RunBuildException {
        super(artifactsWatcher, build, context, progressLogger);

        publicProgressLogger = progressLogger;
    }

    @Override
    protected void runProcess() throws RunBuildException {
        try {
            if (!isInterrupted()) {
                ScanRequestHandler(runnerParameters);
            } else {
                throw new RunBuildException("Scan request is interrupted.");
            }
        } catch (Exception e) {
            throw new RunBuildException(e);
        }
    }

    private String buildBaseUrl() {
        String buildName = build.getSharedBuildParameters().getAllParameters().get("system.teamcity.buildType.id");
        String buildId = runnerParameters.get("teamcity.build.id");
        String buildUrl = build.getSharedBuildParameters().getAllParameters().get("env.BUILD_URL");
        String serverUrl = buildUrl.split("/")[2];
        return String.format("http://%s/repository/download/%s/%s:id", serverUrl, buildName, buildId);
    }

    private void ScanRequestHandler(Map<String, String> runnerParameters) throws Exception {
        String teamcityDirPath = build.getCheckoutDirectory().getAbsolutePath();
        String sbomgenPath = runnerParameters.get(SBOMGEN_PATH);
        String sbomgenSource = runnerParameters.get(SBOMGEN_SELECTION);
        String archivePath = runnerParameters.get(ARCHIVE_PATH);
        String dockerUsername = runnerParameters.get(DOCKER_USERNAME);
        String dockerPassword = runnerParameters.get(DOCKER_PASSWORD);
        String roleArn = runnerParameters.get(ROLE_ARN);
        String region = runnerParameters.get(REGION);
        String awsAccessKeyId = runnerParameters.get(AWS_ACCESS_KEY_ID);
        String awsSecretKey = runnerParameters.get(AWS_SECRET_KEY);
        String awsProfileName = runnerParameters.get(AWS_PROFILE_NAME);

        boolean isThresholdEnabled = Boolean.parseBoolean(runnerParameters.get(IS_THRESHOLD_ENABLED));
        publicProgressLogger.message("Threshold: " + isThresholdEnabled);

        String activeSbomgenPath = sbomgenPath;
        if (!sbomgenSource.equals("manual")) {
            publicProgressLogger.message("Automatic SBOMGen Sourcing selected, downloading now...");
            activeSbomgenPath = SbomgenDownloader.getBinary(sbomgenSource);
        } else {
            publicProgressLogger.message("Using manually provided path to inspector-sbomgen.");
        }

        String sbom = new SbomgenRunner(activeSbomgenPath, archivePath, dockerUsername, dockerPassword).run();

        JsonObject component = JsonParser.parseString(sbom).getAsJsonObject().get("metadata").getAsJsonObject()
                .get("component").getAsJsonObject();

        String imageSha = "No Sha Found";
        for (JsonElement element : component.get("properties").getAsJsonArray()) {
            String elementName = element.getAsJsonObject().get("name").getAsString();
            if (elementName.equals("amazon:inspector:sbom_collector:image_id")) {
                imageSha = element.getAsJsonObject().get("value").getAsString();
            }
        }

        publicProgressLogger.message("Sending SBOM to Inspector for validation");

        AmazonWebServicesCredentials awsCredentials = null;

        if ((awsAccessKeyId != null && !awsAccessKeyId.isEmpty()) && (awsSecretKey != null && !awsSecretKey.isEmpty())) {
            awsCredentials = AmazonWebServicesCredentials.builder()
                    .AWSAccessKeyId(awsAccessKeyId)
                    .AWSSecretKey(awsSecretKey)
                    .build();
        }

        String responseData = new SdkRequests(region, awsCredentials, awsProfileName, roleArn).requestSbom(sbom);

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        SbomData sbomData = SbomData.builder().sbom(gson.fromJson(responseData, Sbom.class)).build();

        String sbomFileName = String.format("%s-%s-sbom.json", build.getProjectName(),
                build.getBuildNumber()).replaceAll("[ #]", "");
        String sbomPath = String.format("%s/%s", teamcityDirPath, sbomFileName);

        writeSbomDataToFile(gson.toJson(sbomData.getSbom()), sbomPath);

        CsvConverter converter = new CsvConverter(sbomData);
        String csvFileName = String.format("%s-%s-sbom.csv", build.getProjectName(),
                build.getBuildNumber()).replaceAll("[ #]", "");
        String csvPath = String.format("%s/%s", teamcityDirPath, csvFileName);

        progressLogger.message("Converting SBOM Results to CSV.");
        converter.convert(csvPath);

        SbomOutputParser parser = new SbomOutputParser(sbomData);
        SeverityCounts severityCounts = parser.parseSbom();

        String sanitizedImageId = null;
        String componentName = component.get("name").getAsString();

        if (componentName.endsWith(".tar")) {
            sanitizedImageId = sanitizeFilePath("file://" + componentName);
        } else {
            sanitizedImageId = sanitizeText(componentName);
        }

        String[] splitName = sanitizedImageId.split(":");
        String tag = null;
        if (splitName.length > 1) {
            tag = splitName[1];
        }

        String baseUrl = buildBaseUrl();
        String sbomUrl = sanitizeUrl(String.format("%s/%s", baseUrl, sbomFileName));
        String csvUrl = sanitizeUrl(String.format("%s/%s", baseUrl, csvFileName));

        HtmlData htmlData = HtmlData.builder()
                .jsonFilePath(sbomUrl)
                .csvFilePath(csvUrl)
                .imageMetadata(ImageMetadata.builder()
                        .id(splitName[0])
                        .tags(tag)
                        .sha(imageSha)
                        .build())
                .severityValues(SeverityValues.builder()
                        .critical(severityCounts.getCounts().get(Severity.CRITICAL))
                        .high(severityCounts.getCounts().get(Severity.HIGH))
                        .medium(severityCounts.getCounts().get(Severity.MEDIUM))
                        .low(severityCounts.getCounts().get(Severity.LOW))
                        .build())
                .vulnerabilities(HtmlConversionUtils.convertVulnerabilities(sbomData.getSbom().getVulnerabilities(),
                        sbomData.getSbom().getComponents()))
                .build();

        String htmlJarPath = new File(HtmlJarHandler.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getPath();
        HtmlJarHandler htmlJarHandler = new HtmlJarHandler(htmlJarPath);
        String htmlPath = htmlJarHandler.copyHtmlToDir(teamcityDirPath);
        String html = new Gson().toJson(htmlData);
        new HtmlGenerator(htmlPath).generateNewHtml(html);

        artifactsWatcher.addNewArtifactsPath(htmlPath);
        artifactsWatcher.addNewArtifactsPath(teamcityDirPath + "/inspector-classic.png");
        artifactsWatcher.addNewArtifactsPath(sbomPath);
        artifactsWatcher.addNewArtifactsPath(csvPath);

        String serverUrl = String.format("http://%s",
                build.getSharedBuildParameters().getAllParameters().get("env.BUILD_URL").split("/")[2]);

        progressLogger.message("Prefixing file paths with the Server URL from settings, currently: " + serverUrl);
        progressLogger.message("CSV Output File: " + csvUrl);
        progressLogger.message("SBOM Output File: " + sbomUrl);
        progressLogger.message(String.format("HTML Report File: %s/index.html", baseUrl));
        progressLogger.message("Files can also be downloaded from the artifacts tab.");

        progressLogger.message(severityCounts.toString());
        if (!isThresholdEnabled) {
            progressLogger.message("Ignoring results due to thresholds being disabled.");
        }
        boolean doesBuildPass = !doesBuildFail(severityCounts.getCounts());

        if (!isThresholdEnabled) {
            scanRequestSuccessHandler();
        } else if (isThresholdEnabled && doesBuildPass) {
            scanRequestSuccessHandler();
        } else {
            scanRequestFailureHandler();
        }
    }

    public static void writeSbomDataToFile(String sbomData, String outputFilePath) throws IOException {
        publicProgressLogger.message("Creating sbom report at " + outputFilePath);
        new File(outputFilePath).createNewFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {
            for (String line : sbomData.split("\n")) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean doesBuildFail(Map<Severity, Integer> counts) {
        String countCritical = runnerParameters.get(ScanConstants.COUNT_CRITICAL);
        String countHigh = runnerParameters.get(ScanConstants.COUNT_HIGH);
        String countMedium = runnerParameters.get(ScanConstants.COUNT_MEDIUM);
        String countLow = runnerParameters.get(ScanConstants.COUNT_LOW);

        boolean criticalExceedsLimit = counts.get(Severity.CRITICAL) > Integer.parseInt(countCritical);
        boolean highExceedsLimit = counts.get(Severity.HIGH) > Integer.parseInt(countHigh);
        boolean mediumExceedsLimit = counts.get(Severity.MEDIUM) > Integer.parseInt(countMedium);
        boolean lowExceedsLimit = counts.get(Severity.LOW) > Integer.parseInt(countLow);

        return criticalExceedsLimit || highExceedsLimit || mediumExceedsLimit || lowExceedsLimit;
    }

    private void scanRequestSuccessHandler() {
        progressLogger.message("Build finished successfully.");
    }

    private void scanRequestFailureHandler() throws RunBuildException {
        throw new RunBuildException("Vulnerabilities found in SBOM exceeded config, failing build.");
    }
}
