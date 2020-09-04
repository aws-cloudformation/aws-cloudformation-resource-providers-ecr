package software.amazon.ecr.repository;

import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.LifecyclePolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;

public class UpdateHandler extends BaseHandlerStd {

    private AmazonWebServicesClientProxy proxy;
    private EcrClient client;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String accountId = request.getAwsAccountId();
        this.client = proxyClient.client();
        this.proxy = proxy;

        try {
            if (model.getRepositoryPolicyText() != null) {
                proxy.injectCredentialsAndInvokeV2(Translator.setRepositoryPolicyRequest(model), client::setRepositoryPolicy);
            } else {
                try {
                    proxy.injectCredentialsAndInvokeV2(Translator.deleteRepositoryPolicyRequest(model), client::deleteRepositoryPolicy);
                } catch (RepositoryPolicyNotFoundException e) {
                    // there's no policy to delete
                }
            }

            if (model.getLifecyclePolicy() != null) {
                proxy.injectCredentialsAndInvokeV2(Translator.putLifecyclePolicyRequest(model), client::putLifecyclePolicy);
            } else {
                try {
                    proxy.injectCredentialsAndInvokeV2(Translator.deleteLifecyclePolicyRequest(model), client::deleteLifecyclePolicy);
                } catch (LifecyclePolicyNotFoundException e) {
                    // there's no policy to delete
                }
            }

            if (model.getImageTagMutability() != null) {
                proxy.injectCredentialsAndInvokeV2(Translator.putImageTagMutabilityRequest(model, accountId), client::putImageTagMutability);
            }

            if (model.getImageScanningConfiguration() != null) {
                proxy.injectCredentialsAndInvokeV2(Translator.putImageScanningConfigurationRequest(model, accountId), client::putImageScanningConfiguration);
            }

            final DescribeRepositoriesResponse describeResponse = proxy.injectCredentialsAndInvokeV2(Translator.describeRepositoriesRequest(model), client::describeRepositories);
            final String arn = describeResponse.repositories().get(0).repositoryArn();
            model.setArn(arn);
            handleTagging(request.getDesiredResourceTags(), arn);
        } catch (RepositoryNotFoundException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, model.getRepositoryName());
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private void handleTagging(final Map<String, String> tags, final String arn) {
        final Set<Tag> newTags = tags == null ? Collections.emptySet() : new HashSet<>(Translator.translateTagsToSdk(tags));
        final Set<Tag> existingTags = new HashSet<>(proxy.injectCredentialsAndInvokeV2(Translator.listTagsForResourceRequest(arn), client::listTagsForResource).tags());

        final List<String> tagsToRemove = existingTags.stream()
                .filter(tag -> !newTags.contains(tag))
                .map(tag -> tag.key())
                .collect(Collectors.toList());
        final List<Tag> tagsToAdd = newTags.stream()
                .filter(tag -> !existingTags.contains(tag))
                .collect(Collectors.toList());

        if (!CollectionUtils.isNullOrEmpty(tagsToRemove)) proxy.injectCredentialsAndInvokeV2(Translator.untagResourceRequest(tagsToRemove, arn), client::untagResource);
        if (!CollectionUtils.isNullOrEmpty(tagsToAdd)) proxy.injectCredentialsAndInvokeV2(Translator.tagResourceRequest(tagsToAdd, arn), client::tagResource);
    }

}
