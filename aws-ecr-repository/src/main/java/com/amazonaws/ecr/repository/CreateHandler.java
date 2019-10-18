package com.amazonaws.ecr.repository;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.ecr.model.RepositoryAlreadyExistsException;
import software.amazon.awssdk.services.ecr.EcrClient;

public class CreateHandler extends BaseHandler<CallbackContext> {
    private static final int MAX_REPO_NAME_LENGTH = 256;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final EcrClient client = ClientBuilder.getClient();

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
            proxy.injectCredentialsAndInvokeV2(Helpers.createRepositoryRequest(model), client::createRepository);
            logger.log(String.format("%s [%s] Created Successfully", ResourceModel.TYPE_NAME, model.getRepositoryName()));
        } catch (RepositoryAlreadyExistsException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, model.getRepositoryName());
        }

        if (model.getLifecyclePolicy() != null) proxy.injectCredentialsAndInvokeV2(Helpers.putLifecyclePolicyRequest(model), client::putLifecyclePolicy);
        if (model.getRepositoryPolicyText() != null) proxy.injectCredentialsAndInvokeV2(Helpers.setRepositoryPolicyRequest(model), client::setRepositoryPolicy);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
