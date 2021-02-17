package software.amazon.ecr.repository;

import java.util.Map;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationTest {

    @Test
    void tagConfigurationWithKeyCollision_Success() {
        final ResourceModel model = ResourceModel.builder()
                .tags(ImmutableSet.of(new Tag("key1", "value0"), new Tag("key1", "value1"), new Tag("key2", "value2")))
                .build();
        final Configuration configuration = new Configuration();

        final Map<String, String> tags = configuration.resourceDefinedTags(model);
        assertEquals(ImmutableMap.of("key1", "value1", "key2", "value2"), tags);
    }
}
