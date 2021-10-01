package software.amazon.ecr.replicationconfiguration;

import lombok.NonNull;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryResponse;
import software.amazon.awssdk.services.ecr.model.PutReplicationConfigurationResponse;
import software.amazon.awssdk.services.ecr.model.ReplicationConfiguration;
import software.amazon.awssdk.services.ecr.model.ReplicationDestination;
import software.amazon.awssdk.services.ecr.model.ReplicationRule;
import software.amazon.awssdk.services.ecr.model.RepositoryFilter;

import java.util.Collections;

import static software.amazon.ecr.replicationconfiguration.TestHelper.DEST_REGION_1;
import static software.amazon.ecr.replicationconfiguration.TestHelper.DEST_REGISTRY_1;
import static software.amazon.ecr.replicationconfiguration.TestHelper.TEST_REGISTRY;
import static software.amazon.ecr.replicationconfiguration.TestHelper.FILTER_1;
import static software.amazon.ecr.replicationconfiguration.TestHelper.FILTER_TYPE_1;

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

    public static DescribeRegistryResponse oneDestinationOneFilterDescribeRegistryResponse() {
        return oneDestinationOneFilterDescribeRegistryResponse(DEST_REGION_1, DEST_REGISTRY_1, FILTER_1, FILTER_TYPE_1);
    }

    private static DescribeRegistryResponse oneDestinationOneFilterDescribeRegistryResponse(String testRegion, String testRegistry, String filter, String filterType) {
        return DescribeRegistryResponse.builder()
                .registryId(TEST_REGISTRY)
                .replicationConfiguration(getReplicationConfigurationWithRepoFilter(testRegion, testRegistry, filter, filterType))
                .build();
    }

    public static PutReplicationConfigurationResponse oneDestinationOneFilterPutReplicationConfigurationResponse() {
        return oneDestinationOneFilterPutReplicationConfigurationResponse(DEST_REGION_1, DEST_REGISTRY_1, FILTER_1, FILTER_TYPE_1);
    }

    private static PutReplicationConfigurationResponse oneDestinationOneFilterPutReplicationConfigurationResponse(@NonNull String region,
                                                                                                                  @NonNull String registryId,
                                                                                                                  @NonNull String filter,
                                                                                                                  @NonNull String filterType) {
        ReplicationConfiguration configuration = getReplicationConfigurationWithRepoFilter(region, registryId, filter, filterType);

        return PutReplicationConfigurationResponse.builder()
                .replicationConfiguration(configuration)
                .build();

    }

    private static ReplicationConfiguration getReplicationConfigurationWithRepoFilter(String region, String registryId, String filters, String filterType) {

        RepositoryFilter filter = RepositoryFilter.builder()
                .filter(filters)
                .filterType(filterType)
                .build();

        ReplicationDestination destination = ReplicationDestination.builder()
                .region(region)
                .registryId(registryId)
                .build();

        ReplicationRule rule = ReplicationRule.builder()
                .repositoryFilters(Collections.singletonList(filter))
                .destinations(Collections.singletonList(destination))
                .build();

        return ReplicationConfiguration.builder()
                .rules(rule)
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
