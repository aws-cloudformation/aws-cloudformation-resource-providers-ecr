package software.amazon.ecr.registrypolicy;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyResponse;
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
            .makeServiceCall((awsRequest, client) -> {
                GetRegistryPolicyResponse response = proxy.injectCredentialsAndInvokeV2(awsRequest,
                        proxyClient.client()::getRegistryPolicy);
                logger.log(ResourceModel.TYPE_NAME + " has successfully been read.");
                return response;
            })
            .handleError((awsRequest, exception, client, model, context) ->
                    this.handleError(exception, model, context))
            .done(awsResponse -> {
                    return ProgressEvent.defaultSuccessHandler(
                            Translator.translateFromReadResponse(awsResponse));
            });
        }


}
