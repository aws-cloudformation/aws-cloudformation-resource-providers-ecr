package software.amazon.ecr.repository;

import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.ImageScanningConfiguration;
import software.amazon.awssdk.services.ecr.model.ImageTagMutability;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EcrClient> proxyEcrClient;

    @Mock
    EcrClient ecr;

    private ListHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ListHandler();
        proxyEcrClient = MOCK_PROXY(proxy, ecr);
    }

    @Test
    void handleRequest_SimpleSuccess() {
        final Repository repository = Repository.builder()
                .repositoryName("repo")
                .registryId("id")
                .repositoryArn("arn")
                .imageScanningConfiguration(ImageScanningConfiguration.builder().scanOnPush(false).build())
                .imageTagMutability(ImageTagMutability.IMMUTABLE)
                .build();

        final DescribeRepositoriesResponse describeRepositoriesResponse = DescribeRepositoriesResponse.builder()
                .repositories(Collections.singletonList(repository))
                .nextToken("newToken")
                .build();

        final GetLifecyclePolicyResponse getLifecyclePolicyResponse = GetLifecyclePolicyResponse.builder()
                .repositoryName("repo")
                .lifecyclePolicyText("policy")
                .registryId("id")
                .build();

        final GetRepositoryPolicyResponse getRepositoryPolicyResponse = GetRepositoryPolicyResponse.builder()
                .repositoryName("repo")
                .policyText("{\"foo\": \"bar\"}")
                .registryId("id")
                .build();

        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
                .tags(Collections.singletonList(Tag.builder().key("key").value("value").build()))
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

        final Set<software.amazon.ecr.repository.Tag> tags = Collections.singleton(software.amazon.ecr.repository.Tag.builder().key("key").value("value").build());

        final Map<String, Object> policyObject = new HashMap<>();
        policyObject.put("foo", "bar");

        final ResourceModel expectedModel = ResourceModel.builder()
                .repositoryName("repo")
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyEcrClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).containsExactly(expectedModel);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo("newToken");
    }
}
