package com.amazonaws.ecr.repository;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.LifecyclePolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final EcrClient client = ClientBuilder.getClient();

        try {
            if (model.getRepositoryPolicyText() != null) {
                proxy.injectCredentialsAndInvokeV2(Helpers.setRepositoryPolicyRequest(model), client::setRepositoryPolicy);
            } else {
                try {
                    proxy.injectCredentialsAndInvokeV2(Helpers.deleteRepositoryPolicyRequest(model), client::deleteRepositoryPolicy);
                } catch (RepositoryPolicyNotFoundException e) {
                    // there's no policy to delete
                }
            }

            if (model.getLifecyclePolicy() != null) {
                proxy.injectCredentialsAndInvokeV2(Helpers.putLifecyclePolicyRequest(model), client::putLifecyclePolicy);
            } else {
                try {
                    proxy.injectCredentialsAndInvokeV2(Helpers.deleteLifecyclePolicyRequest(model), client::deleteLifecyclePolicy);
                } catch (LifecyclePolicyNotFoundException e) {
                    // there's no policy to delete
                }
            }

            final DescribeRepositoriesResponse describeResponse = proxy.injectCredentialsAndInvokeV2(Helpers.describeRepositoriesRequest(model), client::describeRepositories);
            final String arn = describeResponse.repositories().get(0).repositoryArn();
            final ListTagsForResourceResponse listTagsResponse = proxy.injectCredentialsAndInvokeV2(Helpers.listTagsForResourceRequest(arn), client::listTagsForResource);
            final List<String> tagsToRemove = listTagsResponse.tags().stream().map(tag -> tag.key()).collect(Collectors.toList());
            final List<Tag> tagsToAdd = Helpers.translateTagsToSdk(model.getTags());

            if (!CollectionUtils.isNullOrEmpty(tagsToRemove)) proxy.injectCredentialsAndInvokeV2(Helpers.untagResourceRequest(tagsToRemove, arn), client::untagResource);
            if (!CollectionUtils.isNullOrEmpty(tagsToAdd)) proxy.injectCredentialsAndInvokeV2(Helpers.tagResourceRequest(tagsToAdd, arn), client::tagResource);
        } catch (RepositoryNotFoundException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, model.getRepositoryName());
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
