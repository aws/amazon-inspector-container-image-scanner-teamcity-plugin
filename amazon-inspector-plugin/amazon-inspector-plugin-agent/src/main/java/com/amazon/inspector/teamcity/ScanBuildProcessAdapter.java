package com.amazon.inspector.teamcity;

import com.amazon.inspector.teamcity.bomerman.BomermanRunner;
import com.amazon.inspector.teamcity.csvconversion.CsvConverter;
import com.amazon.inspector.teamcity.models.sbom.Sbom;
import com.amazon.inspector.teamcity.models.sbom.SbomData;
import com.amazon.inspector.teamcity.requests.SdkRequests;
import com.amazon.inspector.teamcity.sbomparsing.Results;
import com.amazon.inspector.teamcity.sbomparsing.SbomOutputParser;
import com.amazon.inspector.teamcity.sbomparsing.Severity;
import com.google.gson.Gson;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;


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
        String bomermanPath = "/Users/waltwilo/workplace/EeveeCICDPlugin/src/EeveeCICDTeamcityPlugin/" +
                "amazon-inspector-plugin/amazon-inspector-plugin-agent/src/main/resources/" +
                "bomerman_macos_amd64";
        String archivePath = build.getRunnerParameters().get(ScanConstants.ARCHIVE_PATH);
        String sbom = new BomermanRunner(bomermanPath, archivePath).run();

        String roleArn = build.getRunnerParameters().get(ScanConstants.ROLE_ARN);
        String region = build.getRunnerParameters().get(ScanConstants.REGION);
        String validatedSbom = new SdkRequests(region, roleArn).requestSbom(sbom).toString();

        SbomData sbomData = SbomData.builder().sbom(new Gson().fromJson(validatedSbom, Sbom.class)).build();
        SbomOutputParser parser = new SbomOutputParser(sbomData);
        Results results = parser.parseSbom();
        progressLogger.message(results.toString());

        if (results.hasVulnerabilites()) {
            progressLogger.message("Converting SBOM Results to CSV.");
            CsvConverter csvConverter = new CsvConverter(sbomData);
            csvConverter.convert("/Users/waltwilo/Downloads/results.csv");
        }

        boolean doesBuildPass = !doesBuildFail(results.getCounts());
        if (doesBuildPass) {
            scanRequestSuccessHandler();
        } else {
            scanRequestFailureHandler();
        }
    }

    public boolean doesBuildFail(Map<Severity, Integer> counts) {
        String countCritical = build.getRunnerParameters().get(ScanConstants.COUNT_CRITICAL);
        String countHigh = build.getRunnerParameters().get(ScanConstants.COUNT_HIGH);
        String countMedium = build.getRunnerParameters().get(ScanConstants.COUNT_MEDIUM);
        String countLow = build.getRunnerParameters().get(ScanConstants.COUNT_LOW);

        boolean criticalExceedsLimit = counts.get(Severity.CRITICAL) > Integer.parseInt(countCritical);
        boolean highExceedsLimit = counts.get(Severity.HIGH) > Integer.parseInt(countHigh);
        boolean mediumExceedsLimit = counts.get(Severity.MEDIUM) > Integer.parseInt(countMedium);
        boolean lowExceedsLimit = counts.get(Severity.LOW) > Integer.parseInt(countLow);

        return criticalExceedsLimit || highExceedsLimit || mediumExceedsLimit || lowExceedsLimit;
    }

    private void scanRequestSuccessHandler() throws IOException {
        progressLogger.message("Build finished successfully.");
    }

    private void scanRequestFailureHandler() throws RunBuildException {
        progressLogger.message("Vulnerabilities found in SBOM exceeded config, failing build.");
        throw new RunBuildException("Failed to start the scan. Response status code: ");
    }
}
