package com.peregrine.jsonschema.specification;

import java.util.List;

public interface Schema {

    String getSource();
    String getVersion();
    String getId();

    List<Property> getProperties();
    List<Property> getRequiredProperties();
    List<Property> getDependencyProperties();
    List<String> getReferences();

    Property getProperty(String name);
}
