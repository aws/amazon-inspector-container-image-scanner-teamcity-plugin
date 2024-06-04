package com.amazon.inspector.teamcity.utils;

import java.net.URI;
import java.net.URISyntaxException;

import static com.amazon.inspector.teamcity.ScanBuildProcessAdapter.publicProgressLogger;

public class Sanitizer {
    private Sanitizer() {}

    public static String sanitizeUrl(String rawUrl) throws URISyntaxException {
        URI uri = new URI(rawUrl);
        return uri.toASCIIString();
    }

    public static String sanitizeFilePath(String rawUrl) {
        try {
            String[] splitUrl = rawUrl.split(":");
            URI uri = new URI(splitUrl[0], splitUrl[1], null);
            return uri.toASCIIString();
        } catch(Exception e) {
            publicProgressLogger.message(String.format("%s in invalid format, using it as the path.", rawUrl));
            return rawUrl;
        }

    }

    public static String sanitizeText(String text) throws URISyntaxException {
        return sanitizeFilePath(text);
    }
}
