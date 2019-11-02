package com.amazonaws.ecr.repository;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
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
import software.amazon.awssdk.services.ecr.model.DeleteLifecyclePolicyRequest;
import software.amazon.awssdk.services.ecr.model.DeleteLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.DeleteRepositoryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.DeleteRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.LifecyclePolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ecr.model.PutLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.SetRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.Tag;
import software.amazon.awssdk.services.ecr.model.TagResourceResponse;
import software.amazon.awssdk.services.ecr.model.UntagResourceResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private UpdateHandler handler;

    private Set<Tags> tags = Collections.singleton(Tags.builder().key("newKey").value("newVal").build());

    private Repository repo = Repository.builder()
            .repositoryName("repo")
            .registryId("id")
            .repositoryArn("arn")
            .build();

    private DescribeRepositoriesResponse describeRepositoriesResponse = DescribeRepositoriesResponse.builder()
            .repositories(Collections.singletonList(repo))
            .build();

    private ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
            .tags(Collections.emptyList())
            .build();

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final SetRepositoryPolicyResponse setRepositoryPolicyResponse = SetRepositoryPolicyResponse.builder().build();
        final PutLifecyclePolicyResponse putLifecyclePolicyResponse = PutLifecyclePolicyResponse.builder().build();
        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
                .tags(Collections.singletonList(Tag.builder().key("key").value("val").build()))
                .build();

        doReturn(setRepositoryPolicyResponse,
                putLifecyclePolicyResponse,
                describeRepositoriesResponse,
                listTagsForResourceResponse,
                untagResourceResponse,
                tagResourceResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

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
                .tags(tags)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

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
        final DeleteRepositoryPolicyResponse deleteRepositoryPolicyResponse = DeleteRepositoryPolicyResponse.builder().build();
        final DeleteLifecyclePolicyResponse deleteLifecyclePolicyResponse = DeleteLifecyclePolicyResponse.builder().build();

        doReturn(deleteRepositoryPolicyResponse,
                deleteLifecyclePolicyResponse,
                describeRepositoriesResponse,
                listTagsForResourceResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

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

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

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
                () -> handler.handleRequest(proxy, request, null, logger));
    }
}
