package com.peregrine.graphql.schema.model;

import java.util.List;

public interface TypeModelType {
    boolean isSub();
    List<Query> getQueries();
    List<Variable> getSystemVariables();

    interface Query {
        boolean isMultiple();
        String getSuffix();
        String getDescription();
        List<Variable> getArguments();
    }

    interface Variable {
        String getName();
        String getType();
        boolean isMandatory();
    }
}
