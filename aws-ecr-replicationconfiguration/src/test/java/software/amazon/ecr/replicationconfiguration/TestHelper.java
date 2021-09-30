package software.amazon.ecr.replicationconfiguration;

import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestHelper {

    public static final String TEST_REGISTRY = "1234556778";

    // Cross Region Replication
    public static final String DEST_REGION_1 = "us-west-1";
    public static final String DEST_REGISTRY_1 = "1234556778";

    // Cross Account Replication
    public static final String DEST_REGION_2 = "eu-west-1";
    public static final String DEST_REGISTRY_2 = "5566334422";

    //Cross Region Repository Filtering
    public static final String FILTER_1 = "prod";
    public static final String FILTER_TYPE_1 = "PREFIX_MATCH";

    public static final String FILTER_2 = "dev-";
    public static final String FILTER_TYPE_2 = "PREFIX_MATCH";

    public static ResourceHandlerRequest<ResourceModel> generateRequest(List<ReplicationDestination> destinations) {
        final ResourceModel model = getResourceModel(destinations);
        return getHandlerRequest(model);
    }

    public static ResourceHandlerRequest<ResourceModel> generateRequestWithPrimaryId(List<ReplicationDestination> destinations) {
        final ResourceModel model = getResourceModel(destinations);
        model.setRegistryId(TEST_REGISTRY);
        return getHandlerRequest(model);
    }

    private static ResourceHandlerRequest<ResourceModel> getHandlerRequest(ResourceModel model) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId(TEST_REGISTRY)
                .desiredResourceState(model)
                .build();
    }

    public static ResourceHandlerRequest<ResourceModel> generateRequestWithPrimaryIdForRepoFilter(List<ReplicationDestination> destinations, List<RepositoryFilter> filters) {
        final ResourceModel model = getResourceModelWithFilter(destinations, filters);
        model.setRegistryId(TEST_REGISTRY);
        return getHandlerRequest(model);
    }

    public static ResourceHandlerRequest<ResourceModel> generateRequestWithFilter(List<ReplicationDestination> singleDestination, List<RepositoryFilter> singleFilter) {
        final ResourceModel model = getResourceModelWithFilter(singleDestination, singleFilter);
        return getHandlerRequest(model);
    }

    private static ResourceModel getResourceModelWithFilter(List<ReplicationDestination> destinations, List<RepositoryFilter> filters) {
        final ReplicationConfiguration config = ReplicationConfiguration.builder()
                .rules(Collections.singletonList(ReplicationRule.builder()
                        .repositoryFilters(filters)
                        .destinations(destinations)
                        .build()))
                .build();

        return ResourceModel.builder()
                .replicationConfiguration(config)
                .build();
    }

    private static ResourceModel getResourceModel(List<ReplicationDestination> destinations) {
        final ReplicationConfiguration config = ReplicationConfiguration.builder()
                .rules(Collections.singletonList(
                        ReplicationRule.builder()
                                .destinations(destinations)
                                .build()))
                .build();

        return ResourceModel.builder()
                .replicationConfiguration(config)
                .build();
    }

    public static List<RepositoryFilter> singleFilter() {
        return Collections.singletonList(RepositoryFilter.builder()
                .filter(FILTER_1)
                .filterType(FILTER_TYPE_1)
                .build()
        );
    }

    public static List<RepositoryFilter> multipleFilters() {
        return Arrays.asList(RepositoryFilter.builder()
                        .filter(FILTER_1)
                        .filterType(FILTER_TYPE_1)
                        .build(),
                RepositoryFilter.builder()
                        .filter(FILTER_2)
                        .filterType(FILTER_TYPE_2)
                        .build()
        );
    }

    public static List<ReplicationDestination> singleDestination() {
        return Collections.singletonList(ReplicationDestination.builder()
                .region(DEST_REGION_1)
                .registryId(DEST_REGISTRY_1)
                .build()
        );
    }

    public static List<ReplicationDestination> multipleDestinations() {
        return Arrays.asList(ReplicationDestination.builder()
                        .region(DEST_REGION_1)
                        .registryId(DEST_REGISTRY_1)
                        .build(),
                ReplicationDestination.builder()
                        .region(DEST_REGION_2)
                        .registryId(DEST_REGISTRY_2)
                        .build()
        );
    }
}

