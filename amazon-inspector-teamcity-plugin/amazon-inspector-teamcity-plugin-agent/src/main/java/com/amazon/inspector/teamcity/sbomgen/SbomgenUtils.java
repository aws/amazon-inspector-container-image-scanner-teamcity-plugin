package com.amazon.inspector.teamcity.sbomgen;

import com.amazon.inspector.teamcity.exception.MalformedScanOutputException;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SbomgenUtils {

    public static String processSbomgenOutput(String sbom) throws MalformedScanOutputException {
        sbom.replaceAll("time=.+file=.+\"", "");
        int startIndex = sbom.indexOf("{");
        int endIndex = sbom.lastIndexOf("}");

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            throw new MalformedScanOutputException("Sbom scanning output formatted incorrectly.");
        }

        return sbom.substring(startIndex, endIndex + 1);
    }

    @VisibleForTesting
    public static String stripProperties(String sbom) {
        JsonObject json = JsonParser.parseString(sbom).getAsJsonObject();
        JsonArray components = json.getAsJsonObject().get("components").getAsJsonArray();

        for (JsonElement component : components) {
            component.getAsJsonObject().remove("properties");
        }

        return json.toString();
    }
}
