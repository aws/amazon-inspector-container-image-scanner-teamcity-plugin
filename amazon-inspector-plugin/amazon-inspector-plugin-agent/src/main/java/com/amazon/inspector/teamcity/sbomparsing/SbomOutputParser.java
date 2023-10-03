package com.amazon.inspector.teamcity.sbomparsing;

import com.amazon.inspector.teamcity.models.sbom.Components.Rating;
import com.amazon.inspector.teamcity.models.sbom.Components.Vulnerability;
import com.amazon.inspector.teamcity.models.sbom.SbomData;
import lombok.Getter;

import java.util.List;

public class SbomOutputParser {
    @Getter
    private SbomData sbom;

    public SbomOutputParser(SbomData sbomData) {
        this.sbom = sbomData;
    }

    public Results parseSbom() {
        Results results = new Results();
        List<Vulnerability> vulnerabilities = sbom.getSbom().getVulnerabilities();

        if (vulnerabilities == null) {
            return results;
        }

        for (Vulnerability vulnerability : vulnerabilities) {
            List<Rating> ratings = vulnerability.getRatings();

            Severity severity = getHighestRatingFromList(ratings);
            results.increment(severity);
        }

        return results;
    }

    protected Severity getHighestRatingFromList(List<Rating> ratings) {
        Severity highestSeverity = null;

        for (Rating rating : ratings) {
            Severity severity = Severity.getSeverityFromString(rating.getSeverity());

            if (highestSeverity == null) {
                highestSeverity = severity;
            }

            highestSeverity = Severity.getHigherSeverity(highestSeverity, severity);
        }

        return highestSeverity;
    }
}
