package com.amazonaws.ecr.repository;

import software.amazon.awssdk.services.ecr.EcrClient;

public class ClientBuilder {
    public static EcrClient getClient() {
        return EcrClient.builder().build();
    }
}
