package com.amazon.inspector.teamcity.models.sbom.Components;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Metadata {
    private List<Property> properties;
}
