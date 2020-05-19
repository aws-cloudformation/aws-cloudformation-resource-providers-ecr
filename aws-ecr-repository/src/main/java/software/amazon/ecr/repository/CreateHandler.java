package software.amazon.ecr.repository;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
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

//public class CreateHandler extends BaseHandler<CallbackContext> {
//    private static final int MAX_REPO_NAME_LENGTH = 256;
//
//    @Override
//    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
//            final AmazonWebServicesClientProxy proxy,
//            final ResourceHandlerRequest<ResourceModel> request,
//            final CallbackContext callbackContext,
//            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final EcrClient client = proxyClient.client();

        // resource can auto-generate a name if not supplied by caller
        // this logic should move up into the CloudFormation engine, but
        // currently exists here for backwards-compatibility with existing models
        if (StringUtils.isNullOrEmpty(model.getRepositoryName())) {
            model.setRepositoryName(
                    IdentifierUtils.generateResourceIdentifier(
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

        if (model.getLifecyclePolicy() != null) proxy.injectCredentialsAndInvokeV2(Translator.putLifecyclePolicyRequest(model), client::putLifecyclePolicy);
        if (model.getRepositoryPolicyText() != null) proxy.injectCredentialsAndInvokeV2(Translator.setRepositoryPolicyRequest(model), client::setRepositoryPolicy);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
