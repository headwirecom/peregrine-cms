package com.peregrine.graphql.schema.model;

public enum QueryTypeEnum {
    List("items"), ByPath("item"), Unknown("");

    String selectedField;
    QueryTypeEnum(String selectedField) {
        this.selectedField = selectedField;
    }

    public String getSelectedField() {
        return selectedField;
    }
}
