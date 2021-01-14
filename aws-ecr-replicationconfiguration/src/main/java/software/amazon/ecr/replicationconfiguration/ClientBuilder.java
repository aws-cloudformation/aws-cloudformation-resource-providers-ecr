package software.amazon.ecr.replicationconfiguration;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
    public static EcrClient getClient() {
        return EcrClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
