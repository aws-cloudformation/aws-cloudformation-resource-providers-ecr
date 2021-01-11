package software.amazon.ecr.registrypolicy;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.PutRegistryPolicyResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        // Read first to verify resource exists
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> verifyResourceExists(proxy, request, proxyClient, progress, logger))
                .then(progress -> updateResource(proxy, proxyClient, progress, logger))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateResource(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<EcrClient> proxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progressEvent,
            final Logger logger) {
        return progressEvent.then(progress ->
                proxy.initiate("AWS-ECR-RegistryPolicy::Update::first", proxyClient, progress.getResourceModel(),
                        progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToPutRequest)
                    .makeServiceCall((awsRequest, client) -> {

                        PutRegistryPolicyResponse response = proxy.injectCredentialsAndInvokeV2(awsRequest,
                                proxyClient.client()::putRegistryPolicy);
                        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
                        return response;
                    })
                    .handleError((awsRequest, exception, client, model, context) ->
                            this.handleError(exception, model,context))
                    .progress()
        );
    }

    private ProgressEvent<ResourceModel, CallbackContext> verifyResourceExists(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final ProxyClient<EcrClient> proxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progressEvent,
            final Logger logger) {
        return progressEvent.then(progress ->
                proxy.initiate("AWS-ECR-RegistryPolicy::PreUpdateCheck", proxyClient, request.getDesiredResourceState(),
                        progressEvent.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToReadRequest)
                    .makeServiceCall((awsRequest, client) -> {
                        GetRegistryPolicyResponse response = proxy.injectCredentialsAndInvokeV2(awsRequest,
                            proxyClient.client()::getRegistryPolicy);
                        logger.log(ResourceModel.TYPE_NAME + " has successfully been read.");
                        return response;
                    })
                    .handleError((awsRequest, exception, client, model, context) ->
                        this.handleError(exception, model, context))
                    .progress()
                );
    }
}
