package software.amazon.ecr.registrypolicy;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class ReadHandler extends BaseHandlerStd {
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        return proxy
            .initiate("AWS-ECR-RegistryPolicy::Read", proxyClient, request.getDesiredResourceState(),
                    callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall((awsRequest, client) -> getRegistryPolicy(awsRequest, client, proxy, logger))
            .handleError((awsRequest, exception, client, model, context) ->
                    this.handleError(exception, model, context))
            .done(awsResponse -> ProgressEvent.defaultSuccessHandler(
                    Translator.translateFromReadResponse(awsResponse)));
        }
}
