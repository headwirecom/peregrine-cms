package com.peregrine.jsonschema.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.jsonschema.specification.Schema;
import com.peregrine.jsonschema.specification.SchemaLoaderService;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.JSON_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;


@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "JSon Schema Provider Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "perapi/public/schema"
    }
)
public class SchemaProviderServlet extends AbstractBaseServlet {

    private static final String PROPERTIES_FIELD_NAME = "properties";
    private static final String REQUIRED_FIELD_NAME = "required";
    private static final String REFERENCE_FIELD_NAME = "$ref";

    @Reference
    private SchemaLoaderService schemaLoaderService;

    @Override
    protected Response handleRequest(Request request) throws IOException, ServletException {
        Response answer;
        String path = request.getSuffix();
        if(path == null || path.isEmpty()) {
            // Fallback if the Servlet is called by the source directly
            path = request.getRequestPath();
        }
        Schema schema = schemaLoaderService.getSchema(request.getResourceResolver(), path);

        if(schema == null) {
            answer = new ErrorResponse().setErrorMessage("Failed to obtain Schema from Path").setRequestPath(path);
        } else {
            String source = schema.getSource();
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode node = mapper.readTree(source);
                if (node.has(PROPERTIES_FIELD_NAME)) {
                    JsonNode properties = node.get(PROPERTIES_FIELD_NAME);
                    // Get the Schema, get the content, parse it, go through properties and find references
                    // load references content, parse it, obtain properties, add to parent, add required to required section
                    // rinse and repeat
                    // Serialize JSon into string answer
                    handleProperties(request.getResourceResolver(), mapper, (ObjectNode) node, (ObjectNode) properties);
                }
                answer = new TextResponse(JSON, JSON_MIME_TYPE).write(mapper.writeValueAsString(node));
            } catch(JsonProcessingException e) {
                answer = new ErrorResponse().setErrorMessage("Failed to parse Schema").setRequestPath(path).setException(e);
            }
        }
        return answer;
    }

    private void handleProperties(
        ResourceResolver resourceResolver, ObjectMapper mapper, ObjectNode root, ObjectNode properties
    ) throws JsonProcessingException {
        // Loop over all properties entries and look for a reference in that entry's properties
        Iterator<Entry<String, JsonNode>> it = properties.fields();
        while(it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            JsonNode property = entry.getValue();
            if(property.has(REFERENCE_FIELD_NAME)) {
                JsonNode ref = property.get(REFERENCE_FIELD_NAME);
                String reference = ref.textValue();
                if(reference != null && !reference.isEmpty()) {
                    // Load the Schema
                    Schema schema = schemaLoaderService.getSchema(resourceResolver, reference);
                    if(schema != null) {
                        properties.remove(entry.getKey());
                        // Now add the properties from the referenced schema
                        JsonNode refRoot = mapper.readTree(schema.getSource());
                        mergeSchemas(resourceResolver, mapper, root, refRoot);
                    }
                }
            }
        }
    }

    private void mergeSchemas(ResourceResolver resourceResolver, ObjectMapper mapper, ObjectNode root, JsonNode reference) throws JsonProcessingException {
        boolean containsRef = false;
        if(reference.has(PROPERTIES_FIELD_NAME)) {
            if(!root.has(PROPERTIES_FIELD_NAME)) {
                root.putObject(PROPERTIES_FIELD_NAME);
            }
            containsRef = mergeProperties((ObjectNode) root.get(PROPERTIES_FIELD_NAME), reference.get(PROPERTIES_FIELD_NAME));
        }
        if(reference.has(REQUIRED_FIELD_NAME)) {
            if(!root.has(REQUIRED_FIELD_NAME)) {
                root.putArray(REQUIRED_FIELD_NAME);
            }
            mergeRequired((ArrayNode) root.get(REQUIRED_FIELD_NAME), reference.get(REQUIRED_FIELD_NAME));
        }
        if(containsRef) {
            handleProperties(resourceResolver, mapper, root, (ObjectNode) reference.get(PROPERTIES_FIELD_NAME));
        }
    }

    private boolean mergeProperties(ObjectNode rootProperties, JsonNode referenceProperties) {
        boolean answer = false;
        Iterator<Entry<String, JsonNode>> it = referenceProperties.fields();
        while (it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            JsonNode value = entry.getValue();
            if(value.has(REFERENCE_FIELD_NAME)) {
                answer = true;
            }
            rootProperties.putIfAbsent(entry.getKey(), value);
        }
        return answer;
    }

    private void mergeRequired(ArrayNode rootRequired, JsonNode referenceRequired) {
        rootRequired.addAll((ArrayNode) referenceRequired);
    }
}
