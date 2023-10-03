package com.amazon.inspector.teamcity;

import com.amazon.inspector.teamcity.bomerman.BomermanJarHandler;
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

import java.io.File;
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
        String jarPath = new File(ScanBuildProcessAdapter.class.getProtectionDomain().getCodeSource().getLocation()
        .toURI()).getPath();

        String tmpDirPath = build.getBuildTempDirectory().getAbsolutePath();
        String bomermanPath = new BomermanJarHandler(jarPath).copyBomermanToDir(tmpDirPath);

        progressLogger.message(build.getBuildTempDirectory().getAbsolutePath());

        String archivePath = runnerParameters.get(ScanConstants.ARCHIVE_PATH);
        String sbom = new BomermanRunner(bomermanPath, archivePath).run();

        String roleArn = runnerParameters.get(ScanConstants.ROLE_ARN);
        String region = runnerParameters.get(ScanConstants.REGION);
        String validatedSbom = new SdkRequests(region, roleArn).requestSbom(sbom).toString();

        SbomData sbomData = SbomData.builder().sbom(new Gson().fromJson(validatedSbom, Sbom.class)).build();
        SbomOutputParser parser = new SbomOutputParser(sbomData);
        Results results = parser.parseSbom();
        progressLogger.message(results.toString());

        if (results.hasVulnerabilites()) {
            progressLogger.message("Converting SBOM Results to CSV.");
            CsvConverter csvConverter = new CsvConverter(sbomData);

            String outPath = String.format("%s/results-%s-%s.csv", tmpDirPath, build.getProjectName(), 
                    build.getBuildNumber());
            csvConverter.convert(outPath);
        }

        boolean doesBuildPass = !doesBuildFail(results.getCounts());
        if (doesBuildPass) {
            scanRequestSuccessHandler();
        } else {
            scanRequestFailureHandler();
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
        progressLogger.message("Vulnerabilities found in SBOM exceeded config, failing build.");
        throw new RunBuildException("Failed to start the scan. Response status code: ");
    }
}
