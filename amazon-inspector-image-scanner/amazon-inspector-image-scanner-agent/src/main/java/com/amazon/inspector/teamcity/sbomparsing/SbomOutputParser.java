package com.amazon.inspector.teamcity.sbomparsing;

import com.google.common.annotations.VisibleForTesting;
import com.amazon.inspector.teamcity.models.sbom.Components.Rating;
import com.amazon.inspector.teamcity.models.sbom.Components.Vulnerability;
import com.amazon.inspector.teamcity.models.sbom.SbomData;
import lombok.Getter;

import java.util.List;

import static com.amazon.inspector.teamcity.utils.ConversionUtils.getSeverity;

public class SbomOutputParser {
    @Getter
    private SbomData sbom;
    public static SeverityCounts vulnCounts;
    public static SeverityCounts dockerCounts;
    public static SeverityCounts aggregateCounts;

    public SbomOutputParser(SbomData sbomData) {
        this.sbom = sbomData;
        vulnCounts = new SeverityCounts();
        dockerCounts = new SeverityCounts();
        aggregateCounts = new SeverityCounts();
    }

    public void parseVulnCounts() {
        List<Vulnerability> vulnerabilities = sbom.getSbom().getVulnerabilities();

        if (vulnerabilities == null) {
            return;
        }

        for (Vulnerability vulnerability : vulnerabilities) {
            Severity severity = getSeverity(vulnerability);

            if (vulnerability.getId().contains("IN-DOCKER")) {
                dockerCounts.increment(severity);
            } else {
                vulnCounts.increment(severity);
            }
            aggregateCounts.increment(severity);
        }
    }
}
