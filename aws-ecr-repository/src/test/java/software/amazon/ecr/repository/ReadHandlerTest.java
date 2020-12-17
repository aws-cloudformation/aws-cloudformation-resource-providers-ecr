package software.amazon.ecr.repository;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.EcrException;
import software.amazon.awssdk.services.ecr.model.ImageScanningConfiguration;
import software.amazon.awssdk.services.ecr.model.ImageTagMutability;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.LifecyclePolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryPolicyNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EcrClient> proxyEcrClient;

    @Mock
    EcrClient ecr;

    private ReadHandler handler;

    private final Repository repository = Repository.builder()
            .repositoryName("repo")
            .registryId("id")
            .repositoryArn("arn")
            .imageScanningConfiguration(ImageScanningConfiguration.builder().scanOnPush(true).build())
            .imageTagMutability(ImageTagMutability.MUTABLE)
            .build();

    private final DescribeRepositoriesResponse describeRepositoriesResponse = DescribeRepositoriesResponse.builder()
            .repositories(Collections.singletonList(repository))
            .build();

    private final GetLifecyclePolicyResponse getLifecyclePolicyResponse = GetLifecyclePolicyResponse.builder()
            .repositoryName("repo")
            .lifecyclePolicyText("policy")
            .registryId("id")
            .build();

    private final GetRepositoryPolicyResponse getRepositoryPolicyResponse = GetRepositoryPolicyResponse.builder()
            .repositoryName("repo")
            .policyText("{\"foo\": \"bar\"}")
            .registryId("id")
            .build();

    private final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
            .tags(Collections.singletonList(software.amazon.awssdk.services.ecr.model.Tag.builder().key("key").value("value").build()))
            .build();

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        ecr = mock(EcrClient.class);
        proxy = mock(AmazonWebServicesClientProxy.class);
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
    }

    @Test
    void handleRequest_SimpleSuccess() {
        doReturn(describeRepositoriesResponse,
                getRepositoryPolicyResponse,
                getLifecyclePolicyResponse,
                listTagsForResourceResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final Set<Tag> tags = Collections.singleton(Tag.builder().key("key").value("value").build());

        final Map<String, Object> policyObject = new HashMap<>();
        policyObject.put("foo", "bar");

        final ResourceModel expectedModel = ResourceModel.builder()
                .repositoryName("repo")
                .repositoryPolicyText(policyObject)
                .lifecyclePolicy(LifecyclePolicy.builder()
                        .lifecyclePolicyText("policy")
                        .registryId("id")
                        .build())
                .tags(tags)
                .arn("arn")
                .imageTagMutability("MUTABLE")
                .imageScanningConfiguration(software.amazon.ecr.repository.ImageScanningConfiguration.builder().scanOnPush(true).build())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isNull();
    }

    @Test
    void handleRequest_PoliciesNotFound() {
        doThrow(RepositoryPolicyNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(GetRepositoryPolicyRequest.class), any());

        doThrow(LifecyclePolicyNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(GetLifecyclePolicyRequest.class), any());

        doReturn(describeRepositoriesResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRepositoriesRequest.class), any());

        doReturn(listTagsForResourceResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final Set<software.amazon.ecr.repository.Tag> tags = Collections.singleton(software.amazon.ecr.repository.Tag.builder().key("key").value("value").build());

        final ResourceModel expectedModel = ResourceModel.builder()
                .repositoryName("repo")
                .repositoryPolicyText(null)
                .lifecyclePolicy(null)
                .tags(tags)
                .arn("arn")
                .imageTagMutability("MUTABLE")
                .imageScanningConfiguration(software.amazon.ecr.repository.ImageScanningConfiguration.builder().scanOnPush(true).build())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isNull();
    }

    @Test
    void handleRequest_RepoNotFound() {
        doThrow(RepositoryNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    void handleRequest_NullRepositoryPolicyText() {
        final GetRepositoryPolicyResponse getRepositoryPolicyResponse = GetRepositoryPolicyResponse.builder()
                .repositoryName("repo")
                .registryId("id")
                .build();

        doReturn(describeRepositoriesResponse,
                getRepositoryPolicyResponse,
                getLifecyclePolicyResponse,
                listTagsForResourceResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final Set<Tag> tags = Collections.singleton(Tag.builder().key("key").value("value").build());

        final ResourceModel expectedModel = ResourceModel.builder()
                .repositoryName("repo")
                .lifecyclePolicy(LifecyclePolicy.builder()
                        .lifecyclePolicyText("policy")
                        .registryId("id")
                        .build())
                .tags(tags)
                .arn("arn")
                .imageTagMutability("MUTABLE")
                .imageScanningConfiguration(software.amazon.ecr.repository.ImageScanningConfiguration.builder().scanOnPush(true).build())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isNull();
    }

    @Test
    void handleRequest_AccessDenied() {
        final EcrException exception = (EcrException) EcrException.builder()
            .awsErrorDetails(AwsErrorDetails.builder().errorMessage("message").errorCode("AccessDeniedException").build())
            .build();

        doThrow(exception)
            .when(proxy)
            .injectCredentialsAndInvokeV2(any(GetRepositoryPolicyRequest.class), any());

        doThrow(exception)
            .when(proxy)
            .injectCredentialsAndInvokeV2(any(GetLifecyclePolicyRequest.class), any());

        doThrow(exception)
            .when(proxy)
            .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        doReturn(describeRepositoriesResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(any(DescribeRepositoriesRequest.class), any());

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ResourceModel expectedModel = ResourceModel.builder()
            .repositoryName("repo")
            .repositoryPolicyText(null)
            .lifecyclePolicy(null)
            .tags(null)
            .arn("arn")
            .imageTagMutability("MUTABLE")
            .imageScanningConfiguration(software.amazon.ecr.repository.ImageScanningConfiguration.builder().scanOnPush(true).build())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isNull();
    }
}
