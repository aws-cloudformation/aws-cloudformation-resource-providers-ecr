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
import software.amazon.awssdk.services.ecr.model.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ReadHandler handler;

    private Repository repository = Repository.builder()
            .repositoryName("repo")
            .registryId("id")
            .repositoryArn("arn")
            .build();

    private DescribeRepositoriesResponse describeRepositoriesResponse = DescribeRepositoriesResponse.builder()
            .repositories(Collections.singletonList(repository))
            .build();

    private GetLifecyclePolicyResponse getLifecyclePolicyResponse = GetLifecyclePolicyResponse.builder()
            .repositoryName("repo")
            .lifecyclePolicyText("policy")
            .registryId("id")
            .build();

    private GetRepositoryPolicyResponse getRepositoryPolicyResponse = GetRepositoryPolicyResponse.builder()
            .repositoryName("repo")
            .policyText("{\"foo\": \"bar\"}")
            .registryId("id")
            .build();

    private ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
            .tags(Collections.singletonList(Tag.builder().key("key").value("value").build()))
            .build();

    @BeforeEach
    public void setup() {
         handler = new ReadHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
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

        final Set<Tags> tags = Collections.singleton(Tags.builder().key("key").value("value").build());

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
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

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
    public void handleRequest_PoliciesNotFound() {
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

        final Set<Tags> tags = Collections.singleton(Tags.builder().key("key").value("value").build());

        final ResourceModel expectedModel = ResourceModel.builder()
                .repositoryName("repo")
                .repositoryPolicyText(null)
                .lifecyclePolicy(null)
                .tags(tags)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

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
    public void handleRequest_RepoNotFound() {
        doThrow(RepositoryNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }
}
