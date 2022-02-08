package com.peregrine.graphql.schema.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.graphql.schema.SchemaModelBuilder;
import com.peregrine.graphql.schema.component.DialogJsonConstants.INPUT_TYPE;
import com.peregrine.graphql.schema.component.DialogJsonConstants.TYPE;
import com.peregrine.graphql.schema.model.ScalarEnum;
import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeFieldModel;
import com.peregrine.graphql.schema.model.TypeModel;
import com.peregrine.graphql.schema.model.TypeModelTypeImpl;
import com.peregrine.graphql.schema.model.TypeModelTypeImpl.QueryImpl;
import com.peregrine.graphql.schema.util.Utils;
import com.peregrine.graphql.schema.util.Utils.VariableImpl;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_SUPER_TYPE;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.DIALOG_CONTENT_VALUE;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.DIALOG_FIELDS_PROPERTY;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.DIALOG_FIELD_INPUT_TYPE;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.DIALOG_FIELD_MODEL;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.DIALOG_FIELD_TYPE;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.DIALOG_GROUPS_PROPERTY;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.DIALOG_LEGEND_PROPERTY;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.DIALOG_NODE_NAME;
import static com.peregrine.graphql.schema.model.TypeModelTypeImpl.SUB_COMPONENT_TYPE;
import static com.peregrine.graphql.schema.util.Utils.PAGE_PATH_SYSTEM_VARIABLE;
import static com.peregrine.graphql.schema.util.Utils.PATH_SYSTEM_VARIABLE;
import static com.peregrine.graphql.schema.util.Utils.getSchemaContent;

