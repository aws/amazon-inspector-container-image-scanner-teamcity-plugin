package com.amazon.inspector.teamcity.sbomparsing;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.amazon.inspector.teamcity.sbomparsing.Severity.CRITICAL;
import static com.amazon.inspector.teamcity.sbomparsing.Severity.HIGH;
import static com.amazon.inspector.teamcity.sbomparsing.Severity.LOW;
import static com.amazon.inspector.teamcity.sbomparsing.Severity.MEDIUM;
import static com.amazon.inspector.teamcity.sbomparsing.Severity.OTHER;


public class SeverityCounts {

    @Getter
    private Map<Severity, Integer> counts = new HashMap<>();

    public SeverityCounts() {
        counts.put(CRITICAL, 0);
        counts.put(HIGH, 0);
        counts.put(MEDIUM, 0);
        counts.put(LOW, 0);
        counts.put(OTHER, 0);
    }

    public SeverityCounts(int critical, int high, int medium, int low, int other) {
        counts.put(CRITICAL, critical);
        counts.put(HIGH, high);
        counts.put(MEDIUM, medium);
        counts.put(LOW, low);
        counts.put(OTHER, other);
    }

    public void increment(Severity severityToIncrement) {
        if (counts.containsKey(severityToIncrement)) {
            counts.put(severityToIncrement, counts.get(severityToIncrement) + 1);
        }
    }

    public String toString() {
        return String.format("Critical: %s, High: %s, Medium: %s, Low: %s, Other: %s",
                counts.get(CRITICAL), counts.get(HIGH), counts.get(MEDIUM), counts.get(LOW), counts.get(OTHER));
    }
}
