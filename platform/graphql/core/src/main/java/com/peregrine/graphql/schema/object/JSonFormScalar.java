package com.peregrine.graphql.schema.object;

public enum JSonFormScalar {
    String("string"),
    Boolean("boolean"),
    Number("number"),
    Integer("integer");

    private String name;
    JSonFormScalar(String name) {
        this.name = name;
    }

    public static JSonFormScalar getEnum(String name) {
        for(JSonFormScalar e: values()) {
            if(e.name.equals(name)) {
                return e;
            }
        }
        throw new IllegalArgumentException("No Enum found for name: " + name);
    }
}
