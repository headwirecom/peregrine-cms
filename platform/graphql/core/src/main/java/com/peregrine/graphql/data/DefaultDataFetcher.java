package com.peregrine.graphql.data;

import com.peregrine.graphql.schema.SchemaProvider;
import com.peregrine.graphql.schema.model.QueryTypeEnum;
import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeFieldModel;
import com.peregrine.graphql.schema.model.TypeModel;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.graphql.api.SelectedField;
import org.apache.sling.graphql.api.SelectionSet;
import org.apache.sling.graphql.api.SlingDataFetcher;
import org.apache.sling.graphql.api.SlingDataFetcherEnvironment;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGES;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;
import static com.peregrine.graphql.schema.json.DialogJsonConstants.DIALOG_TYPE;
import static com.peregrine.graphql.schema.json.JSonFormConstants.JSON_FORM_TYPE;

@Component(
    service = SlingDataFetcher.class,
    property = { "name=sites/default"})
public class DefaultDataFetcher
    implements SlingDataFetcher<Object>
{

    private static final Map<Integer, List<String>> ALLOWED_PRIMARY_TYPES = new HashMap<Integer, List<String>>() {{
        put(JSON_FORM_TYPE, new ArrayList<>(Arrays.asList("per:Object")));
        put(DIALOG_TYPE, new ArrayList<>(Arrays.asList("per:Page", "per:Asset", "per:Object", "per:ObjectDefinition")));
    }};

    public static final String OBJECT_PATH_PROPERTY_NAME = "objectPath";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private SchemaProvider schemaProvider;

//    private QueryField getFetcherField(TypeElement type, String fieldName) {
//        Iterable<Field> fields = type.getFields();
//        for (Field field : fields) {
//            if ((field instanceof QueryField) && field.getUniqueName().equals(fieldName)) {
//                return (QueryField) field;
//            }
//        }
//        throw new IllegalArgumentException("Invalid QueryType field: " + fieldName);
//    }

    @Override
    public Object get(SlingDataFetcherEnvironment env) throws Exception {
        Map<String, Object> answer = new HashMap<>();

        Resource tenantResource = env.getCurrentResource();
        String name = "sites/default";
        String source = env.getFetcherSource();
        if (source == null) {
            throw new IllegalArgumentException("No source provided to data fetcher.");
        }

        logger.debug("Creating fetcher for resource {}. name: {}, source: {}",
            tenantResource.getPath(), name, source);

        SchemaModel schemaModel = schemaProvider.getSchemaModel(tenantResource);
        QueryTypeEnum queryType = getQueryType(source);
        if(queryType != QueryTypeEnum.Unknown) {
            String typeName = source.substring(0, source.length() - queryType.toString().length());
            TypeModel typeModel = schemaModel.getTypeByName(typeName);
            String path = typeModel.getPath();
            List<Resource> typeResources = new ArrayList<>();
            int type = typeModel.getType();
            String childPath = type == JSON_FORM_TYPE ? OBJECTS : (type == DIALOG_TYPE ? PAGES : null);
            Resource childResource = childPath != null ? tenantResource.getChild(childPath) : null;
            if (childResource == null) {
                logger.warn("Child Path: '{}' on Resource: '{}' does not yield a folder", childPath, tenantResource.getPath());
            } else {
                switch (queryType) {
                    case List:
                        getResourcesByObjectPath(childResource, typeModel, typeResources);
                        break;
                    case ByPath:
                        getResourceByPath(childResource, path, typeResources);
                        break;
                }
                SelectionSet selectionSet = env.getSelectionSet();
                String selectedField = queryType.getSelectedField();
                SelectedField itemsField = selectionSet.get(selectedField);
                if (itemsField != null) {
                    List<SelectedField> selectedFieldList = itemsField.getSubSelectedFields();
                    List<Map<String, Object>> data = new ArrayList<>();
                    answer.put(selectedField, data);
                    for (Resource typeResource : typeResources) {
                        Map<String, Object> map = new HashMap<>();
                        for (SelectedField subField : selectedFieldList) {
                            String subFieldName = subField.getName();
                            TypeFieldModel subFieldType = typeModel.getField(subFieldName);
                            Object value = handleSubField( subField, subFieldType, typeResource);
                            map.put(subFieldName, value);
//                            // Handle _path
//                            if (subFieldName.equals(PATH_FIELD_NAME)) {
//                                row.put(PATH_FIELD_NAME, typeResource.getPath());
//                            } else {
//                                ValueMap properties = typeResource.getValueMap();
//                                Object value = null;
//                                if(properties.containsKey(subFieldName)) {
//                                    value = properties.get(subField.getName());
//                                } else {
//                                    Resource subNode = typeResource.getChild(subFieldName);
//                                    if(subNode != null) {
//                                        value = handleSubFields(subField, subNode);
//                                    }
//                                }
//                                row.put(subFieldName, value);
//                            }
                        }
                        data.add(map);
                    }
                }
            }
        }
        return answer;
    }

    private Object handleSubField(SelectedField field, TypeFieldModel fieldType, Resource resource) {
        Object answer = null;
        String fieldName = field.getName();
        if(PATH_FIELD_NAME.equals(fieldName)) {
            answer = resource.getPath();
        } else if(field.getSubSelectedFields().size() == 0) {
            ValueMap properties = resource.getValueMap();
            if (properties.containsKey(fieldName)) {
                answer = properties.get(fieldName);
            }
        } else {
            Map<String, Object> map = new HashMap<>();
            //TODO: Check if we need to search by Type Field Model's Original Name instead
            Resource childResource = resource.getChild(fieldName);
            TypeModel customType = fieldType.getCustomType();
            if(fieldType.isArray()) {
                // Loop Over Children
                List<Object> list = new ArrayList<>();
                for(Resource grandChildResource: childResource.getChildren()) {
                    Map<String, Object> childMap = new HashMap<>();
                    for (SelectedField childField : field.getSubSelectedFields()) {
                        TypeFieldModel childFieldType = customType.getField(childField.getName());
                        Object value = handleSubField(childField, childFieldType, grandChildResource);
//                        list.add(value);
                        childMap.put(childField.getName(), value);
//                        map.put(childField.getName(), value);
                    }
                    list.add(childMap);
                }
                answer = list;
//                map.put(fieldName, list);
            } else {
                for (SelectedField childField : field.getSubSelectedFields()) {
                    Object value = handleSubField(childField, fieldType, childResource);
                    map.put(childField.getName(), value);
                }
                answer = map;
            }
        }
        return answer;
    }

    private void getResourcesByObjectPath(Resource root, TypeModel typeModel, List<Resource> result) {
        int type = typeModel.getType();
        String path = typeModel.getPath();
        if(root == null || path == null) {
            logger.warn("No Root given to find Result Resources");
            return;
        }
        ValueMap properties = root.getValueMap();
        String primaryType = properties.get(JCR_PRIMARY_TYPE, String.class);
        List<String> allowedPrimaryTypes = ALLOWED_PRIMARY_TYPES.get(type);
        if(allowedPrimaryTypes != null && allowedPrimaryTypes.contains(primaryType)) {
            if(type == JSON_FORM_TYPE) {
                String objectPath = properties.get(OBJECT_PATH_PROPERTY_NAME, String.class);
                if (OBJECT_PRIMARY_TYPE.equals(primaryType) && path.equals(objectPath)) {
                    result.add(root);
                }
            } else if(type == DIALOG_TYPE) {
                Resource resource = getResourceOfType(root, typeModel);
                if(resource != null) {
                    result.add(resource);
                }
            }
        }
        for(Resource child: root.getChildren()) {
            getResourcesByObjectPath(child, typeModel, result);
        }
    }

    private Resource getResourceOfType(Resource root, TypeModel typeModel) {
        Resource content = root.getChild(JCR_CONTENT);
        Resource answer = null;
        if(content != null) {
                    answer = resourceContainsComponent(content, typeModel);
                }
        return answer;
    }

    private Resource resourceContainsComponent(Resource root, TypeModel typeModel) {
        Resource answer = null;
        for(Resource child: root.getChildren()) {
            ValueMap properties = child.getValueMap();
            String resourceType = properties.get(SLING_RESOURCE_TYPE, String.class);
            if(resourceType != null) {
                if(resourceType.endsWith(typeModel.getName())) {
                    return child;
                }
            } else {
                answer = resourceContainsComponent(child, typeModel);
            }
        }
        return answer;
    }

    private void getResourceByPath(Resource root, String path, List<Resource> result) {
        if(root == null || path == null) {
            // Just a fallback for bad data
            return;
        }
        Resource item = root.getResourceResolver().getResource(path);
        if(item != null) {
            result.add(item);
        }
    }

    private QueryTypeEnum getQueryType(String queryTypeName) {
        if(queryTypeName.endsWith(QueryTypeEnum.List.toString())) {
            return QueryTypeEnum.List;
        } else if(queryTypeName.endsWith(QueryTypeEnum.ByPath.toString())) {
            return QueryTypeEnum.ByPath;
        } else {
            return QueryTypeEnum.Unknown;
        }
    }
}
