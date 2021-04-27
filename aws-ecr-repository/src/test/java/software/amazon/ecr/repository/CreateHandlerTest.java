package software.amazon.ecr.repository;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.ImageScanningConfiguration;
import software.amazon.awssdk.services.ecr.model.InvalidParameterException;
import software.amazon.awssdk.services.ecr.model.KmsException;
import software.amazon.awssdk.services.ecr.model.PutLifecyclePolicyRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.ecr.model.CreateRepositoryRequest;
import software.amazon.awssdk.services.ecr.model.CreateRepositoryResponse;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.RepositoryAlreadyExistsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private Repository repo = Repository.builder()
            .repositoryName("repo")
            .registryId("id")
            .repositoryArn("arn")
            .imageTagMutability("IMMUTABLE")
            .imageScanningConfiguration(
                    ImageScanningConfiguration.builder()
                            .scanOnPush(true)
                            .build())
            .build();

    private CreateRepositoryResponse createRepositoryResponse = CreateRepositoryResponse.builder()
            .repository(repo)
            .build();

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
        ecr = mock(EcrClient.class);
        proxy = mock(AmazonWebServicesClientProxy.class);
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final LifecyclePolicy lifecyclePolicy = LifecyclePolicy.builder()
                .lifecyclePolicyText("policy")
                .registryId("id")
                .build();

        final Map<String, Object> repositoryPolicy = new HashMap<>();
        repositoryPolicy.put("foo", "bar");

        final Set<Tag> tags = Collections.singleton(Tag.builder().key("key").value("value").build());
        final Map<String, String> tagsMap = tags.stream().collect(Collectors.toMap(tag -> tag.getKey(), tag -> tag.getValue()));

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .lifecyclePolicy(lifecyclePolicy)
                .repositoryPolicyText(repositoryPolicy)
                .tags(tags)
                .imageTagMutability("IMMUTABLE")
                .imageScanningConfiguration(
                        software.amazon.ecr.repository.ImageScanningConfiguration.builder()
                                .scanOnPush(true)
                                .build())
                .encryptionConfiguration(
                        software.amazon.ecr.repository.EncryptionConfiguration.builder()
                                .encryptionType("KMS")
                                .build())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(tagsMap)
                .build();

        doReturn(createRepositoryResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateRepositoryRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

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
    public void handleRequest_AutogenerateName() {
        String stackName = "TestStack";
        String stackId = String.join("/",
                "arn:aws:cloudformation:us-west-2:123123123123:stack",
                stackName,
                UUID.randomUUID().toString());
        String resourceId = "MyResource";

        final ResourceModel model = ResourceModel.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .stackId(stackId)
                .logicalResourceIdentifier(resourceId)
                .clientRequestToken("token")
                .desiredResourceState(model)
                .build();

        doReturn(createRepositoryResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateRepositoryRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        String repositoryName = String.join("-", stackName, resourceId).toLowerCase();
        assertThat(response.getResourceModel().getRepositoryName()).startsWith(repositoryName);
    }

    @Test
    public void handleRequest_RepoNameExists() {
        doThrow(RepositoryAlreadyExistsException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(ResourceAlreadyExistsException.class,
                () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger));
    }

    @Test
    public void handleRequest_InvalidPolicy() {
        final ResourceModel model = ResourceModel.builder()
            .repositoryName("repo")
            .lifecyclePolicy(LifecyclePolicy.builder().lifecyclePolicyText("policy").build())
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .logicalResourceIdentifier("MyResource")
            .clientRequestToken("token")
            .desiredResourceState(model)
            .build();

        doReturn(createRepositoryResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(any(CreateRepositoryRequest.class), any());

        doThrow(InvalidParameterException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(any(PutLifecyclePolicyRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);


        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
        assertThat(response.getResourceModel().getArn()).isEqualTo("arn");

        assertThat(response).isNotNull();
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getResourceModel().getRepositoryName()).isNotNull();
    }

    @Test
    public void handleRequest_InvalidEncryptionConfiguration() {
        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .encryptionConfiguration(EncryptionConfiguration.builder().encryptionType("AES256").build())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .logicalResourceIdentifier("MyResource")
                .clientRequestToken("token")
                .desiredResourceState(model)
                .build();

        doThrow(KmsException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateRepositoryRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
        assertThat(response.getResourceModel().getArn()).isNull();

        assertThat(response).isNotNull();
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getResourceModel().getRepositoryName()).isNotNull();
    }
}
