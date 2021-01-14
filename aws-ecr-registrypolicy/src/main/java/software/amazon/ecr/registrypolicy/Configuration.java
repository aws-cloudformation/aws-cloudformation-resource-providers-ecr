package software.amazon.ecr.registrypolicy;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Objects;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ecr-registrypolicy.json");
    }

    @Override
    public JSONObject resourceSchemaJSONObject() {
        return new JSONObject(new JSONTokener(
                Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(schemaFilename))));
    }
}
