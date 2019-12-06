package software.amazon.ecr.repository;

import com.google.common.collect.ImmutableSet;
import software.amazon.awssdk.services.ecr.model.EcrException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private CreateHandler handler;

    private Repository repo = Repository.builder()
            .repositoryName("repo")
            .registryId("id")
            .repositoryArn("arn")
            .build();

    private CreateRepositoryResponse createRepositoryResponse = CreateRepositoryResponse.builder()
            .repository(repo)
            .build();

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
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
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(tagsMap)
                .build();

        doReturn(createRepositoryResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateRepositoryRequest.class), any());

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
    public void handleRequest_AutogenerateName() {
        final ResourceModel model = ResourceModel.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .logicalResourceIdentifier("MyResource")
                .clientRequestToken("token")
                .desiredResourceState(model)
                .build();

        doReturn(createRepositoryResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateRepositoryRequest.class), any());

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
        assertThat(response.getResourceModel().getRepositoryName()).isNotNull();
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
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_StackTagsPermissionError() {
        doThrow(EcrException.builder().message("not authorized to perform: ecr:TagResource").build())
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
    public void handleRequest_ResourceTagsPermissionError() {
        doThrow(EcrException.builder().message("not authorized to perform: ecr:TagResource").build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final Set<Tag> tags = ImmutableSet.of(Tag.builder().key("key1").value("val1").build());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .tags(tags)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_EcrException() {
        doThrow(EcrException.builder().message("general exception").build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }
}
