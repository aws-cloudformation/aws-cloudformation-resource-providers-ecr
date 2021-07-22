package software.amazon.ecr.replicationconfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EcrClient> proxyEcrClient;

    @Mock
    EcrClient ecr;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        handler = new ReadHandler();
        ecr = mock(EcrClient.class);
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
    }

    @Test
    void handleRequest_SimpleSuccess() {
        // Setup
        // Mock DescribeRegistryRequest call
        doReturn(TestSdkResponseHelper.oneDestinationDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Generate test request
        final ResourceHandlerRequest<ResourceModel> request =
                TestHelper.generateRequestWithPrimaryId(TestHelper.singleDestination());

        // Request
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        // Validation
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_NoExistingResourceResponse() {
        // Setup
        // Mock DescribeRegistryRequest call
        doReturn(TestSdkResponseHelper.emptyDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Generate test request
        final ResourceHandlerRequest<ResourceModel> request =
                TestHelper.generateRequest(TestHelper.multipleDestinations());

        // Request
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        // Validation
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    void handleRequest_GeneralException() {
        // Setup
        // Mock DescribeRegistryRequest call
        doThrow(AwsServiceException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Generate test request
        final ResourceHandlerRequest<ResourceModel> request =
                TestHelper.generateRequest(TestHelper.singleDestination());

        // Request
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        // Validation
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }
}
