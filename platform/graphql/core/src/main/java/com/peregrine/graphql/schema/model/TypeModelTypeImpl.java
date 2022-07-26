package com.peregrine.graphql.schema.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeModelTypeImpl
    implements TypeModelType
{
    public static final TypeModelTypeImpl SUB_COMPONENT_TYPE = new TypeModelTypeImpl();

    List<Query> queries;
    List<Variable> systemVariableList;

    public TypeModelTypeImpl() {
        this.queries = new ArrayList<>();
        this.systemVariableList = new ArrayList<>();
    }

    public TypeModelTypeImpl withQueries(Query ... queries) {
        this.queries.addAll(Arrays.asList(queries));
        return this;
    }

    public TypeModelTypeImpl withSystemVariables(Variable... systemVariables) {
        this.systemVariableList.addAll(Arrays.asList(systemVariables));
        return this;
    }

    @Override
    public boolean isSub() {
        return queries.isEmpty();
    }

    @Override
    public List<Query> getQueries() {
        return queries;
    }

    @Override
    public List<Variable> getSystemVariables() {
        return systemVariableList;
    }

    public static class QueryImpl implements Query {
        boolean multiple;
        String suffix;
        String description;
        List<Variable> argumentList;

        public QueryImpl(boolean multiple, String suffix, String description) {
            this.multiple = multiple;
            this.suffix = suffix;
            this.description = description;
            this.argumentList = new ArrayList<>();
        }

        public QueryImpl withArguments(Variable ... arguments) {
            this.argumentList = new ArrayList<>(Arrays.asList(arguments));
            return this;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public boolean isMultiple() {
            return multiple;
        }

        @Override
        public String getSuffix() {
            return suffix;
        }

        @Override
        public List<Variable> getArguments() {
            return argumentList;
        }
    }
}
