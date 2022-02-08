package com.peregrine.graphql.schema;

import com.peregrine.graphql.schema.model.SchemaModel;
import org.apache.sling.api.resource.Resource;

public interface SchemaModelBuilder {

    int getOrder();

    void build(Resource tenant, SchemaModel schemaModel);

}
