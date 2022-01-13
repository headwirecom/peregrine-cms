package com.peregrine.graphql.schema.json;

import com.peregrine.graphql.schema.model.SchemaModel;
import org.apache.sling.api.resource.Resource;

public interface SchemaModelHandler {

    SchemaModel convert(Resource objectDefinitionsResource);
}
