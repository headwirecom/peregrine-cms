package com.peregrine.graphql.schema.model;

import com.peregrine.graphql.schema.model.TypeModelType;

import java.util.Locale;

public abstract class AbstractTypeModel {

    private TypeModelType type;
    private String name;
    private String originalName;
    private boolean subType;

    public AbstractTypeModel(TypeModelType type, String name) {
        if(name == null) {
            throw new IllegalArgumentException("Type Name must not be null");
        }
        this.type = type;
        this.originalName = name;
        this.name = convertName(name);
        this.subType = false;
    }

    public TypeModelType getType() {
        return type;
    }

    public boolean isSubType() {
        return subType;
    }

    public AbstractTypeModel setSubType(boolean subType) {
        this.subType = subType;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String updateName(String newName) {
        this.name = convertName(newName);
        return this.name;
    }

    public static String convertName(String name) {
        String[] tokens = name.split("-");
        String answer = "";
        int count = 0;
        for(String token: tokens) {
            if(token != null && token.length() > 0) {
                if(count == 0) {
                    answer = token;
                } else {
                    answer += token.substring(0, 1).toUpperCase(Locale.ROOT);
                    if(token.length() > 1) {
                        answer += token.substring(1);
                    }
                }
                count++;
            }
        }
        return answer;
    }
}
