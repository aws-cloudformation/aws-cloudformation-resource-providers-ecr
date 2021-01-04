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
