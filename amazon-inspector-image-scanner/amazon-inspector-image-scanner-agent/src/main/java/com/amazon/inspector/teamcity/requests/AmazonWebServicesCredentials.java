package com.amazon.inspector.teamcity.requests;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AmazonWebServicesCredentials {
    private String AWSAccessKeyId;
    private String AWSSecretKey;
}
