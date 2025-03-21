package com.amazon.inspector.teamcity.sbomgen;

import com.amazon.inspector.teamcity.exception.SbomgenNotFoundException;
import com.google.common.annotations.VisibleForTesting;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

import static com.amazon.inspector.teamcity.ScanBuildProcessAdapter.publicProgressLogger;
import static com.amazon.inspector.teamcity.sbomgen.SbomgenUtils.processSbomgenOutput;

public class SbomgenRunner {
    public String sbomgenPath;
    public String archiveType;
    public String archivePath;
    @Setter
    public String dockerUsername;
    @Setter
    public String dockerPassword;

    public SbomgenRunner(String sbomgenPath, String archivePath, String dockerUsername) {
        this.sbomgenPath = sbomgenPath;
        this.archivePath = archivePath;
        this.dockerUsername = dockerUsername;
    }

    public SbomgenRunner(String sbomgenPath, String archivePath, String dockerUsername, String dockerPassword) {
        this.sbomgenPath = sbomgenPath;
        this.archivePath = archivePath;
        this.dockerUsername = dockerUsername;
        this.dockerPassword = dockerPassword;
    }

    public SbomgenRunner(String sbomgenPath, String activeArchiveType, String archivePath, String dockerUsername, String dockerPassword) {
        this.sbomgenPath = sbomgenPath;
        this.archivePath = archivePath;
        this.archiveType = activeArchiveType;
        this.dockerUsername = dockerUsername;
        this.dockerPassword = dockerPassword;
    }

    public String run() throws Exception {
        return runSbomgen(sbomgenPath, archivePath);
    }

    private String runSbomgen(String sbomgenPath, String archivePath) throws Exception {
        if (!isValidPath(sbomgenPath)) {
            throw new IllegalArgumentException("Invalid sbomgen path: " + sbomgenPath);
        }

        publicProgressLogger.message("Making downloaded SBOMGen executable...");
        new ProcessBuilder(new String[]{"chmod", "+x", sbomgenPath}).start();

        publicProgressLogger.message("Running command...");

        String[] command = new String[] {
                sbomgenPath, "container", "--image", archivePath
        };
        publicProgressLogger.message(Arrays.toString(command));
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> environment = builder.environment();

        if (dockerPassword != null && !dockerPassword.isEmpty()) {
            environment.put("INSPECTOR_SBOMGEN_USERNAME", dockerUsername);
            environment.put("INSPECTOR_SBOMGEN_PASSWORD", dockerPassword);
        }

        builder.redirectErrorStream(true);
        Process p = null;

        try {
            p = builder.start();
        } catch (IOException e) {
            throw new SbomgenNotFoundException(String.format("There was an issue running inspector-sbomgen, " +
                    "is %s the correct path?", sbomgenPath));
        }

        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while (true) {
            line = r.readLine();
            sb.append(line + "\n");
            if (line == null) { break; }
        }

        return processSbomgenOutput(sb.toString());
    }

    @VisibleForTesting
    protected boolean isValidPath(String path) {
        String regex = "^[a-zA-Z0-9/._\\-:]+$";
        return path.matches(regex);
    }
}