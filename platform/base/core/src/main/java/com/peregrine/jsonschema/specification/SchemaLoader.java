package com.peregrine.jsonschema.specification;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Date;

public interface SchemaLoader {

    String SCHEMA_JSON_NODE_NAME = "schema.json";

    /**
     * Checks if the given path is a Schema
     * @param resourceResolver Resource Resolver to find resources
     * @param nodePath Path to the schema.json or its parent
     * @return True if this is a schema or false if not
     */
    boolean isSchema(ResourceResolver resourceResolver, String nodePath);

    /**
     * Obtain the Resource of the Schema.json node
     * @param resourceResolver Resource Resolver to find resources
     * @param nodePath Path to the schema.json or its parent
     * @return Resource of the schema.json node or null if not found
     */
    Resource getSchemaResource(ResourceResolver resourceResolver, String nodePath);

    /**
     * Check if the given Schema stored
     */
    Date getSchemaTimestamp(Resource schemaResource);

    /**
     * Obtain the Schema from the given Resource
     * @param schemaResource Resource of the schema.json node
     * @return Schema if found otherwise null
     */
    Schema getSchema(Resource schemaResource);
}
