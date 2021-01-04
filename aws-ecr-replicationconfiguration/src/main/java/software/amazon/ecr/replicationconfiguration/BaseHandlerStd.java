package software.amazon.ecr.replicationconfiguration;

import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRegistryResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers
public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<EcrClient> proxyClient,
            final Logger logger);

    protected boolean hasExistingResource(DescribeRegistryResponse response) {
        // Check if any rules exist on the ECR Registry
        return response.replicationConfiguration() != null
                && CollectionUtils.isNotEmpty(response.replicationConfiguration().rules());
    }

    protected DescribeRegistryResponse describeRegistryResource(AmazonWebServicesClientProxy proxy, EcrClient client) {
        return proxy.injectCredentialsAndInvokeV2(
                DescribeRegistryRequest.builder().build(),
                client::describeRegistry
        );
    }
}
