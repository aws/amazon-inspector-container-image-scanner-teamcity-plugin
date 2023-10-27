package com.amazon.inspector.teamcity.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Sanitizer {
    private Sanitizer() {}

    public static String sanitizeUrl(String rawUrl) {
        return URLEncoder.encode(rawUrl, StandardCharsets.UTF_8).replace("%2F", "/");
    }

    public static String sanitizeNonUrl(String text) {
        return sanitizeUrl(text).replace("%3A", ":");
    }
}
