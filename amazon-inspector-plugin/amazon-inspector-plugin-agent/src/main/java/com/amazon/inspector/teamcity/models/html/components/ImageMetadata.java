package com.amazon.inspector.teamcity.models.html.components;

import lombok.Builder;

@Builder
public class ImageMetadata {
    public String id;
    public String tags;
    public String sha;
}
