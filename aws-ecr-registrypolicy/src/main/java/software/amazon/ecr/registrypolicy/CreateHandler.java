package software.amazon.ecr.registrypolicy;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.PutRegistryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.RegistryPolicyNotFoundException;
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
            .then(progress -> checkForPreCreateResourceExistence(proxy, proxyClient, progress, logger))
            .then(progress -> createResource(proxy, proxyClient, progress, logger))
            .then(progress -> verifyResourceExistence(proxy, proxyClient, progress, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkForPreCreateResourceExistence(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<EcrClient> proxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progressEvent,
            final Logger logger) {

        return progressEvent.then(progress ->
                proxy.initiate("AWS-ECR-RegistryPolicy::Create::PreExistenceCheck", proxyClient, progress.getResourceModel(),
                        progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadRequest)
                        .makeServiceCall((awsRequest, client) -> {
                            try {
                                return getRegistryPolicy(awsRequest, proxyClient, proxy, logger);
                            } catch (RegistryPolicyNotFoundException e) {
                                return null;
                            }
                        })
                        .handleError((awsRequest, exception, client, model, context) ->
                                this.handleError(exception, model, context))
                        .done((response -> done(response, progressEvent))));
    }

    private ProgressEvent<ResourceModel, CallbackContext> done(final GetRegistryPolicyResponse response,
            final ProgressEvent<ResourceModel, CallbackContext> progressEvent) {
        if (response != null) {
            return  ProgressEvent.defaultFailureHandler(new CfnAlreadyExistsException(ResourceModel.TYPE_NAME,
                            progressEvent.getResourceModel().getRegistryId()),
                    HandlerErrorCode.AlreadyExists);
        }

        return ProgressEvent.progress(progressEvent.getResourceModel(), progressEvent.getCallbackContext());
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

    private ProgressEvent<ResourceModel, CallbackContext> verifyResourceExistence(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<EcrClient> proxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progressEvent,
            final Logger logger) {
        return progressEvent.then(progress ->
                proxy.initiate("AWS-ECR-RegistryPolicy::PostCreateCheck", proxyClient, progress.getResourceModel(),
                        progressEvent.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadRequest)
                        .makeServiceCall((awsRequest, client) -> getRegistryPolicy(awsRequest, client, proxy, logger))
                        .handleError((awsRequest, exception, client, model, context) ->
                                this.handleError(exception, model, context))
                        .done(awsResponse -> ProgressEvent.defaultSuccessHandler(
                                Translator.translateFromReadResponse(awsResponse))));
    }
}
