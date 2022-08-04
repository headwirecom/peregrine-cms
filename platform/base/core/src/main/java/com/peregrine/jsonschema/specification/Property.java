package com.peregrine.jsonschema.specification;

import java.util.List;

public interface Property {

    String getName();
    String getType();
    boolean isRequired();
    List<Property> getDependencies();
}
