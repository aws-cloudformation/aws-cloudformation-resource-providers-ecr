package software.amazon.ecr.repository;

import software.amazon.awssdk.services.ecr.model.EcrException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
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

public class ReadHandler extends BaseHandlerStd {

    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final DescribeRepositoriesResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(Translator.describeRepositoriesRequest(model), proxyClient.client()::describeRepositories);
            logger.log(String.format("%s [%s] Read Successful", ResourceModel.TYPE_NAME, model.getRepositoryName()));
        } catch (RepositoryNotFoundException e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .errorCode(HandlerErrorCode.NotFound)
                    .status(OperationStatus.FAILED)
                    .build();
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(buildModel(proxy, proxyClient, response.repositories().get(0), logger))
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

    public static ResourceModel buildModel(final AmazonWebServicesClientProxy proxy, final ProxyClient<EcrClient> proxyClient, final Repository repo, final Logger logger) {
        final String arn = repo.repositoryArn();
        final String repositoryUri = repo.repositoryUri();
        final String repositoryName = repo.repositoryName();
        final String registryId = repo.registryId();
        final EcrClient client = proxyClient.client();

        Map<String, Object> repositoryPolicyText = null;
        LifecyclePolicy lifecyclePolicy = null;
        Set<Tag> tags = null;

        try {
            final GetRepositoryPolicyResponse getRepositoryPolicyResponse = proxy.injectCredentialsAndInvokeV2(Translator.getRepositoryPolicyRequest(repositoryName, registryId), client::getRepositoryPolicy);
            repositoryPolicyText = deserializePolicyText(getRepositoryPolicyResponse.policyText());
        } catch (RepositoryPolicyNotFoundException e) {
            // RepositoryPolicyText is not required so it might not exist
        } catch (EcrException e) {
            // This is a short term fix for GetAtt backwards compatibility
            if (!e.awsErrorDetails().errorCode().equals(ACCESS_DENIED_ERROR_CODE)) {
                throw new CfnGeneralServiceException(e.getMessage(), e);
            }
            logger.log(String.format("AccessDenied error: %s for Repository: %s", e.getMessage(), repo.toString()));
        }

        try {
            final GetLifecyclePolicyResponse getLifecyclePolicyResponse = proxy.injectCredentialsAndInvokeV2(Translator.getLifecyclePolicyRequest(repositoryName, registryId), client::getLifecyclePolicy);
            lifecyclePolicy = LifecyclePolicy.builder()
                    .registryId(getLifecyclePolicyResponse.registryId())
                    .lifecyclePolicyText(getLifecyclePolicyResponse.lifecyclePolicyText())
                    .build();
        } catch (LifecyclePolicyNotFoundException e) {
            // LifecyclePolicy is not required so it might not exist
        } catch (EcrException e) {
            if (!e.awsErrorDetails().errorCode().equals(ACCESS_DENIED_ERROR_CODE)) {
                throw new CfnGeneralServiceException(e.getMessage(), e);
            }
            logger.log(String.format("AccessDenied error: %s for Repository: %s", e.getMessage(), repo.toString()));
        }

        try {
            final ListTagsForResourceResponse listTagsResponse = proxy.injectCredentialsAndInvokeV2(Translator.listTagsForResourceRequest(arn), client::listTagsForResource);
            tags = Translator.translateTagsFromSdk(listTagsResponse.tags());
        } catch (EcrException e) {
            if (!e.awsErrorDetails().errorCode().equals(ACCESS_DENIED_ERROR_CODE)) {
                throw new CfnGeneralServiceException(e.getMessage(), e);
            }
            logger.log(String.format("AccessDenied error: %s for Repository: %s", e.getMessage(), repo.toString()));
        }

        EncryptionConfiguration encryptionConfiguration = null;
        if (repo.encryptionConfiguration() != null) {
            encryptionConfiguration = EncryptionConfiguration.builder()
                    .encryptionType(repo.encryptionConfiguration().encryptionTypeAsString())
                    .build();
            if (repo.encryptionConfiguration().kmsKey() != null) {
                encryptionConfiguration.setKmsKey(repo.encryptionConfiguration().kmsKey());
            }
        }

        return ResourceModel.builder()
                .repositoryName(repositoryName)
                .lifecyclePolicy(lifecyclePolicy)
                .repositoryPolicyText(repositoryPolicyText)
                .tags(tags)
                .arn(arn)
                .repositoryUri(repositoryUri)
                .imageScanningConfiguration(ImageScanningConfiguration.builder().scanOnPush(repo.imageScanningConfiguration().scanOnPush()).build())
                .imageTagMutability(repo.imageTagMutability().toString())
                .encryptionConfiguration(encryptionConfiguration)
                .build();
    }
}
