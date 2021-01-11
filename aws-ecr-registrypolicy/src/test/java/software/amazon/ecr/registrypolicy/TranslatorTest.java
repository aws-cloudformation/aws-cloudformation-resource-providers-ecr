package software.amazon.ecr.registrypolicy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.services.ecr.model.GetRegistryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.PutRegistryPolicyRequest;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TranslatorTest extends AbstractTestBase {

    @Test
    void translateToPutRequest_success() {
        ResourceModel model = ResourceModel.builder()
                .policyText(REGISTRY_POLICY_INPUT_TEXT)
                .registryId(TEST_REGISTRY_ID)
                .build();
        PutRegistryPolicyRequest translatedRequest = Translator.translateToPutRequest(model);
        assertThat(translatedRequest.policyText()).isEqualTo(model.getPolicyText());
    }

    @Test
    void translatePolicyInput_Map() throws JsonProcessingException {
        HashMap map = new ObjectMapper().readValue(REGISTRY_POLICY_INPUT_TEXT, HashMap.class);
        String policy  = Translator.translatePolicyInput(map);

        assertThat(policy).isEqualTo(REGISTRY_POLICY_INPUT_TEXT);
    }

    @Test
    void translatePolicyInput_String() {
        String policy  = Translator.translatePolicyInput(REGISTRY_POLICY_INPUT_TEXT);
        assertThat(policy).isEqualTo(REGISTRY_POLICY_INPUT_TEXT);
    }

    @Test
    void deserializePolicyText_NullText()  {
        GetRegistryPolicyResponse response = GetRegistryPolicyResponse.builder()
                .registryId("1234")
                .build();

        ResourceModel model = Translator.translateFromReadResponse(response);
        assertThat(model).isNotNull();
        assertThat(model.getRegistryId()).isEqualTo("1234");
        assertThat(model.getPolicyText()).isNull();
    }
}
