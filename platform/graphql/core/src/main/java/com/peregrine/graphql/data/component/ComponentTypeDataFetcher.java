package com.peregrine.graphql.data.component;

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

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGES;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.graphql.schema.component.PageSchemaModelBuilderService.pageTypeModelType;

@Component(
    service = TypeDataFetcher.class
)
public class ComponentTypeDataFetcher
    extends AbstractTypeDataFetcher
    implements TypeDataFetcher
{
    private static final List<String> ALLOWED_PRIMARY_TYPES = new ArrayList<>(Arrays.asList("per:Page", "per:Asset", "per:Object", "per:ObjectDefinition"));

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ComponentTypeDataFetcher() {
        super(pageTypeModelType);
    }

    @Override
    public List<Resource> obtainResource(
        Resource tenantResource, TypeModel typeModel, Query typeQuery, String queryName, Map<String, Object> arguments
    ) {
        List<Resource> answer = new ArrayList<>();
        if(typeQuery != null && typeModel != null) {
            String typeName = queryName.substring(0, queryName.length() - typeQuery.getSuffix().length());
            Resource childResource = tenantResource.getChild(PAGES);
            if (childResource == null) {
                logger.warn("Child Path: '{}' on Resource: '{}' does not yield a folder", PAGES, tenantResource.getPath());
            } else {
                if (queryName.endsWith("List")) {
                    getResourcesByObjectPath(childResource, typeModel, answer);
                } else if (queryName.endsWith("ByPath")) {
                    getResourcesByObjectPath(childResource, typeModel, answer);
                    answer = filterByArguments(answer, arguments, true);
                } else if(queryName.endsWith("ByField")) {
                    getResourcesByObjectPath(childResource, typeModel, answer);
                    // Now filter based on field name and value
                    answer = filterByArguments(answer, arguments, false);
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
            List<Resource> resources = getResourcesOfType(root, typeModel);
            if(!resources.isEmpty()) {
                result.addAll(resources);
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
