package com.peregrine.jsonschema.specification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

public class SchemaParser {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Schema parseSchema(String jsonSchemaContent) throws SchemaException {
        SchemaImpl answer;
        try {
            JsonNode schemaNode = objectMapper.readTree(jsonSchemaContent);
            JsonNode field = schemaNode.findValue("$schema");
            if(field == null) {
                throw new SchemaException("Schema is invalid, no '$schema' property set");
            }
            String version = field.asText();
            field = schemaNode.findValue("$id");
            if(field == null) {
                throw new SchemaException("Schema is invalid, no '$id' property set");
            }
            String id = field.asText();
            answer = new SchemaImpl(version, id);
            // Handle Properties
            JsonNode properties = schemaNode.findValue("properties");
            if (properties != null) {
                Iterator<Entry<String, JsonNode>> fields = properties.fields();
                while (fields.hasNext()) {
                    Entry<String, JsonNode> entry = fields.next();
                    JsonNode type = entry.getValue().findValue("type");
                    answer.addProperty(
                        new PropertyImpl(entry.getKey(), type.asText())
                    );
                }
                // Handle Required
                JsonNode required = schemaNode.findValue("required");
                if (required != null) {
                    Iterator<JsonNode> requiredChildren = required.elements();
                    while (requiredChildren.hasNext()) {
                        String requiredField = requiredChildren.next().asText();
                        PropertyImpl property = (PropertyImpl) answer.getProperty(requiredField);
                        if (property != null) {
                            property.setRequired(true);
                        }
                    }
                }
                // Handle Required
                JsonNode dependencies = schemaNode.findValue("dependencies");
                if (dependencies != null) {
                    fields = dependencies.fields();
                    while (fields.hasNext()) {
                        Entry<String, JsonNode> entry = fields.next();
                        String source = entry.getKey();
                        PropertyImpl property = (PropertyImpl) answer.getProperty(source);
                        if (property != null) {
                            JsonNode target = entry.getValue();
                            Iterator<JsonNode> dependants = target.elements();
                            while (dependants.hasNext()) {
                                String dependent = dependants.next().asText();
                                PropertyImpl targetProperty = (PropertyImpl) answer.getProperty(dependent);
                                if (targetProperty != null) {
                                    property.addDependency(targetProperty);
                                }
                            }
                        }
                    }
                }
            }
        } catch(IOException e) {
            throw new SchemaException("Schema is not a valid JSon content", e);
        }
        return answer;
    }

    public static class SchemaException extends Exception {
        public SchemaException(String message) {
            super(message);
        }

        public SchemaException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
