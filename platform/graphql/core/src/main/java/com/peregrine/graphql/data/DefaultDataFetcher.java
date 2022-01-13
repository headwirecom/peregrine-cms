package com.peregrine.graphql.data;

import com.peregrine.graphql.schema.SchemaProvider;
import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeModel;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.graphql.schema.GraphQLConstants.ITEMS_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;

@Component(
    service = SlingDataFetcher.class,
    property = { "name=sites/default"})
public class DefaultDataFetcher
    implements SlingDataFetcher<Object>
{

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

        Resource resource = env.getCurrentResource();
        String name = "sites/default";
        String source = env.getFetcherSource();
        if (source == null) {
            throw new IllegalArgumentException("No source provided to data fetcher.");
        }
        Resource objectsResource = resource.getChild(OBJECTS);
        if(objectsResource == null || ResourceUtil.isNonExistingResource(objectsResource)) {
            throw new IllegalArgumentException("Resource: '" + resource.getPath() + "' does not contain Objects");
        }

        logger.debug("Creating fetcher for resource {}. name: {}, source: {}",
            resource.getPath(), name, source);

        SchemaModel schemaModel = schemaProvider.getSchemaModel(resource);
        TypeModel typeModel = schemaModel.getTypeByListName(source);
        String path = typeModel.getPath();
        List<Resource> typeResources = new ArrayList<>();
        getResourcesByObjectPath(objectsResource, path, typeResources);
        SelectionSet selectionSet = env.getSelectionSet();
        SelectedField itemsField = selectionSet.get(ITEMS_FIELD_NAME);
        if(itemsField != null) {
            List<SelectedField> selectedFieldList = itemsField.getSubSelectedFields();
            List<Map<String, Object>> data = new ArrayList<>();
            answer.put(ITEMS_FIELD_NAME, data);
            for(Resource typeResource: typeResources) {
                Map<String, Object> row = new HashMap<>();
                for (SelectedField subField : selectedFieldList) {
                    // Handle _path
                    if (subField.getName().equals(PATH_FIELD_NAME)) {
                        row.put(PATH_FIELD_NAME, typeResource.getPath());
                    } else {
                        ValueMap properties = typeResource.getValueMap();
                        Object value = properties.get(subField.getName());
                        row.put(subField.getName(), value);
                    }
                }
                data.add(row);
            }
        }
        return answer;
    }

    private void getResourcesByObjectPath(Resource root, String path, List<Resource> result) {
        if(root == null || path == null) {
            // Just a fallback for bad data
            return;
        }
        ValueMap properties = root.getValueMap();
        String primaryType = properties.get(JCR_PRIMARY_TYPE, String.class);
        String objectPath = properties.get(OBJECT_PATH_PROPERTY_NAME, String.class);
        if(OBJECT_PRIMARY_TYPE.equals(primaryType) && path.equals(objectPath)) {
            result.add(root);
        }
        for(Resource child: root.getChildren()) {
            getResourcesByObjectPath(child, path, result);
        }
    }
}
