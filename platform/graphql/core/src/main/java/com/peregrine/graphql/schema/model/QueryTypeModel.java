package com.peregrine.graphql.schema.model;

import java.util.ArrayList;
import java.util.List;

public class QueryTypeModel {

    private List<TypeModel> types = new ArrayList<>();

    public QueryTypeModel() {
    }

    public QueryTypeModel addType(TypeModel type) {
        if(type != null) {
            this.types.add(type);
        }
        return this;
    }
}
