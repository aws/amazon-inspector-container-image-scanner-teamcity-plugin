package com.amazon.inspector.teamcity;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBuildRunner implements AgentBuildRunner{
	
	protected final ArtifactsWatcher artifactsWatcher;
	
	public AbstractBuildRunner(@NotNull final ArtifactsWatcher artifactsWatcher) {
		this.artifactsWatcher = artifactsWatcher;
	}
	
	protected List<String> getValuesFor(@NotNull Map<String, String> parameters, String parameter) {
		String value = parameters.get(parameter);
		if (value == null) {
			return new ArrayList<String>();
		} else {
			return StringUtil.split(value, true, '\r', '\n');
		}
	}
	
	
	@NotNull
	public abstract BuildProcess createBuildProcess(@NotNull final AgentRunningBuild build,
	                                                @NotNull final BuildRunnerContext context) throws RunBuildException;
}