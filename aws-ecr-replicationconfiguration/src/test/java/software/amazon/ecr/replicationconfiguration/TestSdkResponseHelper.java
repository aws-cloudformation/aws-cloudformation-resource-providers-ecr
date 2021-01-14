package software.amazon.ecr.replicationconfiguration;

import lombok.NonNull;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryResponse;
import software.amazon.awssdk.services.ecr.model.PutReplicationConfigurationResponse;
import software.amazon.awssdk.services.ecr.model.ReplicationConfiguration;
import software.amazon.awssdk.services.ecr.model.ReplicationDestination;
import software.amazon.awssdk.services.ecr.model.ReplicationRule;

import java.util.Collections;

import static software.amazon.ecr.replicationconfiguration.TestHelper.DEST_REGION_1;
import static software.amazon.ecr.replicationconfiguration.TestHelper.DEST_REGISTRY_1;
import static software.amazon.ecr.replicationconfiguration.TestHelper.TEST_REGISTRY;

public class TestSdkResponseHelper {

    public static PutReplicationConfigurationResponse oneDestinationPutReplicationConfigurationResponse() {
        return oneDestinationPutReplicationConfigurationResponse(DEST_REGION_1, DEST_REGISTRY_1);
    }

    public static PutReplicationConfigurationResponse oneDestinationPutReplicationConfigurationResponse(@NonNull String region, @NonNull String registryId) {
        ReplicationConfiguration configuration = getReplicationConfiguration(region, registryId);

        return PutReplicationConfigurationResponse.builder()
                .replicationConfiguration(configuration)
                .build();
    }


    public static DescribeRegistryResponse emptyDescribeRegistryResponse() {
        return emptyDescribeRegistryResponse(TEST_REGISTRY);
    }

    public static DescribeRegistryResponse emptyDescribeRegistryResponse(String registryId) {
        return DescribeRegistryResponse.builder()
                .registryId(registryId)
                .build();
    }


    public static DescribeRegistryResponse oneDestinationDescribeRegistryResponse() {
        return oneDestinationDescribeRegistryResponse(DEST_REGION_1, DEST_REGISTRY_1);
    }

    public static DescribeRegistryResponse oneDestinationDescribeRegistryResponse(String testRegion, String testRegistry) {
        return DescribeRegistryResponse.builder()
                .registryId(TEST_REGISTRY)
                .replicationConfiguration(getReplicationConfiguration(testRegion, testRegistry))
                .build();
    }


    private static ReplicationConfiguration getReplicationConfiguration(String region, String registryId) {
        ReplicationDestination destination = ReplicationDestination.builder()
                .region(region)
                .registryId(registryId)
                .build();

        ReplicationRule rule = ReplicationRule.builder()
                .destinations(Collections.singletonList(destination))
                .build();

        return ReplicationConfiguration.builder()
                .rules(rule)
                .build();
    }
}
