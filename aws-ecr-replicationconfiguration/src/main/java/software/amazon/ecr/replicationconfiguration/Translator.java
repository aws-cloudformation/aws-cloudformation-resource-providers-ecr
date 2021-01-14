package software.amazon.ecr.replicationconfiguration;

import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.ecr.model.PutReplicationConfigurationRequest;
import software.amazon.awssdk.services.ecr.model.ReplicationConfiguration;
import software.amazon.awssdk.services.ecr.model.ReplicationDestination;
import software.amazon.awssdk.services.ecr.model.ReplicationRule;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Translator {

    public static PutReplicationConfigurationRequest putReplicationConfiguration(ResourceModel model) {
        Preconditions.checkNotNull(model.getReplicationConfiguration());
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(model.getReplicationConfiguration().getRules()));

        List<ReplicationDestination> replicationDestinations = model.getReplicationConfiguration()
                .getRules().stream().findFirst().orElseThrow(IllegalStateException::new)
                .getDestinations().stream()
                .map(Translator::toReplicationDestinations)
                .collect(Collectors.toList());

        ReplicationRule rule = ReplicationRule.builder()
                .destinations(replicationDestinations)
                .build();

        ReplicationConfiguration configuration = ReplicationConfiguration.builder()
                .rules(Collections.singletonList(rule))
                .build();

        return PutReplicationConfigurationRequest.builder()
                .replicationConfiguration(configuration)
                .build();
    }

    private static ReplicationDestination toReplicationDestinations
            (software.amazon.ecr.replicationconfiguration.ReplicationDestination destination) {
        return ReplicationDestination.builder()
                .registryId(destination.getRegistryId())
                .region(destination.getRegion())
                .build();
    }
}
