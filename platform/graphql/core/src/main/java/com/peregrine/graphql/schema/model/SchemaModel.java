package com.peregrine.graphql.schema.model;

import com.peregrine.graphql.schema.model.TypeModelType.Query;
import com.peregrine.graphql.schema.model.TypeModelType.Variable;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.graphql.schema.GraphQLConstants.ALL_SCHEMA_MODELS;
import static com.peregrine.graphql.schema.GraphQLConstants.FETCHER_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.LIST_SEPARATOR;

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

    public boolean containsEnumByName(String enumerationName) {
        boolean answer = false;
        for(EnumModel enumModel: enumerations) {
            if(enumModel.getName().equals(enumerationName)) {
                answer = true;
                break;
            }
        }
        return answer;
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
            if(!type.isSubType()) {
                answer += type.getName() + LIST_SEPARATOR;
            }
        }
        if(answer.endsWith(LIST_SEPARATOR)) {
            answer = answer.substring(0, answer.length() - LIST_SEPARATOR.length());
        }
        answer += "\n\n";
        // Handle Fetcher
        answer += "directive @fetcher(name: String, options: String, source: String) on FIELD_DEFINITION\n" +
            "type QueryType {\n";
        for(TypeModel type: types) {
            if(!type.isSubType()) {
                String name = type.getName();
                TypeModelType modelType = type.getType();
                for(Query query: modelType.getQueries()) {
                    String queryName = name + query.getSuffix();
                    String resultName = name + "Result" + (query.isMultiple() ? "s" : "");
                    List<Variable> arguments = query.getArguments();
                    String argumentLine = arguments.isEmpty() ? "" : "(";
                    for(Variable argument: arguments) {
                        argumentLine += argument.getName() + ":" + argument.getType() + (argument.isMandatory() ? "!" : "") + ",";
                    }
                    if(!arguments.isEmpty()) {
                        argumentLine = argumentLine.substring(0, argumentLine.length() - 1) + ")";
                    }
                    answer +=
                        "  \"\"\"\n" +
                        " " + query.getDescription() + "\n" +
                        "  \"\"\"\n" +
                        "  " + queryName + argumentLine + ": " + resultName + "! @fetcher(name : " + FETCHER_NAME + ", source : \"" + queryName + "\")\n";
                }
            }
        }
        answer += "}";
        return answer;
    }
}
