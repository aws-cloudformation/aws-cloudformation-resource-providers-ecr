package software.amazon.ecr.replicationconfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryRequest;
import software.amazon.awssdk.services.ecr.model.PutReplicationConfigurationRequest;
import software.amazon.awssdk.services.ecr.model.ValidationException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EcrClient> proxyEcrClient;

    @Mock
    EcrClient ecr;

    private CreateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        handler = new CreateHandler();
        ecr = mock(EcrClient.class);
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        // Setup
        // Mock DescribeRegistryRequest call
        doReturn(TestSdkResponseHelper.emptyDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Mock PutReplicationConfigurationRequest call
        doReturn(TestSdkResponseHelper.oneDestinationPutReplicationConfigurationResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(PutReplicationConfigurationRequest.class), any());

        // Generate test request
        final ResourceHandlerRequest<ResourceModel> request = TestHelper.generateRequest(TestHelper.singleDestination());

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
    public void handleRequestWithRepoFilter_SimpleSuccess() {
        // Setup
        // Mock DescribeRegistryRequest call
        doReturn(TestSdkResponseHelper.emptyDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Mock PutReplicationConfigurationRequest call
        doReturn(TestSdkResponseHelper.oneDestinationOneFilterPutReplicationConfigurationResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(PutReplicationConfigurationRequest.class), any());

        // Generate test request
        final ResourceHandlerRequest<ResourceModel> requestWithRepoFilter = TestHelper.generateRequestWithFilter(TestHelper.singleDestination(), TestHelper.singleFilter());

        // Request with repo filter
        final ProgressEvent<ResourceModel, CallbackContext> responseWithRepoFilter
                = handler.handleRequest(proxy, requestWithRepoFilter, new CallbackContext(), proxyEcrClient, logger);

        // Validation for repo filter
        assertThat(responseWithRepoFilter).isNotNull();
        assertThat(responseWithRepoFilter.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(responseWithRepoFilter.getCallbackContext()).isNull();
        assertThat(responseWithRepoFilter.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(responseWithRepoFilter.getResourceModel()).isEqualTo(requestWithRepoFilter.getDesiredResourceState());
        assertThat(responseWithRepoFilter.getResourceModels()).isNull();
        assertThat(responseWithRepoFilter.getMessage()).isNull();
        assertThat(responseWithRepoFilter.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_AlreadyExistsException() {
        // Setup
        // Mock DescribeRegistryRequest call
        doReturn(TestSdkResponseHelper.oneDestinationDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Generate test request
        final ResourceHandlerRequest<ResourceModel> request = TestHelper.generateRequest(TestHelper.singleDestination());

        // Request and Validation
        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    public void handleRequestWithRepoFilter_AlreadyExistsException() {
        // Setup
        // Mock DescribeRegistryRequest call
        doReturn(TestSdkResponseHelper.oneDestinationOneFilterDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Generate test request with repo filter
        final ResourceHandlerRequest<ResourceModel> requestWithRepoFilter = TestHelper.generateRequestWithFilter(TestHelper.singleDestination(), TestHelper.singleFilter());

        // Request and Validation for repo filter
        assertThatThrownBy(() -> handler.handleRequest(proxy, requestWithRepoFilter, new CallbackContext(), proxyEcrClient, logger))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    public void handleRequest_ValidationException() {
        // Setup
        // Mock DescribeRegistryRequest call
        doReturn(TestSdkResponseHelper.emptyDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Mock PutReplicationConfigurationRequest call
        doThrow(ValidationException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(PutReplicationConfigurationRequest.class), any());

        // Generate test request
        final ResourceHandlerRequest<ResourceModel> request = TestHelper.generateRequest(TestHelper.singleDestination());

        // Request
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        // Generate test request with repo filter
        final ResourceHandlerRequest<ResourceModel> requestWithRepoFilter = TestHelper.generateRequestWithFilter(TestHelper.singleDestination(), TestHelper.singleFilter());

        // Request for repo filter
        final ProgressEvent<ResourceModel, CallbackContext> responseWithRepoFilter
                = handler.handleRequest(proxy, requestWithRepoFilter, new CallbackContext(), proxyEcrClient, logger);

        // Validation
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);

        // Validation for repo filter
        assertThat(responseWithRepoFilter).isNotNull();
        assertThat(responseWithRepoFilter.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(responseWithRepoFilter.getCallbackContext()).isNull();
        assertThat(responseWithRepoFilter.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(responseWithRepoFilter.getResourceModel()).isEqualTo(requestWithRepoFilter.getDesiredResourceState());
        assertThat(responseWithRepoFilter.getResourceModels()).isNull();
        assertThat(responseWithRepoFilter.getMessage()).isNull();
        assertThat(responseWithRepoFilter.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequest_GeneralException() {
        // Setup
        // Mock DescribeRegistryRequest call
        doReturn(TestSdkResponseHelper.emptyDescribeRegistryResponse())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRegistryRequest.class), any());

        // Mock PutReplicationConfigurationRequest call
        doThrow(AwsServiceException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(PutReplicationConfigurationRequest.class), any());

        // Generate test request
        final ResourceHandlerRequest<ResourceModel> request = TestHelper.generateRequest(TestHelper.singleDestination());

        // Generate test request for repo filter
        final ResourceHandlerRequest<ResourceModel> requestWithRepoFilter = TestHelper.generateRequestWithFilter(TestHelper.singleDestination(), TestHelper.singleFilter());

        // Request
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        // Request for repo filter
        final ProgressEvent<ResourceModel, CallbackContext> responseWithRepoFilter
                = handler.handleRequest(proxy, requestWithRepoFilter, new CallbackContext(), proxyEcrClient, logger);

        // Validation
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);

        // Validation for repo filter
        assertThat(responseWithRepoFilter).isNotNull();
        assertThat(responseWithRepoFilter.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(responseWithRepoFilter.getCallbackContext()).isNull();
        assertThat(responseWithRepoFilter.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(responseWithRepoFilter.getResourceModel()).isEqualTo(requestWithRepoFilter.getDesiredResourceState());
        assertThat(responseWithRepoFilter.getResourceModels()).isNull();
        assertThat(responseWithRepoFilter.getMessage()).isNull();
        assertThat(responseWithRepoFilter.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }
}
