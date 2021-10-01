package software.amazon.ecr.replicationconfiguration;

import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.ecr.model.PutReplicationConfigurationRequest;
import software.amazon.awssdk.services.ecr.model.ReplicationConfiguration;
import software.amazon.awssdk.services.ecr.model.ReplicationDestination;
import software.amazon.awssdk.services.ecr.model.ReplicationRule;
import software.amazon.awssdk.services.ecr.model.RepositoryFilter;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Translator {

    public static PutReplicationConfigurationRequest putReplicationConfiguration(ResourceModel model) {
        Preconditions.checkNotNull(model.getReplicationConfiguration());
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(model.getReplicationConfiguration().getRules()));

        return PutReplicationConfigurationRequest.builder()
                .replicationConfiguration(getConfiguration(model))
                .build();
    }

    private static ReplicationConfiguration getConfiguration(ResourceModel model) {

        List<ReplicationRule> replicationRulesList = new ArrayList<>();
        //add support for multiple rules
        for (software.amazon.ecr.replicationconfiguration.ReplicationRule rules : model.getReplicationConfiguration().getRules()) {
            List<ReplicationDestination> replicationDestinations = rules
                    .getDestinations().stream()
                    .map(Translator::toReplicationDestination)
                    .collect(Collectors.toList());

            ReplicationRule.Builder rule = ReplicationRule.builder().destinations(replicationDestinations);
            if (rules.getRepositoryFilters() != null) {
                List<RepositoryFilter> repoFilter = rules
                        .getRepositoryFilters().stream()
                        .map(Translator::toRepositoryFilter)
                        .collect(Collectors.toList());
                rule.repositoryFilters(repoFilter);
            }
            replicationRulesList.add(rule.build());
        }
        ReplicationConfiguration configuration = ReplicationConfiguration.builder()
                .rules(replicationRulesList)
                .build();
        return configuration;
    }

    private static ReplicationDestination toReplicationDestination(software.amazon.ecr.replicationconfiguration.ReplicationDestination destination) {
        return ReplicationDestination.builder()
                .registryId(destination.getRegistryId())
                .region(destination.getRegion())
                .build();
    }

    private static RepositoryFilter toRepositoryFilter(software.amazon.ecr.replicationconfiguration.RepositoryFilter repoFilter) {
        return RepositoryFilter.builder()
                .filter(repoFilter.getFilter())
                .filterType(repoFilter.getFilterType()).build();
    }
}
