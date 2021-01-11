package software.amazon.ecr.registrypolicy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.InvalidParameterException;
import software.amazon.awssdk.services.ecr.model.RegistryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ServerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadHandlerTest extends AbstractTestBase {
    @Mock
    private ProxyClient<EcrClient> proxyClientMock;

    @Mock
    private EcrClient ecrMock;

    private ReadHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ResourceModel model;


    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        model = ResourceModel.builder()
                .registryId(TEST_REGISTRY_ID)
                .policyText(REGISTRY_POLICY_INPUT_TEXT)
                .build();

        handler = new ReadHandler();
    }

    @AfterEach
    public void tear_down() {
        verify(ecrMock, atLeastOnce()).serviceName();
    }

    @Test
    void handleRequest_SimpleSuccess() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        when(proxyClientMock.client()).thenReturn(ecrMock);
        when(ecrMock.getRegistryPolicy(any(GetRegistryPolicyRequest.class)))
                .thenReturn(GetRegistryPolicyResponse.builder()
                        .registryId(TEST_REGISTRY_ID)
                        .policyText(REGISTRY_POLICY_OUTPUT_TEXT)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy,
                request,
                new CallbackContext(),
                proxyClientMock,
                logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_RegistryPolicyNotFoundCausesNotFoundError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClientMock.client()).thenReturn(ecrMock);
        when(ecrMock.getRegistryPolicy(any(GetRegistryPolicyRequest.class)))
                .thenThrow(RegistryPolicyNotFoundException.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy,
                request,
                new CallbackContext(),
                proxyClientMock,
                logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void handleRequest_InvalidParameterExceptionCausesInvalidRequestError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClientMock.client()).thenReturn(ecrMock);
        when(ecrMock.getRegistryPolicy(any(GetRegistryPolicyRequest.class)))
                .thenThrow(InvalidParameterException.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy,
                request,
                new CallbackContext(),
                proxyClientMock,
                logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void handleRequest_ServerExceptionCauseserviceInternalError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClientMock.client()).thenReturn(ecrMock);
        when(ecrMock.getRegistryPolicy(any(GetRegistryPolicyRequest.class)))
                .thenThrow(ServerException.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy,
                request,
                new CallbackContext(),
                proxyClientMock,
                logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
    }
}
