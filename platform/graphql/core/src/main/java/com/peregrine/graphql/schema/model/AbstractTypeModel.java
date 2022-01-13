package com.peregrine.graphql.schema.model;

import java.util.Locale;

public abstract class AbstractTypeModel {

    private String name;
    private String originalName;

    public AbstractTypeModel(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Type Name must not be null");
        }
        this.originalName = name;
        this.name = convertName(name);
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
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
