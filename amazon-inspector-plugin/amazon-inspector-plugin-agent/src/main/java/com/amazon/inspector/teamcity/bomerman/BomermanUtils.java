package com.amazon.inspector.teamcity.bomerman;

import com.amazon.inspector.teamcity.ScanBuildProcessAdapter;

public class BomermanUtils {

    public static String processBomermanOutput(String sbom) {
        ScanBuildProcessAdapter.publicProgressLogger.message("Processing bomerman file");
        sbom.replaceAll("time=.+file=.+\"", "");
        return sbom.substring(sbom.indexOf("{"), sbom.lastIndexOf("}") + 1);
    }
}
