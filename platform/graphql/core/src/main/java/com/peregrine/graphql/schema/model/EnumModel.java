package com.peregrine.graphql.schema.model;

import java.util.ArrayList;
import java.util.List;

public class EnumModel extends AbstractTypeModel {

    private List<String> enumerations;

    public EnumModel(TypeModelType type, String name) {
        super(type, name);
        this.enumerations = new ArrayList<>();
    }

    public EnumModel addEnumeration(String enumeration) {
        if(enumeration.contains("-")) {
            enumeration = enumeration.replaceAll("-", "_");
        }
        this.enumerations.add(enumeration);
        return this;
    }

    public List<String> getEnumerations() {
        return new ArrayList<>(enumerations);
    }

    public String print() {
        String answer = "enum " + getName() + " {\n";
        for(String item: getEnumerations()) {
            answer += "  " + item + "\n";
        }
        answer += "}\n\n";
        return answer;
    }
}
