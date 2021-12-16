package com.peregrine.jsonschema.specification;

import java.util.List;

public interface Property {

    String REFERENCE_TYPE_NAME = "";

    String getName();
    String getType();
    boolean isReference();
    boolean isRequired();
    List<Property> getDependencies();
}
