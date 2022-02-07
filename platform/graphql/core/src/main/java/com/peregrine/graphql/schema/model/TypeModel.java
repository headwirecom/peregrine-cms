package com.peregrine.graphql.schema.model;

import com.peregrine.graphql.schema.model.TypeModelType.Variable;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.graphql.schema.GraphQLConstants.SINGLE_ITEM_RESULT_SET;
import static com.peregrine.graphql.schema.GraphQLConstants.ITEMS_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.ITEM_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.MULTIPLE_ITEMS_RESULT_SET;
import static com.peregrine.graphql.schema.GraphQLConstants.TYPE_NAME;

public class TypeModel extends AbstractTypeModel {

    private String path;
    private List<TypeFieldModel> fields = new ArrayList<>();

    public TypeModel(TypeModelType type, String name, String path) {
        super(type, name);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public TypeModel addField(TypeFieldModel field) {
        fields.add(field);
        // Check for Custom Types and update their name
        AbstractTypeModel subType = field.getCustomType();
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

    public List<TypeFieldModel> getFields() {
        return fields;
    }

    public String print() {
        String answer = TYPE_NAME + " " + getName() + " {\n";
        if(!isSubType()) {
            for(Variable systemVariable: getType().getSystemVariables()) {
                answer += "  " + systemVariable.getName() + ": " + systemVariable.getType() + "\n";
            }
        }
        for(TypeFieldModel field: getFields()) {
            answer += "  " + field.getName() + ": ";
            if(field.isArray()) {
                answer += "[";
            }
            AbstractTypeModel customType = field.getCustomType();
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
            answer += TYPE_NAME + " " + getName() + SINGLE_ITEM_RESULT_SET + " {\n" +
                "  " + ITEM_FIELD_NAME + ": " + getName() + "\n" +
                "}\n\n";
            answer += TYPE_NAME + " " + getName() + MULTIPLE_ITEMS_RESULT_SET + " {\n" +
                "  " + ITEMS_FIELD_NAME + ": [" + getName() + "]\n" +
                "}\n\n";
        }
        return answer;
    }
}
