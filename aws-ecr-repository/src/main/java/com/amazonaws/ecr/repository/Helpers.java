package com.amazonaws.ecr.repository;

import com.amazonaws.cloudformation.exceptions.CfnInternalFailureException;
import com.amazonaws.cloudformation.exceptions.TerminalException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.CreateRepositoryRequest;
import software.amazon.awssdk.services.ecr.model.DeleteLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.DeleteRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.DeleteRepositoryRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesRequest;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.LifecyclePolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ecr.model.PutLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.RepositoryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.SetRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.Tag;
import software.amazon.awssdk.services.ecr.model.TagResourceRequest;
import software.amazon.awssdk.services.ecr.model.UntagResourceRequest;

public class Helpers {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static CreateRepositoryRequest createRepositoryRequest(final ResourceModel model) {
        return CreateRepositoryRequest.builder()
                .repositoryName(model.getRepositoryName())
                .tags(translateTagsToSdk(model.getTags()))
                .build();
    }

    static PutLifecyclePolicyRequest putLifecyclePolicyRequest(final ResourceModel model) {
        return PutLifecyclePolicyRequest.builder()
                .repositoryName(model.getRepositoryName())
                .lifecyclePolicyText(model.getLifecyclePolicy().getLifecyclePolicyText())
                .registryId(model.getLifecyclePolicy().getRegistryId())
                .build();
    }

    static DeleteLifecyclePolicyRequest deleteLifecyclePolicyRequest(final ResourceModel model) {
        return DeleteLifecyclePolicyRequest.builder()
                .repositoryName(model.getRepositoryName())
                .build();
    }

    static SetRepositoryPolicyRequest setRepositoryPolicyRequest(final ResourceModel model) {
        try {
            return SetRepositoryPolicyRequest.builder()
                    .repositoryName(model.getRepositoryName())
                    .policyText(MAPPER.writeValueAsString(model.getRepositoryPolicyText()))
                    .build();
        } catch (final JsonProcessingException e) {
            throw new TerminalException(e);
        }
    }

    static DeleteRepositoryPolicyRequest deleteRepositoryPolicyRequest(final ResourceModel model) {
        return DeleteRepositoryPolicyRequest.builder()
                .repositoryName(model.getRepositoryName())
                .build();
    }

    static DeleteRepositoryRequest deleteRepositoryRequest(final ResourceModel model) {
        return DeleteRepositoryRequest.builder()
                .repositoryName(model.getRepositoryName())
                .build();
    }

    static TagResourceRequest tagResourceRequest(final List<Tag> tags, final String arn) {
        return TagResourceRequest.builder().tags(tags).resourceArn(arn).build();
    }

    static UntagResourceRequest untagResourceRequest(final Collection<String> tagKeys, final String arn) {
        return UntagResourceRequest.builder().tagKeys(tagKeys).resourceArn(arn).build();
    }

    static ListTagsForResourceRequest listTagsForResourceRequest(final String arn) {
        return ListTagsForResourceRequest.builder().resourceArn(arn).build();
    }

    static DescribeRepositoriesRequest describeRepositoriesRequest(final ResourceModel model) {
        return DescribeRepositoriesRequest.builder()
                .repositoryNames(Arrays.asList(model.getRepositoryName()))
                .build();
    }

    static DescribeRepositoriesRequest describeRepositoriesRequest(final String nextToken) {
        return DescribeRepositoriesRequest.builder()
                .maxResults(50)
                .nextToken(nextToken)
                .build();
    }

    static List<Tag> translateTagsToSdk(final List<Tags> tags) {
        if (tags == null) return null;
        return tags.stream().map(tag -> Tag.builder()
                .key(tag.getKey())
                .value(tag.getValue())
                .build()
        ).collect(Collectors.toList());
    }

    static List<Tags> translateTagsFromSdk(final List<Tag> tags) {
        if (tags == null) return null;
        return tags.stream().map(tag -> Tags.builder()
                .key(tag.key())
                .value(tag.value())
                .build()
        ).collect(Collectors.toList());
    }
    static GetRepositoryPolicyRequest getRepositoryPolicyRequest(final String repositoryName, final String registryId) {
        return GetRepositoryPolicyRequest.builder()
                .repositoryName(repositoryName)
                .registryId(registryId)
                .build();
    }

    static GetLifecyclePolicyRequest getLifecyclePolicyRequest(final String repositoryName, final String registryId) {
        return GetLifecyclePolicyRequest.builder()
                .repositoryName(repositoryName)
                .registryId(registryId)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deserializePolicyText(final String policyText) {
        final Map<String, Object> policyTextJson;
        try {
            policyTextJson = policyText == null ? null : MAPPER.readValue(policyText, Map.class);
        } catch (final IOException e) {
            throw new CfnInternalFailureException(e);
        }
        return policyTextJson;
    }

    static ResourceModel buildModel(final AmazonWebServicesClientProxy proxy, final Repository repo) {
        final String arn = repo.repositoryArn();
        final String repositoryName = repo.repositoryName();
        final String registryId = repo.registryId();
        final EcrClient client = ClientBuilder.getClient();

        Map<String, Object> repositoryPolicyText = null;
        LifecyclePolicy lifecyclePolicy = null;

        try {
            final GetRepositoryPolicyResponse getRepositoryPolicyResponse = proxy.injectCredentialsAndInvokeV2(getRepositoryPolicyRequest(repositoryName, registryId), client::getRepositoryPolicy);
            repositoryPolicyText = deserializePolicyText(getRepositoryPolicyResponse.policyText());
        } catch (RepositoryPolicyNotFoundException e) {
            // RepositoryPolicyText is not required so it might not exist
        }

        try {
            final GetLifecyclePolicyResponse getLifecyclePolicyResponse = proxy.injectCredentialsAndInvokeV2(getLifecyclePolicyRequest(repositoryName, registryId), client::getLifecyclePolicy);
            lifecyclePolicy = LifecyclePolicy.builder()
                    .registryId(getLifecyclePolicyResponse.registryId())
                    .lifecyclePolicyText(getLifecyclePolicyResponse.lifecyclePolicyText())
                    .build();
        } catch (LifecyclePolicyNotFoundException e) {
            // LifecyclePolicy is not required so it might not exist
        }

        final ListTagsForResourceResponse listTagsResponse = proxy.injectCredentialsAndInvokeV2(listTagsForResourceRequest(arn), client::listTagsForResource);
        final List<Tags> tags = translateTagsFromSdk(listTagsResponse.tags());

        return ResourceModel.builder()
            .repositoryName(repositoryName)
            .lifecyclePolicy(lifecyclePolicy)
            .repositoryPolicyText(repositoryPolicyText)
            .tags(tags)
            .build();
    }
}
