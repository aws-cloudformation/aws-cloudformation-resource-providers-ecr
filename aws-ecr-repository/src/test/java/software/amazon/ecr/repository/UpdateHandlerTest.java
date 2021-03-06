package software.amazon.ecr.repository;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DeleteLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.DeleteLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.DeleteRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.DeleteRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.EncryptionConfiguration;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.ImageScanningConfiguration;
import software.amazon.awssdk.services.ecr.model.LifecyclePolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ecr.model.PutImageScanningConfigurationRequest;
import software.amazon.awssdk.services.ecr.model.PutImageScanningConfigurationResponse;
import software.amazon.awssdk.services.ecr.model.PutImageTagMutabilityRequest;
import software.amazon.awssdk.services.ecr.model.PutImageTagMutabilityResponse;
import software.amazon.awssdk.services.ecr.model.PutLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.PutLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.SetRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.SetRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.TagResourceRequest;
import software.amazon.awssdk.services.ecr.model.TagResourceResponse;
import software.amazon.awssdk.services.ecr.model.UntagResourceRequest;
import software.amazon.awssdk.services.ecr.model.UntagResourceResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EcrClient> proxyEcrClient;

    @Mock
    EcrClient ecr;

    private UpdateHandler handler;


    private final Repository repo = Repository.builder()
            .repositoryName("repo")
            .registryId("id")
            .repositoryArn("arn")
            .repositoryUri("uri")
            .imageTagMutability("IMMUTABLE")
            .imageScanningConfiguration(ImageScanningConfiguration.builder()
                    .scanOnPush(true)
                    .build())
            .encryptionConfiguration(EncryptionConfiguration.builder()
                    .encryptionType("AES256")
                    .build())
            .build();

    private final DescribeRepositoriesResponse describeRepositoriesResponse = DescribeRepositoriesResponse.builder()
            .repositories(Collections.singletonList(repo))
            .build();

    private final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
            .tags(Collections.emptyList())
            .build();

    private final GetRepositoryPolicyResponse getRepositoryPolicyResponse = GetRepositoryPolicyResponse.builder().build();
    private final GetLifecyclePolicyResponse getLifecyclePolicyResponse = GetLifecyclePolicyResponse.builder().build();

    private final SetRepositoryPolicyResponse setRepositoryPolicyResponse = SetRepositoryPolicyResponse.builder().build();
    private final PutLifecyclePolicyResponse putLifecyclePolicyResponse = PutLifecyclePolicyResponse.builder().build();

    private final DeleteRepositoryPolicyResponse deleteRepositoryPolicyResponse = DeleteRepositoryPolicyResponse.builder().build();
    private final DeleteLifecyclePolicyResponse deleteLifecyclePolicyResponse = DeleteLifecyclePolicyResponse.builder().build();

    private final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
    private final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();

    private final PutImageTagMutabilityResponse putImageTagMutabilityResponse = PutImageTagMutabilityResponse.builder().build();
    private final PutImageScanningConfigurationResponse putImageScanningConfigurationResponse = PutImageScanningConfigurationResponse.builder().build();

    private final ResourceModel previousModel = ResourceModel.builder()
            .repositoryName("repo")
            .build();

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
    }

    @Test
    void handleRequest_SimpleSuccess() {
        final List<software.amazon.awssdk.services.ecr.model.Tag> existingTags = ImmutableList.of(
                software.amazon.awssdk.services.ecr.model.Tag.builder().key("key1").value("val1").build(),
                software.amazon.awssdk.services.ecr.model.Tag.builder().key("key2").value("val2").build()
        );
        final Set<Tag> newTags = ImmutableSet.of(
                Tag.builder().key("key1").value("val1").build(),
                Tag.builder().key("key2updated").value("val2").build()
        );
        final Map<String, String> newTagsMap = newTags.stream().collect(Collectors.toMap(tag -> tag.getKey(), tag -> tag.getValue()));

        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
                .tags(existingTags)
                .build();

        doReturn(setRepositoryPolicyResponse).when(proxy).injectCredentialsAndInvokeV2(any(SetRepositoryPolicyRequest.class), any());
        doReturn(putLifecyclePolicyResponse).when(proxy).injectCredentialsAndInvokeV2(any(PutLifecyclePolicyRequest.class), any());
        doReturn(describeRepositoriesResponse).when(proxy).injectCredentialsAndInvokeV2(any(DescribeRepositoriesRequest.class), any());
        doReturn(listTagsForResourceResponse).when(proxy).injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());
        doReturn(untagResourceResponse).when(proxy).injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());
        doReturn(tagResourceResponse).when(proxy).injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());
        doReturn(putImageTagMutabilityResponse).when(proxy).injectCredentialsAndInvokeV2(any(PutImageTagMutabilityRequest.class), any());
        doReturn(putImageScanningConfigurationResponse).when(proxy).injectCredentialsAndInvokeV2(any(PutImageScanningConfigurationRequest.class), any());

        final LifecyclePolicy lifecyclePolicy = LifecyclePolicy.builder()
                .lifecyclePolicyText("policy")
                .registryId("id")
                .build();
        final Map<String, Object> repositoryPolicy = new HashMap<>();
        repositoryPolicy.put("foo", "bar");

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .lifecyclePolicy(lifecyclePolicy)
                .repositoryPolicyText(repositoryPolicy)
                .tags(newTags)
                .imageTagMutability("IMMUTABLE")
                .imageScanningConfiguration(software.amazon.ecr.repository.ImageScanningConfiguration.builder()
                        .scanOnPush(true)
                        .build())
                .encryptionConfiguration(software.amazon.ecr.repository.EncryptionConfiguration.builder()
                        .encryptionType("AES256")
                        .build())
                .build();

        final ResourceModel previousModel = ResourceModel.builder()
                .repositoryName("repo")
                .encryptionConfiguration(software.amazon.ecr.repository.EncryptionConfiguration.builder()
                        .encryptionType("AES256")
                        .build())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(previousModel)
                .desiredResourceTags(newTagsMap)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_RemoveProperties() {
        doReturn(getRepositoryPolicyResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRepositoryPolicyRequest.class), any());
        doReturn(getLifecyclePolicyResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetLifecyclePolicyRequest.class), any());
        doReturn(deleteRepositoryPolicyResponse).when(proxy).injectCredentialsAndInvokeV2(any(DeleteRepositoryPolicyRequest.class), any());
        doReturn(deleteLifecyclePolicyResponse).when(proxy).injectCredentialsAndInvokeV2(any(DeleteLifecyclePolicyRequest.class), any());
        doReturn(describeRepositoriesResponse).when(proxy).injectCredentialsAndInvokeV2(any(DescribeRepositoriesRequest.class), any());
        doReturn(listTagsForResourceResponse).when(proxy).injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(previousModel)
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_PoliciesAlreadyDeleted() {
        doReturn(describeRepositoriesResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRepositoriesRequest.class), any());
        doThrow(RepositoryPolicyNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(GetRepositoryPolicyRequest.class), any());
        doThrow(LifecyclePolicyNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(GetLifecyclePolicyRequest.class), any());
        doReturn(listTagsForResourceResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_RepoNotFound() {
        doThrow(RepositoryNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    void handleRequest_ChangeEncryptionConfigurations() {
        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .encryptionConfiguration(software.amazon.ecr.repository.EncryptionConfiguration.builder()
                        .encryptionType("KMS")
                        .build())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotUpdatable);
    }

    @Test
    void handleRequest_DeleteEncryptionConfiguration() {
        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .encryptionConfiguration(software.amazon.ecr.repository.EncryptionConfiguration.builder()
                        .encryptionType("KMS")
                        .build())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(previousModel)
                .previousResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotUpdatable);
    }
}
