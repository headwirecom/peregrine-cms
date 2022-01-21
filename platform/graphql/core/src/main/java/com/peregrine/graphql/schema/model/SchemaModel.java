package com.peregrine.graphql.schema.model;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.graphql.schema.GraphQLConstants.ALL_SCHEMA_MODELS;
import static com.peregrine.graphql.schema.GraphQLConstants.BY_PATH_MODEL_SUFFIX;
import static com.peregrine.graphql.schema.GraphQLConstants.BY_PATH_SUFFIX;
import static com.peregrine.graphql.schema.GraphQLConstants.FETCHER_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.LIST_MODEL_SUFFIX;
import static com.peregrine.graphql.schema.GraphQLConstants.LIST_SEPARATOR;
import static com.peregrine.graphql.schema.GraphQLConstants.LIST_SUFFIX;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;

public class SchemaModel {

    private List<TypeModel> types = new ArrayList<>();
    private List<EnumModel> enumerations = new ArrayList<>();

    public TypeModel getTypeByName(String typeName) {
        TypeModel answer = null;
        for (TypeModel type : types) {
            if (typeName.equals(type.getName())) {
                answer = type;
                break;
            }
        }
        return answer;
    }

    public SchemaModel addType(TypeModel type) {
        types.add(type);
        return this;
    }

    public SchemaModel addEnum(EnumModel enumeration) {
        enumerations.add(enumeration);
        return this;
    }

    public String print() {
        String answer =
            "schema {\n" +
            "  query: QueryType\n" +
            "}\n" +
            "\n";
        for(EnumModel enumeration: enumerations) {
            answer += enumeration.print();
        }
        for(TypeModel type: types) {
            answer += type.print();
        }
        // Handling Unions
        answer += "directive @resolver(name: String, options: String, source: String) on UNION\n" +
            "union " + ALL_SCHEMA_MODELS + " @resolver(name : " + FETCHER_NAME + ", source : \"" + ALL_SCHEMA_MODELS + "\") = ";
        for(TypeModel type: types) {
            answer += type.getName() + LIST_SEPARATOR;
        }
        if(answer.endsWith(LIST_SEPARATOR)) {
            answer = answer.substring(0, answer.length() - LIST_SEPARATOR.length());
        }
        answer += "\n\n";
        // Handle Fetcher
        answer += "directive @fetcher(name: String, options: String, source: String) on FIELD_DEFINITION\n" +
            "type QueryType {\n";
        for(TypeModel type: types) {
            String name = type.getName();
            String listName = name + QueryTypeEnum.List;
            String listResultName = name + LIST_MODEL_SUFFIX;
            String byPathName = name + QueryTypeEnum.ByPath;
            String ByPathResultName = name + BY_PATH_MODEL_SUFFIX;
            answer +=
                "  \"\"\"\n" +
                "  Get a List of " + name + "\n" +
                "  \"\"\"\n" +
                "  " + listName + ": " + listResultName + "! @fetcher(name : " + FETCHER_NAME + ", source : \"" + listName + "\")\n" +
                "  \"\"\"\n" +
                "  Get a Single Instance of " + name + " by Path\n" +
                "  \"\"\"\n" +
                "  " + byPathName + "(" + PATH_FIELD_NAME + ": String!): " + ByPathResultName + "! @fetcher(name : " + FETCHER_NAME + ", source : \"" + byPathName + "\")\n";
        }
        answer += "}";
        return answer;
    }
}
