package software.amazon.ecr.replicationconfiguration;

import software.amazon.awssdk.services.ecr.model.DescribeRegistryResponse;
import software.amazon.awssdk.services.ecr.model.ReplicationDestination;
import software.amazon.awssdk.services.ecr.model.ReplicationRule;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Response {

    public static ResourceModel generateResourceModel(String registryId, DescribeRegistryResponse response) {
        ReplicationConfiguration replicationConfiguration = getReplicationConfiguration(response);

        return ResourceModel.builder()
                .registryId(registryId)
                .replicationConfiguration(replicationConfiguration)
                .build();
    }

    private static ReplicationConfiguration getReplicationConfiguration(DescribeRegistryResponse response) {
        List<ReplicationRule> rules = response.replicationConfiguration().rules();
        List<software.amazon.ecr.replicationconfiguration.ReplicationDestination> destinations = rules.stream().findFirst().orElseThrow(IllegalStateException::new)
                .destinations().stream()
                .map(Response::toDestinations)
                .collect(Collectors.toList());
        software.amazon.ecr.replicationconfiguration.ReplicationRule rule = software.amazon.ecr.replicationconfiguration.ReplicationRule.builder()
                .destinations(destinations)
                .build();
        return ReplicationConfiguration.builder()
                .rules(Collections.singletonList(rule))
                .build();
    }

    private static software.amazon.ecr.replicationconfiguration.ReplicationDestination toDestinations(ReplicationDestination replicationDestination) {
        return software.amazon.ecr.replicationconfiguration.ReplicationDestination.builder()
                .region(replicationDestination.region())
                .registryId(replicationDestination.registryId())
                .build();
    }
}
