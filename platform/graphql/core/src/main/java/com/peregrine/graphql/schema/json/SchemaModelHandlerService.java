package com.peregrine.graphql.schema.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.commons.ResourceUtils;
import com.peregrine.graphql.schema.model.EnumModel;
import com.peregrine.graphql.schema.model.ScalarEnum;
import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeFieldModel;
import com.peregrine.graphql.schema.model.TypeModel;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.OBJECT_DEFINITIONS;

@Component(
    service = SchemaModelHandler.class,
    immediate = true
)
public class SchemaModelHandlerService
    implements SchemaModelHandler
{

    private static final String JSON_SCHEMA_NODE_NAME = "schema.json";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SchemaModel convert(Resource tenantResource) {
        SchemaModel answer = null;
        List<Resource> schemaResources = new ArrayList<>();
        if(tenantResource == null || ResourceUtil.isNonExistingResource(tenantResource)) {
            logger.warn("Called with a null or non-existing resource: {}", tenantResource);
        } else {
            Resource objectDefinitionsResource = tenantResource.getChild(OBJECT_DEFINITIONS);
            if(objectDefinitionsResource == null || ResourceUtil.isNonExistingResource(objectDefinitionsResource)) {
                logger.warn("Given Resource does not yield a child '{}': {}", OBJECT_DEFINITIONS, tenantResource);
            } else {
                getChildResources(objectDefinitionsResource, JSON_SCHEMA_NODE_NAME, schemaResources);
                logger.info("Found Schema Resources: {}", schemaResources);
                answer = new SchemaModel();
                for (Resource schemaResource : schemaResources) {
                    try {
                        InputStream inputStream = getSchemaContent(schemaResource);
                        JsonNode jsonSchema = new ObjectMapper().readTree(inputStream);
                        Resource schemaParentResource = schemaResource.getParent();
                        if (schemaParentResource.getName().equals(JSON_SCHEMA_NODE_NAME)) {
                            schemaParentResource = schemaResource.getParent();
                        }
                        String name = schemaParentResource.getName();
                        String path = schemaParentResource.getPath();
                        String type = jsonSchema.get("type").asText();
                        if ("object".equals(type)) {
                            TypeModel typeModel = new TypeModel(name, path);
                            JsonNode properties = jsonSchema.get("properties");
                            if (properties != null && properties.isObject()) {
                                Iterator<String> keys = properties.fieldNames();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    JsonNode item = properties.get(key);
                                    if (item != null && item.isObject()) {
                                        String itemType = item.get("type").asText();
                                        JSonFormScalar e = JSonFormScalar.getEnum(itemType);
                                        TypeFieldModel fieldModel = new TypeFieldModel().setName(key);
                                        switch (e) {
                                            case String:
                                                if (item.has("enum")) {
                                                    // Handle Enums
                                                    EnumModel enumModel = new EnumModel(key);
                                                    JsonNode temp = item.get(key);
                                                    answer.addEnum(enumModel);
                                                    if (temp.isArray()) {
                                                        Iterator<JsonNode> enumItems = temp.iterator();
                                                        while (enumItems.hasNext()) {
                                                            JsonNode enumItem = enumItems.next();
                                                            enumModel.addEnumeration(enumItem.asText());
                                                        }
                                                        fieldModel = null;
                                                    } else {
                                                        throw new IllegalArgumentException("Enumeration is not an Array");
                                                    }
                                                } else {
                                                    fieldModel.setType(ScalarEnum.String);
                                                    //AS TODO: Handle Formatting
                                                }
                                                break;
                                            case Boolean:
                                                fieldModel.setType(ScalarEnum.Boolean);
                                                break;
                                            case Number:
                                            case Integer:
                                                fieldModel.setType(ScalarEnum.Int);
                                                break;
                                        }
                                        if (fieldModel != null) {
                                            typeModel.addField(fieldModel);
                                        }
                                    }
                                }
                            }
                            JsonNode required = jsonSchema.get("required");
                            if (required != null && required.isArray()) {
                                Iterator<JsonNode> requiredFields = required.elements();
                                while (requiredFields.hasNext()) {
                                    String fieldName = requiredFields.next().asText();
                                    for (TypeFieldModel field : typeModel.getFields()) {
                                        if (field.getName().equals(fieldName)) {
                                            field.setNotNull(true);
                                        }
                                    }
                                }
                            }
                            answer.addType(typeModel);
                        }
                    } catch (IOException e) {
                        logger.error("Failed to parse Schema: {}", schemaResource.getPath(), e);
                    }
                }
            }
        }
        return answer;
    }

    private void getChildResources(Resource resource, String fileName, List<Resource> result) {
        String resourceName = resource.getName();
        if(resourceName != null && resourceName.equals(fileName)) {
            result.add(resource);
        }
        for(Resource child: resource.getChildren()) {
            getChildResources(child, fileName, result);
        }
    }

    private InputStream getSchemaContent(Resource resource) {
        Resource jcrContent = resource != null ?
            resource.getName().equals(JCR_CONTENT) ?
                resource :
                resource.getChild(JCR_CONTENT) :
            null;
        if(jcrContent != null) {
            ValueMap properties = jcrContent.getValueMap();
            return properties.get(JCR_DATA, InputStream.class);
        } else {
            return null;
        }
    }
}
