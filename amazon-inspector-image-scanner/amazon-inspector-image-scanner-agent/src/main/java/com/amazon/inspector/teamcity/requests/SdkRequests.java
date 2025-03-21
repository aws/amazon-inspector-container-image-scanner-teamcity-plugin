package com.amazon.inspector.teamcity.requests;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.protocols.json.internal.unmarshall.document.DocumentUnmarshaller;
import software.amazon.awssdk.protocols.jsoncore.JsonNodeParser;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.inspectorscan.InspectorScanClient;
import software.amazon.awssdk.services.inspectorscan.model.OutputFormat;
import software.amazon.awssdk.services.inspectorscan.model.ScanSbomRequest;
import software.amazon.awssdk.services.inspectorscan.model.ScanSbomResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;


import static com.amazon.inspector.teamcity.ScanBuildProcessAdapter.publicProgressLogger;

public class SdkRequests {
    private final String region;
    private final AmazonWebServicesCredentials awsCredential;
    private final String roleArn;
    private final String awsProfileName;

    public SdkRequests(String region, AmazonWebServicesCredentials awsCredential, String awsProfileName, String roleArn) {
        this.region = region;
        this.awsCredential = awsCredential;
        this.awsProfileName = awsProfileName;
        this.roleArn = roleArn;
    }

    public String requestSbom(String sbom) {
        SdkHttpClient client = ApacheHttpClient.builder().build();
        String workingProfileName = awsProfileName;
        AmazonWebServicesCredentials workingCredential = awsCredential;
        boolean retry = true;

        while (true) {
            try {
                InspectorScanClient scanClient = InspectorScanClient.builder()
                        .region(Region.of(region))
                        .httpClient(client)
                        .credentialsProvider(getCredentialProvider(workingProfileName, workingCredential))
                        .overrideConfiguration(ClientOverrideConfiguration.builder()
                                .putHeader("Accept-Encoding", "gzip")
                                .build())
                        .build();

                JsonNodeParser jsonNodeParser = JsonNodeParser.create();
                DocumentUnmarshaller unmarshaller = new DocumentUnmarshaller();
                Document document = jsonNodeParser.parse(sbom).visit(unmarshaller);

                ScanSbomRequest request = ScanSbomRequest.builder()
                        .sbom(document)
                        .outputFormat(OutputFormat.CYCLONE_DX_1_5)
                        .build();
                ScanSbomResponse response = scanClient.scanSbom(request);
                return response.sbom().toString();
            } catch (Exception e) {
                if (!retry) {
                    throw e;
                }

                retry = false;
                publicProgressLogger.message("An issue occurred while authenticating, attempting to " +
                        "authenticate with default credential provider chain");
                workingProfileName = "default";
                workingCredential = null;
            }
        }
    }

    private AwsCredentialsProvider getCredentialProvider(String workingProfileName,
                                                         AmazonWebServicesCredentials workingCredential) {
        if (workingCredential != null) {
            publicProgressLogger.message("Using explicitly provided AWS credentials to authenticate.");
            return StaticCredentialsProvider.create(createRawCredentialProvider(workingCredential).resolveCredentials());
        } else if (roleArn != null && !roleArn.isEmpty()) {
            publicProgressLogger.message("Authenticating to STS via a role and default credential provider chain.");
            StsClient stsClient = StsClient.builder().region(Region.of(region)).build();
            return StsAssumeRoleCredentialsProvider.builder().stsClient(stsClient).refreshRequest(AssumeRoleRequest.builder()
                    .roleArn(roleArn).roleSessionName("inspectorscan").build()).build();
        } else if (workingProfileName != null && !workingProfileName.isEmpty()) {
            publicProgressLogger.message(
                    String.format("AWS Credential and role not provided, authenticating using \"%s\" as profile name.",
                            workingProfileName)
            );
            return ProfileCredentialsProvider.builder().profileName(workingProfileName).build();
        } else {
            publicProgressLogger.message("Using default credential provider chain to authenticate.");
            return DefaultCredentialsProvider.create();
        }
    }

    private AwsCredentialsProvider createRawCredentialProvider(AmazonWebServicesCredentials workingCredential) {
        return () -> new AwsCredentials() {
            @Override
            public String accessKeyId() {
                return workingCredential.getAWSAccessKeyId();
            }

            @Override
            public String secretAccessKey() {
                return workingCredential.getAWSSecretKey();
            }
        };
    }
}