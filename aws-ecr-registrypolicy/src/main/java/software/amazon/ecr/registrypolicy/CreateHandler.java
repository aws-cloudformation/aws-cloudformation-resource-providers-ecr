package software.amazon.ecr.registrypolicy;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.PutRegistryPolicyResponse;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/*
 * https://github.com/aws-cloudformation/cloudformation-cli-java-plugin/blob/master/src/main/java/software/amazon/cloudformation/proxy/CallChain.java
 * PutRegistryPolicy will overwrite existing policy, but CreateHandler
 * "MUST return FAILED with an AlreadyExists error code if the resource already existed prior to the create request."
 * for more information -> https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
 */
public class CreateHandler extends BaseHandlerStd {
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress -> checkForPreCreateResourceExistence(proxy, request, proxyClient, progress, logger))
            .then(progress -> createResource(proxy, proxyClient, progress, logger))
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkForPreCreateResourceExistence(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final ProxyClient<EcrClient> proxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progressEvent,
            final Logger logger) {

        logger.log("AWS-ECR-RegistryPolicy::Create::PreExistenceCheck");

        ProgressEvent<ResourceModel, CallbackContext> resultFromRead =
                new ReadHandler().handleRequest(proxy, request, progressEvent.getCallbackContext(), proxyClient, logger);

        if (resultFromRead.isSuccess()) {
            return ProgressEvent.defaultFailureHandler(new CfnAlreadyExistsException(ResourceModel.TYPE_NAME,
                            progressEvent.getResourceModel().getRegistryId()),
                    HandlerErrorCode.AlreadyExists);
        }

        if (resultFromRead.getErrorCode() != null && resultFromRead.getErrorCode().equals(HandlerErrorCode.NotFound)) {
            return ProgressEvent.progress(progressEvent.getResourceModel(), progressEvent.getCallbackContext());
        }

        // Encountered an error other than NotFound
        return resultFromRead;
    }

    private ProgressEvent<ResourceModel, CallbackContext> createResource(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<EcrClient> proxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progressEvent,
            final Logger logger) {

        return progressEvent.then(progress ->
                proxy.initiate("AWS-ECR-RegistryPolicy::Create", proxyClient, progress.getResourceModel(),
                        progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToPutRequest)
                        .makeServiceCall((awsRequest, client) -> {
                            PutRegistryPolicyResponse response = proxy.injectCredentialsAndInvokeV2(awsRequest,
                                    proxyClient.client()::putRegistryPolicy);
                            logger.log(ResourceModel.TYPE_NAME + " successfully created.");
                            return response;
                        })
                        .handleError((awsRequest, exception, client, model, context) ->
                                this.handleError(exception, model,context))
                        .progress()
        );
    }
}
