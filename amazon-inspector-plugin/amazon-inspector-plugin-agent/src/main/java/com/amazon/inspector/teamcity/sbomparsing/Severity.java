package com.amazon.inspector.teamcity.sbomparsing;

import lombok.AllArgsConstructor;

import java.util.Locale;

@AllArgsConstructor
public enum Severity {
    CRITICAL("critical", 4),
    HIGH("high", 3),
    MEDIUM("medium", 2),
    LOW("low", 1),
    INFO("informational", 0),
    NONE("none", 0);

    private String severityName;
    private int rating;

    public static Severity getHigherSeverity(Severity sevLeft, Severity sevRight) {
        if (sevLeft.rating > sevRight.rating) {
            return sevLeft;
        }
        return sevRight;
    }

    public static Severity getSeverityFromString(String severityName) {
        switch (severityName.toLowerCase(Locale.ROOT)) {
            case "critical":
                return CRITICAL;
            case "high":
                return HIGH;
            case "medium":
                return MEDIUM;
            case "low":
                return LOW;
            case "informational":
                return INFO;
            case "none":
                return NONE;
            default:
                throw new RuntimeException(String.format("Severity value doesn't exist: %s", severityName));
        }
    }
}
