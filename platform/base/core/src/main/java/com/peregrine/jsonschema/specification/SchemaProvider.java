package com.peregrine.jsonschema.specification;

import java.util.List;

public interface SchemaProvider {

    Schema getSchemaNameById(String schemaId);

    List<Schema> getSchemas();
}
