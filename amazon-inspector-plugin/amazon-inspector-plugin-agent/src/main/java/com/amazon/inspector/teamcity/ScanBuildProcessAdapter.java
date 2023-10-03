package com.amazon.inspector.teamcity;

import com.amazon.inspector.teamcity.bomerman.BomermanRunner;
import com.amazon.inspector.teamcity.requests.SdkRequests;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.jetbrains.annotations.NotNull;

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

        progressLogger.message(validatedSbom);
    }
}
