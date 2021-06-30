package software.amazon.ecr.registrypolicy;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.InvalidParameterException;
import software.amazon.awssdk.services.ecr.model.RegistryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

 GetRegistryPolicyResponse getRegistryPolicy(GetRegistryPolicyRequest request,
         ProxyClient<EcrClient> proxyClient,
         AmazonWebServicesClientProxy proxy,
         Logger logger) {
   GetRegistryPolicyResponse response = proxy.injectCredentialsAndInvokeV2(request,
           proxyClient.client()::getRegistryPolicy);
   logger.log(ResourceModel.TYPE_NAME + " has successfully been read.");
   return response;
 }

  protected ProgressEvent<ResourceModel, CallbackContext> handleError(
          final Exception e,
          final ResourceModel resourceModel,
          final CallbackContext callbackContext) {

    BaseHandlerException ex = convertException(e);
    return ProgressEvent.failed(resourceModel, callbackContext, ex.getErrorCode(), ex.getMessage());
  }

  protected ProgressEvent<ResourceModel, CallbackContext> handleError(
          final Exception e,
          final List<ResourceModel> models,
          final CallbackContext callbackContext) {

    BaseHandlerException ex = convertException(e);
    ProgressEvent <ResourceModel, CallbackContext> event = ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .callbackContext(callbackContext)
            .nextToken(null)
            .status(OperationStatus.SUCCESS)
            .build();
    event.setStatus(OperationStatus.FAILED);
    event.setErrorCode(ex.getErrorCode());
    event.setMessage(ex.getMessage());

    return event;
  }

  private BaseHandlerException convertException(final Exception e) {
    BaseHandlerException ex;

    if (e instanceof RegistryPolicyNotFoundException) {
      ex = new CfnNotFoundException(e);
    } else if (e instanceof InvalidParameterException || e instanceof ValidationException) {
      ex = new CfnInvalidRequestException(e);
    } else if (e instanceof SdkException) {
      ex = new CfnServiceInternalErrorException(e);
    } else if (e instanceof CfnAlreadyExistsException) {
      ex = new CfnAlreadyExistsException(e);
    } else {
      ex = new CfnGeneralServiceException(e);
    }

    return ex;
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<EcrClient> proxyClient,
    final Logger logger);
}
