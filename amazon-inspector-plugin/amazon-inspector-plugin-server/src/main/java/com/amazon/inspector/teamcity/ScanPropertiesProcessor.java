package com.amazon.inspector.teamcity;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ScanPropertiesProcessor implements PropertiesProcessor {

	@Override
	public Collection<InvalidProperty> process(Map<String, String> properties) {
		Collection<InvalidProperty> result = new HashSet<>();
		return result;
	}
}