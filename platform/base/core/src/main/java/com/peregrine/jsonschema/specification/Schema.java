package com.peregrine.jsonschema.specification;

import java.util.List;

public interface Schema {

    String getVersion();
    String getId();

    List<Property> getProperties();
    List<Property> getRequiredProperties();
    List<Property> getDependencyProperties();

    Property getProperty(String name);
}
