package com.amazonaws.ecr.repository;

import com.amazonaws.cloudformation.exceptions.CfnInternalFailureException;
import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.LifecyclePolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryPolicyNotFoundException;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final DescribeRepositoriesResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(Translator.describeRepositoriesRequest(model), ClientBuilder.getClient()::describeRepositories);
            logger.log("READ success");
        } catch (RepositoryNotFoundException e) {
            logger.log("READ RepositoryNotFoundException");
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, model.getRepositoryName());
        }

        ResourceModel modelToReturn = buildModel(proxy, response.repositories().get(0));
        logger.log(String.format("READ %s", modelToReturn.getArn()));
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(modelToReturn)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private static Map<String, Object> deserializePolicyText(final String policyText) {
        if (policyText == null) return null;
        try {
            return Translator.MAPPER.readValue(policyText, new TypeReference<HashMap<String,Object>>() {});
        } catch (final IOException e) {
            throw new CfnInternalFailureException(e);
        }
    }

    public static ResourceModel buildModel(final AmazonWebServicesClientProxy proxy, final Repository repo) {
        final String arn = repo.repositoryArn();
        final String repositoryName = repo.repositoryName();
        final String registryId = repo.registryId();
        final EcrClient client = ClientBuilder.getClient();

        Map<String, Object> repositoryPolicyText = null;
        LifecyclePolicy lifecyclePolicy = null;

        try {
            final GetRepositoryPolicyResponse getRepositoryPolicyResponse = proxy.injectCredentialsAndInvokeV2(Translator.getRepositoryPolicyRequest(repositoryName, registryId), client::getRepositoryPolicy);
            repositoryPolicyText = deserializePolicyText(getRepositoryPolicyResponse.policyText());
        } catch (RepositoryPolicyNotFoundException e) {
            // RepositoryPolicyText is not required so it might not exist
        }

        try {
            final GetLifecyclePolicyResponse getLifecyclePolicyResponse = proxy.injectCredentialsAndInvokeV2(Translator.getLifecyclePolicyRequest(repositoryName, registryId), client::getLifecyclePolicy);
            lifecyclePolicy = LifecyclePolicy.builder()
                    .registryId(getLifecyclePolicyResponse.registryId())
                    .lifecyclePolicyText(getLifecyclePolicyResponse.lifecyclePolicyText())
                    .build();
        } catch (LifecyclePolicyNotFoundException e) {
            // LifecyclePolicy is not required so it might not exist
        }

        final ListTagsForResourceResponse listTagsResponse = proxy.injectCredentialsAndInvokeV2(Translator.listTagsForResourceRequest(arn), client::listTagsForResource);
        final Set<Tag> tags = Translator.translateTagsFromSdk(listTagsResponse.tags());

        return ResourceModel.builder()
                .repositoryName(repositoryName)
                .lifecyclePolicy(lifecyclePolicy)
                .repositoryPolicyText(repositoryPolicyText)
                .tags(tags)
                .arn(arn)
                .build();
    }
}
