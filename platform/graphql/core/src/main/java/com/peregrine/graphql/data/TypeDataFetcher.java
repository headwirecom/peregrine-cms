package com.peregrine.graphql.data;

import com.peregrine.graphql.schema.model.SchemaModel;
import com.peregrine.graphql.schema.model.TypeModel;
import com.peregrine.graphql.schema.model.TypeModelType.Query;
import org.apache.sling.api.resource.Resource;

import java.util.List;
import java.util.Map;

public interface TypeDataFetcher {
    Query getQuery(String queryName);
    TypeModel getTypeModel(Query query, SchemaModel schemaModel, String queryName);
    List<Resource> obtainResource(Resource tenantResource, TypeModel typeModel, Query typeQuery, String queryName, Map<String, Object> arguments);
}
