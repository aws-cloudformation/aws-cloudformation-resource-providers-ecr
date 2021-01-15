package software.amazon.ecr.registrypolicy;

import java.time.Duration;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.InvalidParameterException;
import software.amazon.awssdk.services.ecr.model.PutRegistryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.RegistryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ServerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateHandlerTest extends AbstractTestBase {
    @Mock
    private EcrClient ecrMock;

    @Mock
    private ProxyClient<EcrClient> proxyClientMock;

    private AmazonWebServicesClientProxy proxy;
    private UpdateHandler handler;
    private ResourceModel model;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        model = ResourceModel.builder()
                .policyText(REGISTRY_POLICY_INPUT)
                .registryId(TEST_REGISTRY_ID)
                .build();
        handler = new UpdateHandler();

        when(proxyClientMock.client()).thenReturn(ecrMock);
        when(ecrMock.getRegistryPolicy(any(GetRegistryPolicyRequest.class)))
                .thenReturn(GetRegistryPolicyResponse.builder()
                        .registryId(TEST_REGISTRY_ID)
                        .policyText(REGISTRY_POLICY_INPUT_TEXT)
                        .build());
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

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClientMock, logger);

        assertThat(response).isNotNull();
        verify(ecrMock).putRegistryPolicy(any(PutRegistryPolicyRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_MissingPolicyFailsToUpdate() {
        when(ecrMock.putRegistryPolicy(any(PutRegistryPolicyRequest.class)))
                .thenThrow(RegistryPolicyNotFoundException.builder().message("testMissingRegistry").build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, software.amazon.ecr.registrypolicy.CallbackContext> response = handler
                .handleRequest(proxy, request, new software.amazon.ecr.registrypolicy.CallbackContext(), proxyClientMock, logger);

        verify(ecrMock).putRegistryPolicy(any(PutRegistryPolicyRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).startsWith("testMissingRegistry");
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    void handleRequest_EcrServerException() {
        when(ecrMock.putRegistryPolicy(any(PutRegistryPolicyRequest.class)))
                .thenThrow(ServerException.builder().message("testServerException").build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, software.amazon.ecr.registrypolicy.CallbackContext> response = handler
                .handleRequest(proxy, request, new software.amazon.ecr.registrypolicy.CallbackContext(), proxyClientMock, logger);

        verify(ecrMock).putRegistryPolicy(any(PutRegistryPolicyRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).startsWith("testServerException");
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
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
