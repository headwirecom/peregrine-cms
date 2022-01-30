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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.OBJECT_DEFINITIONS;
import static com.peregrine.graphql.schema.json.JSonFormConstants.JSON_ENUMS;
import static com.peregrine.graphql.schema.json.JSonFormConstants.JSON_FORMAT_PROPERTY;
import static com.peregrine.graphql.schema.json.JSonFormConstants.JSON_OBJECT_TYPE;
import static com.peregrine.graphql.schema.json.JSonFormConstants.JSON_PROPERTIES;
import static com.peregrine.graphql.schema.json.JSonFormConstants.JSON_REQUIRED;
import static com.peregrine.graphql.schema.json.JSonFormConstants.JSON_TYPE;

@Component(
    service = SchemaModelHandler.class,
    immediate = true
)
public class SchemaModelHandlerService
    implements SchemaModelHandler
{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    List<SchemaModelBuilder> schemaModelBuilderList = new ArrayList<>();

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC
    )
    void addSchemaModelBuilder(SchemaModelBuilder schemaModelBuilder) {
        schemaModelBuilderList.add(schemaModelBuilder);
        schemaModelBuilderList.stream()
            .sorted(Comparator.comparing(SchemaModelBuilder::getOrder))
            .collect(Collectors.toList());
    }

    void removeSchemaModelBuilder(SchemaModelBuilder schemaModelBuilder) {
        schemaModelBuilderList.remove(schemaModelBuilder);
    }

    public SchemaModel convert(Resource tenantResource) {
        SchemaModel answer = null;
        if(tenantResource == null || ResourceUtil.isNonExistingResource(tenantResource)) {
            logger.warn("Called with a null or non-existing resource: {}", tenantResource);
        } else {
            answer = new SchemaModel();
            for (SchemaModelBuilder builder : schemaModelBuilderList) {
                builder.buildFromTenant(tenantResource, answer);
            }
        }
        return answer;
    }
}
