package com.amazon.inspector.teamcity.models.sbom;

import com.amazon.inspector.teamcity.models.sbom.Components.Component;
import com.amazon.inspector.teamcity.models.sbom.Components.Metadata;
import com.amazon.inspector.teamcity.models.sbom.Components.Vulnerability;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Sbom {
    private String bomFormat;
    private String specVersion;
    private int version;
    private String serialNumber;
    private Metadata metadata;
    private List<Component> components;
    private List<Vulnerability> vulnerabilities;
}