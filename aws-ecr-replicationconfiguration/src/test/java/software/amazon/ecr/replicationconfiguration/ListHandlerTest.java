package software.amazon.ecr.replicationconfiguration;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryRequest;
import software.amazon.awssdk.services.ecr.model.ValidationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EcrClient> proxyEcrClient;

    @Mock
    EcrClient ecr;

    private ListHandler handler;

    @BeforeEach
    public void setup() {
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
        handler = new ListHandler();
    }

    @Test
    void handleRequest_SimpleSuccess() {
        doReturn(TestSdkResponseHelper.oneDestinationDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request =
                TestHelper.generateRequestWithPrimaryId(TestHelper.singleDestination());

        // Request
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isZero();
        softly.assertThat(response.getResourceModel()).isNull();
        softly.assertThat(response.getResourceModels()).isNotNull();
        softly.assertThat(response.getResourceModels().get(0)).isEqualTo(request.getDesiredResourceState());
        softly.assertAll();
    }

    @Test
    void handleRequest_NoExistingResourceReturnsSuccessWithEmptyList() {
        doReturn(TestSdkResponseHelper.emptyDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request =
                TestHelper.generateRequest(TestHelper.multipleDestinations());

        // Request
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        // Validation
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isZero();
        softly.assertThat(response.getResourceModel()).isNull();
        softly.assertThat(response.getResourceModels()).isEmpty();
        softly.assertAll();
    }

    @Test
    void handleRequest_ValidationExceptionReturnsFailedEvent() {
        when(proxy.injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any()))
                .thenThrow(ValidationException.builder().build());

        final ResourceHandlerRequest<ResourceModel> request =
                TestHelper.generateRequest(TestHelper.multipleDestinations());


        // Request
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        // Validation
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        softly.assertThat(response.getResourceModel()).isNull();
        softly.assertThat(response.getResourceModels()).isEmpty();
        softly.assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
        softly.assertAll();
    }
}
