package com.amazon.inspector.teamcity.models.html;

import com.amazon.inspector.teamcity.models.html.components.HtmlVulnerability;
import com.amazon.inspector.teamcity.models.html.components.ImageMetadata;
import com.amazon.inspector.teamcity.models.html.components.SeverityValues;
import lombok.Builder;

import java.util.List;

@Builder
public class HtmlData {
    public String jsonFilePath;
    public String csvFilePath;
    public String bomFormat;
    public String specVersion;
    public String version;
    public ImageMetadata imageMetadata;
    public SeverityValues severityValues;
    public List<HtmlVulnerability> vulnerabilities;
}

