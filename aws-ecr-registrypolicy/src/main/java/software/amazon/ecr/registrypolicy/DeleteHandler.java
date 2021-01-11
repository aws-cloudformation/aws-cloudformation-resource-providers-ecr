package software.amazon.ecr.registrypolicy;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DeleteRegistryPolicyResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends software.amazon.ecr.registrypolicy.BaseHandlerStd {

    protected ProgressEvent<ResourceModel, software.amazon.ecr.registrypolicy.CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final software.amazon.ecr.registrypolicy.CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                proxy.initiate("AWS-ECR-RegistryPolicy::Delete", proxyClient, progress.getResourceModel(),
                        progress.getCallbackContext())
                    .translateToServiceRequest(t -> Translator.translateToDeleteRequest())
                    .makeServiceCall((awsRequest, client) -> {
                        DeleteRegistryPolicyResponse response = proxy.
                                injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteRegistryPolicy);
                        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
                        return response;
                    })
                    .handleError((awsRequest, exception, client, model, context) -> this.handleError(exception, model,context))
                    .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .build())
                );
    }
}
