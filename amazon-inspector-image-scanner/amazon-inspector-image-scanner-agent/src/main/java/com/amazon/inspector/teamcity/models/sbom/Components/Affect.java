package com.amazon.inspector.teamcity.models.sbom.Components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Affect {
    private String ref;
}