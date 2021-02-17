package software.amazon.ecr.repository;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.ecr.model.CreateRepositoryResponse;
import software.amazon.awssdk.services.ecr.model.RepositoryAlreadyExistsException;
import software.amazon.awssdk.services.ecr.EcrClient;

public class CreateHandler extends BaseHandlerStd {
    private static final int MAX_REPO_NAME_LENGTH = 256;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final EcrClient client = proxyClient.client();

        // Auto-generate RepositoryName if not supplied
        if (StringUtils.isNullOrEmpty(model.getRepositoryName())) {
            model.setRepositoryName(
                    IdentifierUtils.generateResourceIdentifier(
                            request.getStackId(),
                            request.getLogicalResourceIdentifier(),
                            request.getClientRequestToken(),
                            MAX_REPO_NAME_LENGTH
                    ).toLowerCase()
            );
        }

        try {
            final CreateRepositoryResponse response = proxy.injectCredentialsAndInvokeV2(
                    Translator.createRepositoryRequest(
                            model,
                            request.getDesiredResourceTags()),
                    client::createRepository);
            model.setArn(response.repository().repositoryArn());
            logger.log(String.format("%s [%s] Created Successfully", ResourceModel.TYPE_NAME, model.getRepositoryName()));
        } catch (RepositoryAlreadyExistsException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, model.getRepositoryName());
        }

        try {
            if (model.getLifecyclePolicy() != null) proxy.injectCredentialsAndInvokeV2(Translator.putLifecyclePolicyRequest(model), client::putLifecyclePolicy);
            if (model.getRepositoryPolicyText() != null) proxy.injectCredentialsAndInvokeV2(Translator.setRepositoryPolicyRequest(model), client::setRepositoryPolicy);
        } catch (AwsServiceException e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .errorCode(HandlerErrorCode.GeneralServiceException)
                .message(e.getMessage())
                .build();
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
