package com.peregrine.graphql.data.object;

import com.peregrine.graphql.data.TypeDataFetcher;
import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeModel;
import com.peregrine.graphql.schema.model.TypeModelType;
import com.peregrine.graphql.schema.model.TypeModelType.Query;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;
import static com.peregrine.graphql.schema.component.DialogJsonConstants.COMPONENT_TYPE;
import static com.peregrine.graphql.schema.object.JSonFormConstants.OBJECT_TYPE;
import static com.peregrine.graphql.schema.object.ObjectDefinitionsSchemaModelBuilderService.objectTypeModelType;

@Component(
    service = TypeDataFetcher.class
)
public class ObjectTypeDataFetcher
    implements TypeDataFetcher
{
    private static final List<String> ALLOWED_PRIMARY_TYPES = new ArrayList<>(Arrays.asList("per:Object"));
    public static final String OBJECT_PATH_PROPERTY_NAME = "objectPath";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Query getQuery(String queryName) {
        Query answer = null;
        if(queryName != null) {
            for (Query query : objectTypeModelType.getQueries()) {
                if (queryName.endsWith(query.getSuffix())) {
                    answer = query;
                    break;
                }
            }
        }
        return answer;
    }

    @Override
    public TypeModel getTypeModel(Query typeQuery, SchemaModel schemaModel, String queryName) {
        TypeModel answer = null;
        if(typeQuery != null && schemaModel != null && queryName != null) {
            String typeName = queryName.substring(0, queryName.length() - typeQuery.getSuffix().length());
            answer = schemaModel.getTypeByName(typeName);
        }
        return answer;
    }

    @Override
    public List<Resource> obtainResource(
        Resource tenantResource, TypeModel typeModel, Query typeQuery, String queryName, Map<String, Object> arguments
    ) {
        List<Resource> answer = new ArrayList<>();
        if(typeQuery != null && typeModel != null) {
//        TypeModelType type = typeModel.getType();
            Resource childResource = tenantResource.getChild(OBJECTS);
            String selectedField = "item" + (typeQuery.isMultiple() ? "s" : "");
            List<Map<String, Object>> data = new ArrayList<>();
            if (childResource == null) {
                logger.warn("Child Path: '{}' on Resource: '{}' does not yield a folder", OBJECTS, tenantResource.getPath());
            } else {
                if (queryName.endsWith("List")) {
                    getResourcesByObjectPath(childResource, typeModel, answer);
                } else if (queryName.endsWith("ByPath")) {
                    getResourcesByObjectPath(childResource, typeModel, answer);
                    answer = filterByArguments(answer, arguments, true);
                }
            }
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
    private void getResourcesByObjectPath(Resource root, TypeModel typeModel, List<Resource> result) {
        String path = typeModel.getPath();
        if(root == null || path == null) {
            logger.warn("No Root given to find Result Resources");
            return;
        }
        ValueMap properties = root.getValueMap();
        String primaryType = properties.get(JCR_PRIMARY_TYPE, String.class);
        if(ALLOWED_PRIMARY_TYPES.contains(primaryType)) {
            String objectPath = properties.get(OBJECT_PATH_PROPERTY_NAME, String.class);
            if (OBJECT_PRIMARY_TYPE.equals(primaryType) && path.equals(objectPath)) {
                result.add(root);
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
}
