package com.amazon.inspector.teamcity.html;

import com.amazon.inspector.teamcity.models.html.components.DockerVulnerability;
import com.amazon.inspector.teamcity.models.html.components.HtmlVulnerability;
import com.amazon.inspector.teamcity.models.sbom.Components.Affect;
import com.amazon.inspector.teamcity.models.sbom.Components.Component;
import com.amazon.inspector.teamcity.models.sbom.Components.Metadata;
import com.amazon.inspector.teamcity.models.sbom.Components.Property;
import com.amazon.inspector.teamcity.models.sbom.Components.Rating;
import com.amazon.inspector.teamcity.models.sbom.Components.Vulnerability;
import com.amazon.inspector.teamcity.sbomparsing.Severity;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.amazon.inspector.teamcity.utils.ConversionUtils.getSeverity;

public class HtmlConversionUtils {

    public static List<HtmlVulnerability> convertVulnerabilities(List<Vulnerability> vulnerabilities,
                                                                 List<Component> components) {
        List<HtmlVulnerability> htmlVulnerabilities = new ArrayList<>();
        if (vulnerabilities == null) {
            return htmlVulnerabilities;
        }

        for (Vulnerability vulnerability : vulnerabilities) {
            if (vulnerability.getId().contains("IN-DOCKER")) {
                continue;
            }

            String severity = getSeverity(vulnerability).toString().toUpperCase(Locale.ROOT);
            if (severity == null) {
                severity = "Untriaged";
            }

            for (Affect affect : vulnerability.getAffects()) {
                String component = getComponent(components, affect.getRef());
                if (component == null) {
                    continue;
                }

                HtmlVulnerability htmlVulnerability = HtmlVulnerability.builder()
                        .title(vulnerability.getId())
                        .severity(StringUtils.capitalize(severity))
                        .component(StringEscapeUtils.unescapeJava(component))
                        .build();
                htmlVulnerabilities.add(htmlVulnerability);
            }
        }

        Collections.sort(htmlVulnerabilities, (v1, v2) -> sortVulnerabilitiesBySeverity(v1.severity, v2.severity));
        return htmlVulnerabilities;
    }

    public static String getLines(String id, List<Property> properties) {
        if (properties == null) {
            return "N/A";
        }

        for (Property property : properties) {
            if (property.getName().contains(id)) {
                String lines = property.getValue().split(":")[1];
                String[] splitLines = lines.split("-");
                if (splitLines[0].equals(splitLines[1])) {
                    return splitLines[0];
                } else {
                    return lines;
                }
            }
        }

        return "N/A";
    }

    public static List<Component> getLineComponents(List<Component> components) {
        List<Component> lineComponents = new ArrayList<>();
        if (components == null) {
            return lineComponents;
        }
        for (Component component : components) {
            if (component.getName().contains("dockerfile")) {
                lineComponents.add(component);
            }
        }

        return lineComponents;
    }

    public static List<DockerVulnerability> convertDocker(List<Vulnerability> vulnerabilities,
                                                          List<Component> components) {
        List<DockerVulnerability> dockerVulnerabilities = new ArrayList<>();
        List<Component> lineComponents = getLineComponents(components);

        if (vulnerabilities == null) {
            return dockerVulnerabilities;
        }

        for (Vulnerability vulnerability : vulnerabilities) {
            if (!vulnerability.getId().contains("IN-DOCKER")) {
                continue;
            }

            String severity = getSeverity(vulnerability).toString().toUpperCase(Locale.ROOT);

            String description = vulnerability.getDescription();

            String filename = "N/A";
            String lines = "N/A";

            for (Component lineComponent : lineComponents) {
                if (lineComponent != null)  {
                    lines = getLines(vulnerability.getId(), lineComponent.getProperties());
                    filename = lineComponent.getName();
                }

                if (lineComponent.getName().equals("dockerfile:comp-1.Dockerfile")) {
                    lines += " - Derived";
                }
            }

            DockerVulnerability dockerVulnerability = DockerVulnerability.builder()
                    .id(vulnerability.getId())
                    .severity(severity)
                    .description(description)
                    .file(filename)
                    .lines(lines)
                    .build();
            dockerVulnerabilities.add(dockerVulnerability);
        }

        Collections.sort(dockerVulnerabilities, (v1, v2) -> sortVulnerabilitiesBySeverity(v1.severity, v2.severity));
        return dockerVulnerabilities;
    }


    private static int sortVulnerabilitiesBySeverity(String s1, String s2) {
        Severity sev1 = Severity.getSeverityFromString(s1);
        Severity sev2 = Severity.getSeverityFromString(s2);

        return sev1.compareTo(sev2);
    }

    private static String getComponent(List<Component> components, String componentId) {
        for (Component component : components) {
            if (component.getBomRef().equals(componentId)) {
                return component.getPurl();
            }
        }

        return "None Found";
    }
}