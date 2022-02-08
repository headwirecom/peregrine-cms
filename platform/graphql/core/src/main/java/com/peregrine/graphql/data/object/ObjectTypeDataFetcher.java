package com.peregrine.graphql.data.object;

import com.peregrine.graphql.data.AbstractTypeDataFetcher;
import com.peregrine.graphql.data.TypeDataFetcher;
import com.peregrine.graphql.schema.model.TypeModel;
import com.peregrine.graphql.schema.model.TypeModelType.Query;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.graphql.schema.object.ObjectDefinitionsSchemaModelBuilderService.objectTypeModelType;

@Component(
    service = TypeDataFetcher.class
)
public class ObjectTypeDataFetcher
    extends AbstractTypeDataFetcher
    implements TypeDataFetcher
{
    private static final List<String> ALLOWED_PRIMARY_TYPES = new ArrayList<>(Arrays.asList("per:Object"));
    public static final String OBJECT_PATH_PROPERTY_NAME = "objectPath";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ObjectTypeDataFetcher() {
        super(objectTypeModelType);
    }

    @Override
    public List<Resource> obtainResource(
        Resource tenantResource, TypeModel typeModel, Query typeQuery, String queryName, Map<String, Object> arguments
    ) {
        List<Resource> answer = new ArrayList<>();
        if(typeQuery != null && typeModel != null) {
            Resource childResource = tenantResource.getChild(OBJECTS);
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
}
