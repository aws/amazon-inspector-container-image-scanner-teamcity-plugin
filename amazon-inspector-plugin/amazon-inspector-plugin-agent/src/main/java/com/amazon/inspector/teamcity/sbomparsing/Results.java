package com.amazon.inspector.teamcity.sbomparsing;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amazon.inspector.teamcity.sbomparsing.Severity.CRITICAL;
import static com.amazon.inspector.teamcity.sbomparsing.Severity.HIGH;
import static com.amazon.inspector.teamcity.sbomparsing.Severity.LOW;
import static com.amazon.inspector.teamcity.sbomparsing.Severity.MEDIUM;

public class Results {
    @Getter
    private Map<Severity, Integer> counts = new HashMap<>();

    public Results() {
        counts.put(CRITICAL, 0);
        counts.put(HIGH, 0);
        counts.put(MEDIUM, 0);
        counts.put(LOW, 0);
    }

    public void increment(Severity severityToIncrement) {
        counts.put(severityToIncrement, counts.get(severityToIncrement) + 1);
    }

    public boolean hasVulnerabilites() {
        List<Integer> countValues = new ArrayList<>(counts.values());

        if (countValues.stream().anyMatch(x -> x > 0)) {
            return true;
        }

        return false;
    }

    public String toString() {
        return String.format("Critical: %s, High: %s, Medium: %s, Low: %s",
                counts.get(CRITICAL), counts.get(HIGH), counts.get(MEDIUM), counts.get(LOW));
    }
}