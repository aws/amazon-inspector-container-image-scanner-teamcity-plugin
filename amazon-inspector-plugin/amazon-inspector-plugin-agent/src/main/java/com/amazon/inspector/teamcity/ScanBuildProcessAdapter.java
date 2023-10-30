package com.amazon.inspector.teamcity;

import com.amazon.inspector.teamcity.bomerman.BomermanRunner;
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
import com.amazon.inspector.teamcity.sbomparsing.Results;
import com.amazon.inspector.teamcity.sbomparsing.SbomOutputParser;
import com.amazon.inspector.teamcity.sbomparsing.Severity;
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

import static com.amazon.inspector.teamcity.utils.Sanitizer.sanitizeNonUrl;
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

    private void ScanRequestHandler(Map<String, String> runnerParameters) throws Exception {
        String jarPath = new File(ScanBuildProcessAdapter.class.getProtectionDomain().getCodeSource().getLocation()
        .toURI()).getPath();

        String teamcityDirPath = build.getCheckoutDirectory().getAbsolutePath();
        String bomermanPath = runnerParameters.get(ScanConstants.BOMERMAN_PATH);

        String archivePath = runnerParameters.get(ScanConstants.ARCHIVE_PATH);
        String dockerUsername = runnerParameters.get(ScanConstants.DOCKER_USERNAME);
        String dockerPassword = runnerParameters.get(ScanConstants.DOCKER_PASSWORD);
        String sbom = new BomermanRunner(bomermanPath, archivePath, dockerUsername, dockerPassword).run();

        String roleArn = runnerParameters.get(ScanConstants.ROLE_ARN);
        String region = runnerParameters.get(ScanConstants.REGION);
        String validatedSbom = new SdkRequests(region, roleArn).requestSbom(sbom).toString();

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        SbomData sbomData = SbomData.builder().sbom(gson.fromJson(validatedSbom, Sbom.class)).build();
        String sbomPath = String.format("%s/results-%s-%s.json", teamcityDirPath, build.getProjectName(),
                build.getBuildNumber());

        writeSbomDataToFile(gson.toJson(sbomData.getSbom()), sbomPath);

        SbomOutputParser parser = new SbomOutputParser(sbomData);
        Results results = parser.parseSbom();

        progressLogger.message("Converting SBOM Results to CSV.");
        CsvConverter csvConverter = new CsvConverter(sbomData);

        String csvPath = String.format("%s/results-%s-%s.csv", teamcityDirPath, build.getProjectName(),
                build.getBuildNumber());
        csvConverter.convert(csvPath);

        JsonObject component = JsonParser.parseString(sbom).getAsJsonObject().get("metadata").getAsJsonObject()
                .get("component").getAsJsonObject();

        String imageSha = "No Sha Found";
        for (JsonElement element : component.get("properties").getAsJsonArray()) {
            String elementName = element.getAsJsonObject().get("name").getAsString();
            if (elementName.equals("amazon:inspector:sbom_collector:image_id")) {
                imageSha = element.getAsJsonObject().get("value").getAsString();
            }
        }

        String sanitizedSbomPath = sanitizeUrl("file://" + sbomPath);
        String sanitizedCsvPath = sanitizeUrl("file://" + csvPath);
        String sanitizedImageId = sanitizeNonUrl(component.get("name").getAsString());

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
                        .critical(results.getCounts().get(Severity.CRITICAL))
                        .high(results.getCounts().get(Severity.HIGH))
                        .medium(results.getCounts().get(Severity.MEDIUM))
                        .low(results.getCounts().get(Severity.LOW))
                        .build())
                .vulnerabilities(HtmlConversionUtils.convertVulnerabilities(sbomData.getSbom().getVulnerabilities(),
                        sbomData.getSbom().getComponents()))
                .build();

        publicProgressLogger.message(htmlData.toString());
        HtmlJarHandler htmlJarHandler = new HtmlJarHandler(jarPath);
        String htmlPath = htmlJarHandler.copyHtmlToDir(teamcityDirPath);

        String html = new Gson().toJson(htmlData);
        new HtmlGenerator(htmlPath).generateNewHtml(html);

        progressLogger.message("CSV Output File: " + sanitizedCsvPath);
        progressLogger.message("SBOM Output File: " + sanitizedSbomPath);
        progressLogger.message("HTML Report File:" + sanitizeUrl("file://" + htmlPath));
        progressLogger.message("\n");
        progressLogger.message(results.toString());
        boolean doesBuildPass = !doesBuildFail(results.getCounts());
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
