package com.amazon.inspector.teamcity.models.html;

import com.amazon.inspector.teamcity.models.html.components.DockerVulnerability;
import com.amazon.inspector.teamcity.models.html.components.HtmlVulnerability;
import com.amazon.inspector.teamcity.models.html.components.ImageMetadata;
import lombok.Builder;

import java.util.List;

@Builder
public class HtmlData {
    public String artifactsPath;
    public String bomFormat;
    public String specVersion;
    public String version;
    public String updatedAt;
    public ImageMetadata imageMetadata;
    public List<HtmlVulnerability> vulnerabilities;
    public List<DockerVulnerability> docker;
}
