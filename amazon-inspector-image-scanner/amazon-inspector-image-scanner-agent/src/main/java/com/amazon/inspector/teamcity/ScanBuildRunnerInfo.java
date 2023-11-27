package com.amazon.inspector.teamcity;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import org.jetbrains.annotations.NotNull;

public class ScanBuildRunnerInfo implements AgentBuildRunnerInfo{

	@Override
	@NotNull
	public String getType() {
		return ScanConstants.SCAN_RUN_TYPE;
	}
	
	public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
		return true;
	}
}