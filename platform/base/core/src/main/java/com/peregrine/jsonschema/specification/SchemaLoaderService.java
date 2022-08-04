package com.peregrine.jsonschema.specification;

import org.apache.sling.api.resource.ResourceResolver;

public interface SchemaLoaderService {
    boolean isSchemaAvailable(ResourceResolver resourceResolver, String nodePath);

    Schema getSchema(ResourceResolver resourceResolver, String nodePath);
}
