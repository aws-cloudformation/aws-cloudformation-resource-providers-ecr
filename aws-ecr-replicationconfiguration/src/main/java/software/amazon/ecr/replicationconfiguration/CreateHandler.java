package software.amazon.ecr.replicationconfiguration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.InvalidParameterException;
import software.amazon.awssdk.services.ecr.model.PutReplicationConfigurationResponse;
import software.amazon.awssdk.services.ecr.model.ValidationException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final EcrClient client = proxyClient.client();

        try {
            // Set primary id for the requested resource.
            model.setRegistryId(request.getAwsAccountId());

            // Ensure resource does NOT exist on the ECR Registry.
            if (hasExistingResource(describeRegistryResource(proxy, client))) {
                throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, model.getRegistryId());
            }

            final PutReplicationConfigurationResponse response = proxy.injectCredentialsAndInvokeV2(
                    Translator.putReplicationConfiguration(model),
                    client::putReplicationConfiguration);
            logger.log(String.format("%s [%s] Created Successfully", ResourceModel.TYPE_NAME, response.replicationConfiguration()));
        } catch (InvalidParameterException | ValidationException e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.InvalidRequest)
                    .message(e.getMessage())
                    .build();
        } catch (AwsServiceException e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.GeneralServiceException)
                    .message(e.getMessage())
                    .build();
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
