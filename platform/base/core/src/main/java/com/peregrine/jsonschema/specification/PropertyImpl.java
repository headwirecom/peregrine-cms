package com.peregrine.jsonschema.specification;

import java.util.ArrayList;
import java.util.List;

public class PropertyImpl
    implements Property
{
    String name;
    String type;
    boolean required;
    List<Property> dependencies = new ArrayList<>();

    public PropertyImpl(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public List<Property> getDependencies() {
        return dependencies;
    }

    public PropertyImpl setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public PropertyImpl addDependency(Property dependency) {
        this.dependencies.add(dependency);
        return this;
    }
}
