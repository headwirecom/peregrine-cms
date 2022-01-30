package com.peregrine.graphql.schema.model;

import java.util.ArrayList;
import java.util.List;

public class EnumModel extends AbstractTypeModel {

    private List<String> enumeration;

    public EnumModel(int type, String name) {
        super(type, name);
        this.enumeration = new ArrayList<>();
    }

    public EnumModel addEnumeration(String enumeration) {
        this.enumeration.add(enumeration);
        return this;
    }

    public List<String> getEnumeration() {
        return new ArrayList<>(enumeration);
    }

    public String print() {
        String answer = "enum " + getName() + " {\n";
        for(String item: getEnumeration()) {
            answer += "  " + item + "\n";
        }
        answer += "}\n\n";
        return answer;
    }
}
