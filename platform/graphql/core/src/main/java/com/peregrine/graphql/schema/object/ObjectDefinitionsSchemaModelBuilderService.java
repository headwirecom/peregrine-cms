package com.peregrine.graphql.schema.object;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.graphql.schema.SchemaModelBuilder;
import com.peregrine.graphql.schema.model.EnumModel;
import com.peregrine.graphql.schema.model.ScalarEnum;
import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeFieldModel;
import com.peregrine.graphql.schema.model.TypeModel;
import com.peregrine.graphql.schema.model.TypeModelTypeImpl;
import com.peregrine.graphql.schema.model.TypeModelTypeImpl.QueryImpl;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.OBJECT_DEFINITIONS;
import static com.peregrine.graphql.schema.model.TypeModelTypeImpl.SUB_COMPONENT_TYPE;
import static com.peregrine.graphql.schema.object.JSonFormConstants.JSON_ENUMS;
import static com.peregrine.graphql.schema.object.JSonFormConstants.JSON_FORMAT_PROPERTY;
import static com.peregrine.graphql.schema.object.JSonFormConstants.JSON_OBJECT_TYPE;
import static com.peregrine.graphql.schema.object.JSonFormConstants.JSON_PROPERTIES;
import static com.peregrine.graphql.schema.object.JSonFormConstants.JSON_REQUIRED;
import static com.peregrine.graphql.schema.object.JSonFormConstants.JSON_SCHEMA_NODE_NAME;
import static com.peregrine.graphql.schema.object.JSonFormConstants.JSON_TYPE;
import static com.peregrine.graphql.schema.util.Utils.PATH_SYSTEM_VARIABLE;
import static com.peregrine.graphql.schema.util.Utils.getChildResources;
import static com.peregrine.graphql.schema.util.Utils.getSchemaContent;

@Component(
    service = SchemaModelBuilder.class,
    immediate = true
)
public class ObjectDefinitionsSchemaModelBuilderService
    implements SchemaModelBuilder
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final TypeModelTypeImpl objectTypeModelType = new TypeModelTypeImpl()
        .withQueries(
            new QueryImpl(true, "ObjectList", "List all Objects of that Type"),
            new QueryImpl(false, "ObjectByPath", "List an Object of that Type by the given Path")
                .withArguments(PATH_SYSTEM_VARIABLE)
        )
        .withSystemVariables(PATH_SYSTEM_VARIABLE);

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public void buildFromTenant(Resource tenant, SchemaModel schemaModel) {
        Resource objectDefinitionsResource = tenant.getChild(OBJECT_DEFINITIONS);
        if(objectDefinitionsResource == null || ResourceUtil.isNonExistingResource(objectDefinitionsResource)) {
            logger.warn("Given Resource does not yield a child '{}': {}", OBJECT_DEFINITIONS, tenant);
        } else {
            List<Resource> schemaResources = new ArrayList<>();
            getChildResources(objectDefinitionsResource, JSON_SCHEMA_NODE_NAME, schemaResources);
            logger.info("Found Schema Resources: {}", schemaResources);
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
                    String type = jsonSchema.get(JSON_TYPE).asText();
                    if (JSON_OBJECT_TYPE.equals(type)) {
                        TypeModel typeModel = new TypeModel(objectTypeModelType, name, path);
                        JsonNode properties = jsonSchema.get(JSON_PROPERTIES);
                        if (properties != null && properties.isObject()) {
                            Iterator<String> keys = properties.fieldNames();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JsonNode item = properties.get(key);
                                if (item != null && item.isObject()) {
                                    String itemType = item.get(JSON_TYPE).asText();
                                    JSonFormScalar e = JSonFormScalar.getEnum(itemType);
                                    TypeFieldModel fieldModel = new TypeFieldModel().setName(key);
                                    switch (e) {
                                        case String:
                                            if (item.has(JSON_ENUMS)) {
                                                // Handle Enums
                                                EnumModel enumModel = new EnumModel(SUB_COMPONENT_TYPE, key);
                                                JsonNode temp = item.get(key);
                                                schemaModel.addEnum(enumModel);
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
                                                if (item.has(JSON_FORMAT_PROPERTY)) {
                                                    //AS TODO: Handle Formatting

                                                }
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
                        JsonNode required = jsonSchema.get(JSON_REQUIRED);
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
                        schemaModel.addType(typeModel);
                    }
                } catch (IOException e) {
                    logger.error("Failed to parse Schema: {}", schemaResource.getPath(), e);
                }
            }
        }
    }
}
