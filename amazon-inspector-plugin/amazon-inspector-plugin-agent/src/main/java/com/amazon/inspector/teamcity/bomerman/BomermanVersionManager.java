package com.amazon.inspector.teamcity.bomerman;

import com.amazon.inspector.teamcity.exception.BomermanNotFoundException;

import java.util.List;
import java.util.Locale;

import static com.amazon.inspector.teamcity.ScanBuildProcessAdapter.publicProgressLogger;

public class BomermanVersionManager {
    private static final String LINUX_NAME = "linux";
    private static final String MACOS_NAME = "mac";
    private static final String AMD64_ARCH_NAME = "x86_64";
    private static final List<String> ARM64_ARCH_NAMES = List.of("aarch64", "amd64");
    private static final String MACOS_ARM64_NAME = "inspector-sbomgen.macos.arm64";
    private static final String MACOS_AMD64_NAME = "inspector-sbomgen.macos.amd64";
    private static final String LINUX_ARM64_NAME = "inspector-sbomgen.linux.arm64";
    private static final String LINUX_AMD64_NAME = "inspector-sbomgen.linux.amd64";

    public static String getBomermanName(String osName, String archName) throws BomermanNotFoundException {
        osName = osName.toLowerCase(Locale.ROOT);
        archName = archName.toLowerCase(Locale.ROOT);
        publicProgressLogger.message(String.format("Detecting Sbom Util Version using info:\nOS: %s\nArch: %s", osName, archName));

        if (osName.contains(LINUX_NAME)) {
            if (archName.contains(AMD64_ARCH_NAME)) {
                publicProgressLogger.message("Using Linux AMD64");
                return LINUX_AMD64_NAME;
            } else if (ARM64_ARCH_NAMES.contains(archName)) {
                publicProgressLogger.message("Using Linux ARM64");
                return LINUX_ARM64_NAME;
            }
        } else if (osName.contains(MACOS_NAME)) {
            if (archName.contains(AMD64_ARCH_NAME)) {
                publicProgressLogger.message("Using Macos AMD64");
                return MACOS_AMD64_NAME;
            } else if (ARM64_ARCH_NAMES.contains(archName)) {
                publicProgressLogger.message("Using Macos ARM64");
                return MACOS_ARM64_NAME;
            }
        }

        throw new BomermanNotFoundException("Compatible version of bomerman not found.");
    }
}
