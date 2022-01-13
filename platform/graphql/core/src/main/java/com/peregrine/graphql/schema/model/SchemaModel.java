package com.peregrine.graphql.schema.model;

import java.util.ArrayList;
import java.util.List;

public class SchemaModel {

    private List<TypeModel> types = new ArrayList<>();
    private List<EnumModel> enumerations = new ArrayList<>();

    public TypeModel getTypeByListName(String listName) {
        TypeModel answer = null;
        for(TypeModel type: types) {
            if(type.getListName().equals(listName)) {
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
        answer += "directive @fetcher(name: String, options: String, source: String) on FIELD_DEFINITION\n" +
            "type QueryType {\n";
        for(TypeModel type: types) {
            String name = type.getName();
            String listName = type.getListName();
            String listResultName = type.getListResultName();
            answer +=
                "  \"\"\"\n" +
                "  Get a List of " + name + "\n" +
                "  \"\"\"\n" +
                "  " + listName + ": " + listResultName + "! @fetcher(name : \"sites/default\", source : \"" + listName + "\")\n";
        }
        answer += "}";
        return answer;
    }
}
