package software.amazon.ecr.registrypolicy;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.RegistryPolicyNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<EcrClient> proxyClient,
            final Logger logger) {

        return proxy
                .initiate("AWS-ECR-RegistryPolicy::List", proxyClient, request.getDesiredResourceState(),
                        callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, client) -> {
                    try {
                        return getRegistryPolicy(awsRequest, proxyClient, proxy, logger);
                    } catch (RegistryPolicyNotFoundException e) {
                        return null;
                    }
                })
                .handleError((awsRequest, exception, client, model, context) ->
                        this.handleError(exception, Collections.singletonList(model), context))
                .done(Translator::translateToListResponseEvent);
    }
}
