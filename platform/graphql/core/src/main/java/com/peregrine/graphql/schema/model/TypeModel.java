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
    private boolean subType;

    public TypeModel(int type, String name, String path) {
        super(type, name);
        this.path = path;
        this.subType = false;
    }

    public String getPath() {
        return path;
    }

    public TypeModel addField(TypeFieldModel field) {
        fields.add(field);
        // Check for Custom Types and update their name
        TypeModel subType = field.getCustomType();
        if(subType != null) {
            subType.updateName(
                getName() + '_' + subType.getName() + (field.isArray() ? "Items" : "Item")
            );
        }
        return this;
    }

    public TypeFieldModel getField(String name) {
        return fields.stream().filter(i -> i.getName().equals(name)).findFirst().orElse(null);
    }

    public boolean isSubType() {
        return subType;
    }

    public TypeModel setSubType(boolean subType) {
        this.subType = subType;
        return this;
    }

    public List<TypeFieldModel> getFields() {
        return fields;
    }

    public String print() {
        String answer = TYPE_NAME + " " + getName() + " {\n";
        if(!isSubType()) {
            answer += "  " + PATH_FIELD_NAME + ": " + ID_TYPE + "\n";
        }
        for(TypeFieldModel field: getFields()) {
            answer += "  " + field.getName() + ": ";
            if(field.isArray()) {
                answer += "[";
            }
            TypeModel customType = field.getCustomType();
            if(customType != null) {
                answer += customType.getName();
            } else {
                answer += field.getType().name();
            }
            if(field.isNotNull()) {
                answer += "!";
            }
            if(field.isArray()) {
                answer += "]";
            }
            answer += "\n";
        }
        answer += "}\n\n";
        if(!isSubType()) {
            answer += TYPE_NAME + " " + getName() + BY_PATH_MODEL_SUFFIX + " {\n" +
                "  " + ITEM_FIELD_NAME + ": [" + getName() + "]!\n" +
                "}\n\n";
            answer += TYPE_NAME + " " + getName() + LIST_MODEL_SUFFIX + " {\n" +
                "  " + ITEMS_FIELD_NAME + ": [" + getName() + "]!\n" +
                "}\n\n";
        }
        return answer;
    }
}
