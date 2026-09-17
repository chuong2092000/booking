package project.commonutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseJsonHelper {
    private final ObjectMapper objectMapper;

    public BaseJsonHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode objectToJsonNode(Object obj) {
        if (obj == null) {
            return null;
        }
        return objectMapper.valueToTree(obj);
    }

    public JsonNode stringToJsonNode(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error parse JSON to JsonNode", e);
        }
    }

    public String jsonNodeToString(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return jsonNode.toString();
    }
}
