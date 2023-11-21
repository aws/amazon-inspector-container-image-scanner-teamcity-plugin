package com.amazon.inspector.teamcity;

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
import java.util.Map;

import static com.amazon.inspector.teamcity.ScanConstants.ARCHIVE_PATH;
import static com.amazon.inspector.teamcity.ScanConstants.HTML_PATH;
import static com.amazon.inspector.teamcity.ScanConstants.SBOMGEN_PATH;
import static com.amazon.inspector.teamcity.ScanConstants.DOCKER_PASSWORD;
import static com.amazon.inspector.teamcity.ScanConstants.DOCKER_USERNAME;
import static com.amazon.inspector.teamcity.ScanConstants.REGION;
import static com.amazon.inspector.teamcity.ScanConstants.ROLE_ARN;
import static com.amazon.inspector.teamcity.utils.Sanitizer.sanitizeFilePath;
import static com.amazon.inspector.teamcity.utils.Sanitizer.sanitizeText;


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

    private void ScanRequestHandler(Map<String, String> runnerParameters) throws Exception {
        String teamcityDirPath = build.getCheckoutDirectory().getAbsolutePath();
        String sbomgenPath = runnerParameters.get(SBOMGEN_PATH);
        String archivePath = runnerParameters.get(ARCHIVE_PATH);
        String dockerUsername = runnerParameters.get(DOCKER_USERNAME);
        String dockerPassword = runnerParameters.get(DOCKER_PASSWORD);
        String roleArn = runnerParameters.get(ROLE_ARN);
        String region = runnerParameters.get(REGION);

        String sbom = new SbomgenRunner(sbomgenPath, archivePath, dockerUsername, dockerPassword).run();

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
        String responseData = new SdkRequests(region, roleArn).requestSbom(sbom);

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

        String sanitizedSbomPath = sanitizeFilePath("file://" + sbomPath);
        String sanitizedCsvPath = sanitizeFilePath("file://" + csvPath);
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

        HtmlData htmlData = HtmlData.builder()
                .jsonFilePath(sanitizedSbomPath)
                .csvFilePath(sanitizedCsvPath)
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
        String sanitizedHtmlPath = sanitizeFilePath("file://" + htmlPath);
        String html = new Gson().toJson(htmlData);
        new HtmlGenerator(htmlPath).generateNewHtml(html);

        artifactsWatcher.addNewArtifactsPath(htmlPath);
        artifactsWatcher.addNewArtifactsPath(teamcityDirPath + "/inspector-classic.png");
        artifactsWatcher.addNewArtifactsPath(sbomPath);
        artifactsWatcher.addNewArtifactsPath(csvPath);

        progressLogger.message("CSV Output File: " + sanitizedCsvPath);
        progressLogger.message("SBOM Output File: " + sanitizedSbomPath);
        progressLogger.message("HTML Report File: " + sanitizedHtmlPath);
        progressLogger.message("Files can be downloaded from the artifacts tab.");

        progressLogger.message(severityCounts.toString());
        boolean doesBuildPass = !doesBuildFail(severityCounts.getCounts());
        if (doesBuildPass) {
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
