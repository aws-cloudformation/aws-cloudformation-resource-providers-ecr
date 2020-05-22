package software.amazon.ecr.repository;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DeleteLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.DeleteLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.DeleteRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.DeleteRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
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
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EcrClient> proxyEcrClient;

    @Mock
    EcrClient ecr;

    private UpdateHandler handler;


    private Repository repo = Repository.builder()
            .repositoryName("repo")
            .registryId("id")
            .repositoryArn("arn")
            .imageTagMutability("IMMUTABLE")
            .imageScanningConfiguration(ImageScanningConfiguration.builder()
                    .scanOnPush(true)
                    .build())
            .build();

    private DescribeRepositoriesResponse describeRepositoriesResponse = DescribeRepositoriesResponse.builder()
            .repositories(Collections.singletonList(repo))
            .build();

    private ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
            .tags(Collections.emptyList())
            .build();

    private SetRepositoryPolicyResponse setRepositoryPolicyResponse = SetRepositoryPolicyResponse.builder().build();
    private PutLifecyclePolicyResponse putLifecyclePolicyResponse = PutLifecyclePolicyResponse.builder().build();

    private DeleteRepositoryPolicyResponse deleteRepositoryPolicyResponse = DeleteRepositoryPolicyResponse.builder().build();
    private DeleteLifecyclePolicyResponse deleteLifecyclePolicyResponse = DeleteLifecyclePolicyResponse.builder().build();

    private UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
    private TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();

    private PutImageTagMutabilityResponse putImageTagMutabilityResponse = PutImageTagMutabilityResponse.builder().build();
    private PutImageScanningConfigurationResponse putImageScanningConfigurationResponse = PutImageScanningConfigurationResponse.builder().build();


    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
        ecr = mock(EcrClient.class);
        proxy = mock(AmazonWebServicesClientProxy.class);
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
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
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(newTagsMap)
                .build();

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
    public void handleRequest_RemoveProperties() {
        doReturn(deleteRepositoryPolicyResponse).when(proxy).injectCredentialsAndInvokeV2(any(DeleteRepositoryPolicyRequest.class), any());
        doReturn(deleteLifecyclePolicyResponse).when(proxy).injectCredentialsAndInvokeV2(any(DeleteLifecyclePolicyRequest.class), any());
        doReturn(describeRepositoriesResponse).when(proxy).injectCredentialsAndInvokeV2(any(DescribeRepositoriesRequest.class), any());
        doReturn(listTagsForResourceResponse).when(proxy).injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());
        doReturn(putImageTagMutabilityResponse).when(proxy).injectCredentialsAndInvokeV2(any(PutImageTagMutabilityRequest.class), any());
        doReturn(putImageScanningConfigurationResponse).when(proxy).injectCredentialsAndInvokeV2(any(PutImageScanningConfigurationRequest.class), any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

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
    public void handleRequest_PoliciesAlreadyDeleted() {
        doReturn(describeRepositoriesResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DescribeRepositoriesRequest.class), any());
        doThrow(RepositoryPolicyNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DeleteRepositoryPolicyRequest.class), any());
        doThrow(LifecyclePolicyNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(DeleteLifecyclePolicyRequest.class), any());
        doReturn(listTagsForResourceResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());
        doReturn(putImageTagMutabilityResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(PutImageTagMutabilityRequest.class), any());
        doReturn(putImageScanningConfigurationResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(PutImageScanningConfigurationRequest.class), any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

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
    public void handleRequest_RepoNotFound() {
        doThrow(RepositoryNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger));
    }
}
