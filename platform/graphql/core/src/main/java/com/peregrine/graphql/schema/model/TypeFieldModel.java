package com.peregrine.graphql.schema.model;

public class TypeFieldModel {

    private String name;
    private ScalarEnum type;
    private TypeModel customType;
    private boolean id;
    private boolean notNull;
    private boolean array;

    public String getName() {
        return name;
    }

    public TypeFieldModel setName(String name) {
        this.name = name;
        return this;
    }

    public ScalarEnum getType() {
        return type;
    }

    public TypeFieldModel setType(ScalarEnum type) {
        this.type = type;
        return this;
    }

    public TypeModel getCustomType() {
        return customType;
    }

    public TypeFieldModel setCustomType(TypeModel customType) {
        this.customType = customType;
        this.customType.setSubType(true);
        return this;
    }

    public boolean isId() {
        return id;
    }

    public TypeFieldModel setId(boolean id) {
        this.id = id;
        return this;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public TypeFieldModel setNotNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }

    public boolean isArray() {
        return array;
    }

    public TypeFieldModel setArray(boolean array) {
        this.array = array;
        return this;
    }
}