@Component(
    service = SchemaModelBuilder.class,
    immediate = true
)
public class PageSchemaModelBuilderService
    implements SchemaModelBuilder
{
    private static final List<String> PAGES_PRIMARY_TYPE = new ArrayList<>(Arrays.asList("per:Page", "per:Asset", "per:Object", "per:ObjectDefinition"));

    public static final TypeModelTypeImpl pageTypeModelType = new TypeModelTypeImpl()
        .withQueries(
            new QueryImpl(true, "ComponentList", "List all Components with that Type"),
            new QueryImpl(true, "ComponentByPath", "List all Components of a Page with that Path")
                .withArguments(
                    new VariableImpl(PATH_FIELD_NAME, ScalarEnum.String, true)
                ),
            new QueryImpl(true, "ComponentByField", "List all Components by Field Name and Value as well as Page Path")
                .withArguments(
                    new VariableImpl(PATH_FIELD_NAME, ScalarEnum.String, false),
                    new VariableImpl("fieldName", ScalarEnum.String, true),
                    new VariableImpl("fieldValue", ScalarEnum.String, false)
                )
        )
        .withSystemVariables(PATH_SYSTEM_VARIABLE, PAGE_PATH_SYSTEM_VARIABLE);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private WeakHashMap<String, TypeModel> typeModelMap = new WeakHashMap<>();

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void build(Resource tenant, SchemaModel schemaModel) {
        String appsComponentsFolderPath = "/apps/" + tenant.getName() + "/components";
        Resource tenantAppsComponentsFolder = tenant.getResourceResolver()
            .getResource(appsComponentsFolderPath);
        if(tenantAppsComponentsFolder == null || ResourceUtil.isNonExistingResource(tenantAppsComponentsFolder)) {
            logger.warn("Tenant '{}' does not have an Apps Components Folder: '{}'", tenant, appsComponentsFolderPath);
        } else {
            List<Resource> dialogResources = new ArrayList<>();
            getDialogResources(tenantAppsComponentsFolder, dialogResources);
            // Sort Dialogs to make the types appear alphabetically
            dialogResources = dialogResources.stream()
                .sorted(Comparator.comparing(Resource::getPath))
                .collect(Collectors.toList());
            logger.info("Found Schema Resources: {}", dialogResources);
            for (Resource dialogResource : dialogResources) {
                try {
                    TypeModel componentTypeModel = typeModelMap.get(dialogResource.getParent().getPath());
                    if(componentTypeModel == null) {
                        InputStream inputStream = getSchemaContent(dialogResource);
                        JsonNode jsonSchema = new ObjectMapper().readTree(inputStream);
                        JsonNode groups = jsonSchema.get(DIALOG_GROUPS_PROPERTY);
                        if(groups.isArray()) {
                            Iterator<JsonNode> i = groups.elements();
                            while(i.hasNext()) {
                                JsonNode group = i.next();
                                if(group.has(DIALOG_LEGEND_PROPERTY)) {
                                    JsonNode legend = group.get(DIALOG_LEGEND_PROPERTY);
                                    if(DIALOG_CONTENT_VALUE.equals(legend.asText()) && group.has(DIALOG_FIELDS_PROPERTY)) {
                                        JsonNode fields = group.get(DIALOG_FIELDS_PROPERTY);
                                        if(fields.isArray() && fields.elements().hasNext()) {
                                            componentTypeModel = parseFields(dialogResource, fields, schemaModel);
                                            if(componentTypeModel != null) {
                                                typeModelMap.put(dialogResource.getParent().getPath(), componentTypeModel);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(componentTypeModel != null) {
                        if(schemaModel.getTypeByName(componentTypeModel.getName()) == null) {
                            schemaModel.addType(componentTypeModel);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Failed to parse Schema: {}", dialogResource.getPath(), e);
                }
            }
        }
    }

    /**
     * Go through all Fields in the dialog.json item, parse them into a type field and add them to the type
     * @param dialogResource Dialog Resource to obtain its name and path
     * @param fields Json Node of fields
     * @param schemaModel Schema Model to add enumerations if needed
     * @return Type Model for the fields of the component
     */
    private TypeModel parseFields(Resource dialogResource, JsonNode fields, SchemaModel schemaModel) {
        TypeModel answer = new TypeModel(pageTypeModelType, dialogResource.getParent().getName(), dialogResource.getPath());
        Iterator<JsonNode> i = fields.iterator();
        while(i.hasNext()) {
            JsonNode field = i.next();
            TypeFieldModel typeFieldModel = parseField(field, schemaModel);
            if(typeFieldModel != null) {
                answer.addField(typeFieldModel);
            }
        }
        return answer;
    }

    /**
     * Parse one field into a Schema Type Field Model if accepted
     * @param field Json Node that contains the Field
     * @param schemaModel Schema Model to add enumerations
     * @return Type Field Model if accepted otherwise null
     */
    private TypeFieldModel parseField(JsonNode field, SchemaModel schemaModel) {
        TypeFieldModel answer = null;
        String type = getTextProperty(field, DIALOG_FIELD_TYPE);
        String inputType = getTextProperty(field, DIALOG_FIELD_INPUT_TYPE);
        String model = getTextProperty(field, DIALOG_FIELD_MODEL);
        if (type == null || model == null) {
            logger.warn("Field: {} does not have a property 'type' or 'model' -> ignored", field);
        } else {
            TYPE aType = getType(type);
            switch (aType) {
                case input:
                    answer = new TypeFieldModel()
                        .setName(model)
                        .setType(getInputType(inputType));
                    break;
                case material_textarea:
                case pathbrowser:
                case object_definition_reference:
                case texteditor:
                case material_radios:
                case material_select:
                    answer = new TypeFieldModel()
                        .setName(model)
                        .setType(ScalarEnum.String);
                    break;
                case materialswitch:
                    answer = new TypeFieldModel()
                        .setName(model)
                        .setType(ScalarEnum.Boolean);
                    break;
                case collection:
                    TypeModel collectionType = null;
                    JsonNode fields = field.get(DIALOG_FIELDS_PROPERTY);
                    if(fields.isArray()) {
                        collectionType = new TypeModel(SUB_COMPONENT_TYPE, model, null);
                        Iterator<JsonNode> i = fields.iterator();
                        while(i.hasNext()) {
                            JsonNode collectionField = i.next();
                            TypeFieldModel typeField = parseField(collectionField, schemaModel);
                            if(typeField != null) {
                                collectionType.addField(typeField);
                            }
                        }
                        schemaModel.addType(collectionType);
                    }
                    answer = new TypeFieldModel()
                        .setName(model)
                        .setArray(true);
                    if(collectionType != null) {
                        answer.setCustomType(collectionType);
                    } else {
                        answer.setType(ScalarEnum.String);
                    }
                    break;
                default:
                    logger.warn("Unknown Type of Field: {}", field);
            }
        }
        return answer;
    }

    private void getDialogResources(Resource appsComponentsFolder, List<Resource> pageResources) {
        for(Resource component: appsComponentsFolder.getChildren()) {
            ValueMap properties = component.getValueMap();
            String slingResourceSuperType = properties.get(SLING_RESOURCE_SUPER_TYPE, String.class);
            if(slingResourceSuperType != null && slingResourceSuperType.length() > 0) {
                for(String searchPath: component.getResourceResolver().getSearchPath()) {
                    String superTypePath = searchPath + slingResourceSuperType;
                    Resource target = component.getResourceResolver().getResource(superTypePath);
                    if(target != null) {
                        Resource dialog = target.getChild(DIALOG_NODE_NAME);
                        if (dialog != null) {
                            pageResources.add(dialog);
                        }
                    }
                }
            }
        }
    }

    private String getTextProperty(JsonNode node, String fieldName) {
        String answer = null;
        if(node.has(fieldName)) {
            answer = node.get(fieldName).asText();
        }
        return answer;
    }

    /**
     * Obtain the TYPE enum based on the given type (name adjusted)
     * @param type Type from the Dialog.json Field
     * @return Enum instance if found otherwise TYPE.unknown
     */
    private TYPE getType(final String type) {
        String temp = type.replaceAll("-", "_");
        TYPE answer = Arrays.stream(
                TYPE.values()
        )
            .filter(value -> value.name().equals(temp))
            .findFirst()
            .orElse(TYPE.unknown);
        return answer;
    }

    /**
     * Obtain the Scalar enum based on the given input type (name adjusted)
     * @param inputType Input Type of the Dialog.json field
     * @return SchalarEnum if a matching INPUT_TYPE is found
     */
    private ScalarEnum getInputType(final String inputType) {
        String temp = inputType.replaceAll("-", "_");
        INPUT_TYPE answer = Arrays.stream(
            INPUT_TYPE.values()
        )
            .filter(value -> value.name().equals(temp))
            .findFirst()
            .orElse(INPUT_TYPE.unknown);
        return answer.getScalar();
    }
}
