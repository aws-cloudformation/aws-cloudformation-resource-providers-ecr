package com.amazonaws.ecr.repository;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final DescribeRepositoriesResponse response;
        response = proxy.injectCredentialsAndInvokeV2(Helpers.describeRepositoriesRequest(request.getNextToken()), ClientBuilder.getClient()::describeRepositories);

        final List<ResourceModel> models = response
                .repositories()
                .stream()
                .map(repository -> Helpers.buildModel(proxy, repository))
                .collect(Collectors.toList());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(response.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
