package software.amazon.ecr.registrypolicy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.ecr.model.DeleteRegistryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyRequest;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.PutRegistryPolicyRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {
  public static final ObjectMapper MAPPER = new ObjectMapper();

  private Translator(){ }

  /**
   * Request to create a PutRegistryPolicyRequest
   * @param model resource model
   * @return PutRegistryPolicyRequest required to create a resource
   */
  static PutRegistryPolicyRequest translateToPutRequest(final ResourceModel model) {
      return PutRegistryPolicyRequest.builder()
              .policyText(translatePolicyInput(model.getPolicyText()))
              .build();
  }

  static String translatePolicyInput(final Object policy) {
    try {
      if (policy instanceof Map){
        return MAPPER.writeValueAsString(policy);
      }
    } catch (JsonProcessingException e ) {
      throw new CfnGeneralServiceException(e);
    }

    return (String)policy;
  }

  /**
   * Translate to request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static GetRegistryPolicyRequest translateToReadRequest(final ResourceModel model) {
    return GetRegistryPolicyRequest.builder().build();
  }

  /**
   * Translates resource response from sdk into a resource model
   * @param response the GetRegistryPolicyResponse
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final GetRegistryPolicyResponse response) {
    return ResourceModel.builder()
            .registryId(response.registryId())
            .policyText(deserializePolicyText(response.policyText()))
            .build();
  }

  /**
   * Translates to request to delete a registry policy
   * @return DeleteRegistryPolicyRequest
   */
  static DeleteRegistryPolicyRequest translateToDeleteRequest() {
    return DeleteRegistryPolicyRequest.builder().build();
  }

  private static Map<String, Object> deserializePolicyText(final String policyText) {
    if (policyText == null) return null;
    try {
      return Translator.MAPPER.readValue(policyText, new TypeReference<HashMap<String,Object>>() {});
    } catch (final IOException e) {
      throw new CfnInternalFailureException(e);
    }
  }
}
