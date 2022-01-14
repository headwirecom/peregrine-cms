package com.peregrine.graphql.schema.model;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.graphql.schema.GraphQLConstants.BY_PATH_MODEL_SUFFIX;
import static com.peregrine.graphql.schema.GraphQLConstants.BY_PATH_SUFFIX;
import static com.peregrine.graphql.schema.GraphQLConstants.ID_TYPE;
import static com.peregrine.graphql.schema.GraphQLConstants.ITEMS_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.ITEM_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.LIST_MODEL_SUFFIX;
import static com.peregrine.graphql.schema.GraphQLConstants.LIST_SUFFIX;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.TYPE_NAME;

public class TypeModel extends AbstractTypeModel {

    private String path;
    private List<TypeFieldModel> fields = new ArrayList<>();

    public TypeModel(String name, String path) {
        super(name);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public TypeModel addField(TypeFieldModel field) {
        fields.add(field);
        return this;
    }

    public List<TypeFieldModel> getFields() {
        return fields;
    }

    public String print() {
        String answer = TYPE_NAME + " " + getName() + " {\n" +
            "  " + PATH_FIELD_NAME + ": " + ID_TYPE + "\n";
        for(TypeFieldModel field: getFields()) {
            answer += "  " + field.getName() + ": ";
            if(field.isArray()) {
                answer += "[";
            }
            answer += field.getType().name();
            if(field.isNotNull()) {
                answer += "!";
            }
            if(field.isArray()) {
                answer += "]";
            }
            answer += "\n";
        }
        answer += "}\n\n";
        answer += TYPE_NAME + " " + getName() + BY_PATH_MODEL_SUFFIX + " {\n" +
            "  " + ITEM_FIELD_NAME + ": [" + getName() + "]!\n" +
            "}\n\n";
        answer += TYPE_NAME + " " + getName() + LIST_MODEL_SUFFIX + " {\n" +
            "  " + ITEMS_FIELD_NAME + ": [" + getName() + "]!\n" +
            "}\n\n";
        return answer;
    }
}
