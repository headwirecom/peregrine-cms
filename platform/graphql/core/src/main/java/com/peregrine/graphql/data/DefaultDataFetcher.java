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
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
        Map<String, Object> arguments = env.getArguments();
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
            String selectedField = queryType.getSelectedField();
            List<Map<String, Object>> data = new ArrayList<>();
            answer.put(selectedField, data);
            if (childResource == null) {
                logger.warn("Child Path: '{}' on Resource: '{}' does not yield a folder", childPath, tenantResource.getPath());
            } else {
                switch (queryType) {
                    case List:
                        getResourcesByObjectPath(childResource, typeModel, typeResources);
                        break;
                    case ByPath:
                        getResourcesByObjectPath(childResource, typeModel, typeResources);
//                        getResourceByPath(childResource, path, typeResources);
                        typeResources = filterByArguments(typeResources, arguments, true);
                        break;
                    case ByFieldNameAndValue:
                        getResourcesByObjectPath(childResource, typeModel, typeResources);
                        // Now filter based on field name and value
                        typeResources = filterByArguments(typeResources, arguments, false);
                        break;
                }
                SelectionSet selectionSet = env.getSelectionSet();
                SelectedField itemsField = selectionSet.get(selectedField);
                if (itemsField != null) {
                    List<SelectedField> selectedFieldList = itemsField.getSubSelectedFields();
                    for (Resource typeResource : typeResources) {
                        Map<String, Object> map = new HashMap<>();
                        for (SelectedField subField : selectedFieldList) {
                            String subFieldName = subField.getName();
                            TypeFieldModel subFieldType = typeModel.getField(subFieldName);
                            Object value = handleSubField( subField, subFieldType, typeResource);
                            map.put(subFieldName, value);
                        }
                        data.add(map);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Query Source '" + source + "' is unknown");
        }
        return answer;
    }

    private List<Resource> filterByArguments(List<Resource> resources, Map<String, Object> queryArguments, boolean onlyByPath) {
        List<Resource> answer = resources;
        String pathArgument = (String) queryArguments.get(PATH_FIELD_NAME);
        if(pathArgument != null && !pathArgument.isEmpty()) {
            answer = answer.stream()
                .filter(r -> r.getPath().startsWith(pathArgument))
                .collect(Collectors.toList());
        }
        if(!onlyByPath) {
            String fieldName = (String) queryArguments.get("fieldName");
            String fieldValue = (String) queryArguments.get("fieldValue");
            if(fieldName != null) {
                answer = answer.stream()
                    .filter(r -> {
                        ValueMap properties = r.getValueMap();
                        if (properties.containsKey(fieldName)) {
                            if(fieldValue == null || fieldValue.isEmpty()) {
                                return true;
                            } else {
                                return fieldValue.equals(properties.get(fieldName, String.class));
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            } else {
                answer.clear();
            }
        }
        return answer;
    }

//    private List<Resource> filterByFieldArguments(List<Resource> resources, Map<String, Object> queryArguments) {
//        List<Resource> answer = resources;
//        List<String> fields = (List<String>) queryArguments.get("fields");
//        Object operation = queryArguments.get("operation");
//        if(fields != null && (fields.size() % 2) == 0 ) {
//            Map<String,String> fieldMap = new HashMap<>();
//            for(int i = 0; i < (fields.size() / 2); i++) {
//                fieldMap.put(fields.get(i), fields.get(i + 1));
//            }
//            answer = answer.stream()
//                .filter(r -> {
//                    ValueMap properties = r.getValueMap();
//                    boolean insideAnswer = true;
//                    for(Entry<String,String> entry: fieldMap.entrySet()) {
//                        String fieldName = entry.getKey();
//                        String fieldValue = entry.getValue();
//                        if(fieldName != null) {
//                            if (properties.containsKey(fieldName)) {
//                                if(!fieldValue.equals(properties.get(fieldName, String.class))) {
//                                    insideAnswer = false;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    return insideAnswer;
//                })
//                .collect(Collectors.toList());
//        }
//        return answer;
//    }

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
            TypeModel customType = (TypeModel) fieldType.getCustomType();
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
                List<Resource> resources = getResourcesOfType(root, typeModel);
                if(!resources.isEmpty()) {
                    result.addAll(resources);
                }
            }
        }
        for(Resource child: root.getChildren()) {
            getResourcesByObjectPath(child, typeModel, result);
        }
    }

    private List<Resource> getResourcesOfType(Resource root, TypeModel typeModel) {
        Resource content = root.getChild(JCR_CONTENT);
        List<Resource> answer = new ArrayList<>();
        if(content != null) {
            answer.addAll(resourcesContainsComponent(content, typeModel));
        }
        return answer;
    }

    private List<Resource> resourcesContainsComponent(Resource root, TypeModel typeModel) {
        List<Resource> answer = new ArrayList<>();
        for(Resource child: root.getChildren()) {
            ValueMap properties = child.getValueMap();
            String resourceType = properties.get(SLING_RESOURCE_TYPE, String.class);
            if(resourceType != null) {
                if(resourceType.endsWith(typeModel.getName())) {
                    answer.add(child);
                }
            } else {
                answer.addAll(resourcesContainsComponent(child, typeModel));
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
        return Arrays.stream(QueryTypeEnum.values())
            .filter(e -> queryTypeName.endsWith(e.toString()))
            .findFirst()
            .orElse(QueryTypeEnum.Unknown);
    }
}
