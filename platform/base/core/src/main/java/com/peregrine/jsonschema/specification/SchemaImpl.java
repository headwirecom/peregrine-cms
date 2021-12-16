package com.peregrine.jsonschema.specification;

import java.util.ArrayList;
import java.util.List;

public class SchemaImpl
    implements Schema
{
    String version;
    String id;
    List<Property> properties = new ArrayList<>();
    List<Property> required = new ArrayList<>();
    List<Property> dependencies = new ArrayList<>();
    List<String> references = new ArrayList<>();

    public SchemaImpl(String version, String id, List<String> references) {
        this.version = version;
        this.id = id;
        if(references != null) {
            this.references.addAll(references);
        }
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<Property> getProperties() {
        return properties;
    }

    @Override
    public List<Property> getRequiredProperties() {
        return required;
    }

    @Override
    public List<Property> getDependencyProperties() {
        return dependencies;
    }

    @Override
    public Property getProperty(String name) {
        return properties.stream().filter(p -> p.getName().equals(name))
            .findFirst().orElse(null);
    }

    public List<String> getReferences() {
        return references;
    }

    public SchemaImpl addProperty(Property property) {
        this.properties.add(property);
        if(property.isRequired()) {
            this.required.add(property);
        }
        for(Property item: property.getDependencies()) {
            if(!this.dependencies.contains(item)) {
                this.dependencies.add(item);
            }
        }
        return this;
    }
}
