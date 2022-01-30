package com.peregrine.graphql.schema.json;

import com.peregrine.graphql.schema.model.SchemaModel;
import org.apache.sling.api.resource.Resource;

public interface SchemaModelBuilder {

    int getOrder();

    void buildFromTenant(Resource tenant, SchemaModel schemaModel);

}
