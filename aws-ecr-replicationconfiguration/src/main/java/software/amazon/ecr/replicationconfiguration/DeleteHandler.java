package software.amazon.ecr.replicationconfiguration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.PutReplicationConfigurationRequest;
import software.amazon.awssdk.services.ecr.model.ReplicationConfiguration;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;

public class DeleteHandler extends BaseHandlerStd {

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
            // Ensure resource exists on the ECR Registry.
            if (!hasExistingResource(describeRegistryResource(proxy, client))) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .status(OperationStatus.FAILED)
                        .errorCode(HandlerErrorCode.NotFound)
                        .build();
            }

            PutReplicationConfigurationRequest deleteRequest = PutReplicationConfigurationRequest.builder()
                    .replicationConfiguration(ReplicationConfiguration.builder()
                            .rules(Collections.emptyList())
                            .build())
                    .build();

            proxy.injectCredentialsAndInvokeV2(
                    deleteRequest,
                    client::putReplicationConfiguration);

            logger.log(String.format("%s [%s] Deleted Successfully", ResourceModel.TYPE_NAME, model.getRegistryId()));
        } catch (AwsServiceException e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.GeneralServiceException)
                    .message(e.getMessage())
                    .build();
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
