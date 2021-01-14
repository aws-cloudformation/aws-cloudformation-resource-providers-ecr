package software.amazon.ecr.replicationconfiguration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EcrClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final EcrClient client = proxyClient.client();

        DescribeRegistryResponse response;
        try {
            response = describeRegistryResource(proxy, client);

            // If no resource exists then return failed response.
            if (!hasExistingResource(response)) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .status(OperationStatus.FAILED)
                        .errorCode(HandlerErrorCode.NotFound)
                        .build();
            }
            logger.log(String.format("%s [%s] Read Successful", ResourceModel.TYPE_NAME, response.replicationConfiguration()));
        } catch (AwsServiceException e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.GeneralServiceException)
                    .message(e.getMessage())
                    .build();
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(Response.generateResourceModel(request.getAwsAccountId(), response))
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
