package com.peregrine.jsonschema.specification;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jsonschema2pojo.ContentResolver;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.removeStart;

public class PerSchemaContentResolver
    extends ContentResolver
{
    private static final Set<String> PEREGRINE_SCHEMES = new HashSet<>(asList("per"));

    private final ResourceResolver resourceResolver;
    private final ObjectMapper objectMapper;
    private final List<String> references;

    public PerSchemaContentResolver(ResourceResolver resourceResolver, List<String> references) {
        this(resourceResolver, references, null);
    }

    public PerSchemaContentResolver(ResourceResolver resourceResolver, List<String> references, JsonFactory jsonFactory) {
        super(jsonFactory);
        this.resourceResolver = resourceResolver;
        this.references = references;
        this.objectMapper = new ObjectMapper(jsonFactory)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

    public JsonNode resolve(URI uri) {

        if (PEREGRINE_SCHEMES.contains(uri.getScheme())) {
            return resolveFromJCR(uri, true);
        }
        if(uri.toString().startsWith("/")) {
            return resolveFromJCR(uri, false);
        }
        return super.resolve(uri);
    }

    private JsonNode resolveFromJCR(URI uri, boolean perScheme) {
        String path;
        if(perScheme) {
            path = removeStart(removeStart(uri.toString(), uri.getScheme() + ":"), "/");
            path = path.startsWith("/") ? path.substring(1) : path;
            String siteName = path.substring(0, path.indexOf('/'));
            String objectDefinitionsName = path.substring(path.indexOf('/') + 1);
            path = "/content/" + siteName + "/object-definitions/" + objectDefinitionsName + "/schema.json";
        } else {
            path = uri.toString();
        }
        Resource resource = resourceResolver.getResource(path);
        if(resource == null) {
            throw new IllegalArgumentException("Couldn't read content from the JCR, node not found: " + path);
        }
        references.add(resource.getPath());
        Resource contentResource = resource.getChild(JCR_CONTENT);
        if(contentResource == null) {
            throw new IllegalArgumentException("Couldn't find jcr:content child of node: " + path);
        }
        ValueMap properties = contentResource.getValueMap();
        String content = properties.get(JCR_DATA, String.class);
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error parsing document: " + uri, e);
        }
    }
}
