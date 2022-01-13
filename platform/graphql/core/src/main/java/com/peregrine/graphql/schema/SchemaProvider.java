package com.peregrine.graphql.schema;

import com.peregrine.graphql.schema.model.SchemaModel;
import org.apache.sling.api.resource.Resource;

public interface SchemaProvider {

    String getSchema(Resource schemaParent);

    SchemaModel getSchemaModel(Resource schemaParent);
}
