package software.amazon.ecr.repository;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EcrClient> proxyEcrClient;

    @Mock
    EcrClient ecr;

    private ResourceModel model;
    private ResourceHandlerRequest<ResourceModel> request;
    private DeleteHandler handler;

    @BeforeEach
    public void setup() {
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
        model = ResourceModel.builder().repositoryName("repo").build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        handler = new DeleteHandler();
    }

    @Test
    void handleRequest_SimpleSuccess() {
        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_RepoNotFound() {
        doThrow(RepositoryNotFoundException.class).when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final CallbackContext context = new CallbackContext();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, context, proxyEcrClient, logger))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
