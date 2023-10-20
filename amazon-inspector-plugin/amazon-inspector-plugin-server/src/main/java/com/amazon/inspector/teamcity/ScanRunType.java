package com.amazon.inspector.teamcity;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ScanRunType extends RunType {

	private final PluginDescriptor pluginDescriptor;

	public ScanRunType(@NotNull final RunTypeRegistry registry, @NotNull final PluginDescriptor descriptor) {
		this.pluginDescriptor = descriptor;
		registry.registerRunType(this);
	}

	@NotNull
	@Override
	public String getType() {
		return ScanConstants.SCAN_RUN_TYPE;
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return "Amazon Inspector Scan";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Build runner that starts Inspector Scan for a container image.";
	}

	@Nullable
	@Override
	public PropertiesProcessor getRunnerPropertiesProcessor() {
		return new ScanPropertiesProcessor();
	}

	@Nullable
	@Override
	public String getEditRunnerParamsJspFilePath() {
		return pluginDescriptor.getPluginResourcesPath() + "editScanParams.jsp";
	}

	@Nullable
	@Override
	public String getViewRunnerParamsJspFilePath() {
		return pluginDescriptor.getPluginResourcesPath() + "viewScanParams.jsp";
	}

	@Nullable
	@Override
	public Map<String, String> getDefaultRunnerProperties() {
		return new HashMap<>();
	}
}