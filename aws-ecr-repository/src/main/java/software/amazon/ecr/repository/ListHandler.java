package software.amazon.ecr.repository;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import static software.amazon.ecr.repository.ReadHandler.buildModel;

public class ListHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        final DescribeRepositoriesResponse response;
        response = proxy.injectCredentialsAndInvokeV2(Translator.describeRepositoriesRequest(request.getNextToken()), proxyClient.client()::describeRepositories);

        final List<ResourceModel> models = response
                .repositories()
                .stream()
                .map(repository -> buildModel(proxy, proxyClient, repository))
                .collect(Collectors.toList());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(response.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
