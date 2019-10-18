package com.amazonaws.ecr.repository;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;

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
            response = proxy.injectCredentialsAndInvokeV2(Helpers.describeRepositoriesRequest(model), ClientBuilder.getClient()::describeRepositories);
        } catch (RepositoryNotFoundException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, model.getRepositoryName());
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(Helpers.buildModel(proxy, response.repositories().get(0)))
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
