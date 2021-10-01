package software.amazon.ecr.replicationconfiguration;

import software.amazon.awssdk.services.ecr.model.DescribeRegistryResponse;
import software.amazon.awssdk.services.ecr.model.ReplicationDestination;
import software.amazon.awssdk.services.ecr.model.ReplicationRule;
import software.amazon.awssdk.services.ecr.model.RepositoryFilter;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
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

        return ReplicationConfiguration.builder()
                .rules(getReplicationRulesList(rules))
                .build();
    }

    private static List<software.amazon.ecr.replicationconfiguration.ReplicationRule> getReplicationRulesList(List<ReplicationRule> rules) {
        List<software.amazon.ecr.replicationconfiguration.ReplicationRule> replicationRulesList = new ArrayList<>();
        //add supoort for multiple rules
        for (ReplicationRule replicationRule : rules) {
            List<software.amazon.ecr.replicationconfiguration.RepositoryFilter> repoFilters = replicationRule
                    .repositoryFilters().stream()
                    .map(Response::toFilter)
                    .collect(Collectors.toList());
            List<software.amazon.ecr.replicationconfiguration.ReplicationDestination> destinations = replicationRule
                    .destinations().stream()
                    .map(Response::toDestination)
                    .collect(Collectors.toList());

            software.amazon.ecr.replicationconfiguration.ReplicationRule.ReplicationRuleBuilder ruleBuilder =
                    software.amazon.ecr.replicationconfiguration.ReplicationRule.builder()
                            .destinations(destinations);

            if (!repoFilters.isEmpty()) {
                ruleBuilder.repositoryFilters(repoFilters);
            }
            replicationRulesList.add(ruleBuilder.build());
        }
        return replicationRulesList;
    }

    private static software.amazon.ecr.replicationconfiguration.ReplicationDestination toDestination(ReplicationDestination replicationDestination) {
        return software.amazon.ecr.replicationconfiguration.ReplicationDestination.builder()
                .region(replicationDestination.region())
                .registryId(replicationDestination.registryId())
                .build();
    }

    private static software.amazon.ecr.replicationconfiguration.RepositoryFilter toFilter(RepositoryFilter repoFilter) {
        return software.amazon.ecr.replicationconfiguration.RepositoryFilter.builder()
                .filter(repoFilter.filter())
                .filterType(repoFilter.filterType().toString())
                .build();
    }
}
