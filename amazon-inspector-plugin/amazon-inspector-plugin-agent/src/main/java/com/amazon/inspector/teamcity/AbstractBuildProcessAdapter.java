package com.amazon.inspector.teamcity;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcessAdapter;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBuildProcessAdapter extends BuildProcessAdapter{
	
	protected ArtifactsWatcher artifactsWatcher;
	protected @NotNull final AgentRunningBuild build;
	protected @NotNull final BuildRunnerContext context;
	
	//Logger to use for messages sent to the build log on server.
	protected BuildProgressLogger progressLogger;
	protected File workingRoot;
	protected File reportingRoot;
	protected Map<String, String> runnerParameters;
	
	private volatile boolean isFinished;
	private volatile boolean isFailed;
	private volatile boolean isInterrupted;
	
	protected AbstractBuildProcessAdapter(
			@NotNull final ArtifactsWatcher artifactsWatcher,
			@NotNull final AgentRunningBuild build,
			@NotNull final BuildRunnerContext context,
			@NotNull final BuildProgressLogger progresslogger
	) {
		this.artifactsWatcher=artifactsWatcher;
		this.build=build;
		this.context=context;
		
		this.runnerParameters = context.getRunnerParameters();
		this.progressLogger = progresslogger;
		this.workingRoot = build.getCheckoutDirectory();
		this.reportingRoot = build.getBuildTempDirectory();
		this.artifactsWatcher = artifactsWatcher;
		this.isFinished = false;
		this.isFailed = false;
		this.isInterrupted = false;
	}
	
	@Override
	public void interrupt() {
		isInterrupted = true;
	}
	
	@Override
	public boolean isInterrupted() {
		return isInterrupted;
	}
	
	@Override
	public boolean isFinished() {
		return isFinished;
	}
	
	@NotNull
	@Override
	public BuildFinishedStatus waitFor() throws RunBuildException {
		while (!isInterrupted && !isFinished) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RunBuildException(e);
			}
		}
		
		return isFinished
				? (isFailed ? BuildFinishedStatus.FINISHED_FAILED : BuildFinishedStatus.FINISHED_SUCCESS)
				: BuildFinishedStatus.INTERRUPTED;
	}
	
	@Override
	public void start() throws RunBuildException {
		try {
			runProcess();
		} catch (RunBuildException e) {
			progressLogger.buildFailureDescription(e.getMessage());
			Loggers.AGENT.error(e);
			isFailed = true;
		} finally {
			isFinished = true;
		}
	}
	
	protected abstract void runProcess() throws RunBuildException;
	
	
	protected Path CreateDirectoryForReporting(String folderName) throws IOException {
		// initialize reporting root path
		final String reportingRootCanonicalPath = reportingRoot.getCanonicalPath();
		progressLogger.message(String.format("The reporting root path is [%1$s].", reportingRootCanonicalPath));
		final Path reportingRootPath = Paths.get(reportingRootCanonicalPath, folderName);
		Path directory = Files.createDirectory(reportingRootPath);
		progressLogger.message(String.format("Create directory for reporting [%1$s].", reportingRootPath));
		// register artifacts
		artifactsWatcher.addNewArtifactsPath(reportingRootPath.toString());
		
		return directory;
	}
	
	protected Path GetWorkingDirectoryPath() throws IOException {
		// initialize working root path
		final String workingRootCanonicalPath = this.workingRoot.getCanonicalPath();
		progressLogger.message(String.format("The working root path is [%1$s].", workingRootCanonicalPath));
		final Path workingRootPath = Paths.get(workingRootCanonicalPath);
		
		return workingRootPath;
	}
	
	protected List<String> getValuesFor(@NotNull Map<String, String> parameters, String parameter) {
		String value = parameters.get(parameter);
		if (value == null) {
			return new ArrayList<String>();
		} else {
			return StringUtil.split(value, true, '\r', '\n');
		}
	}
	
	protected String getServerURL()  {
		return this.build.getAgentConfiguration().getServerUrl();
	}
}
