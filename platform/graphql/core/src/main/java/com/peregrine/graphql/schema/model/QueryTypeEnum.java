package com.peregrine.graphql.schema.model;

import static com.peregrine.graphql.schema.GraphQLConstants.ITEMS_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.ITEM_FIELD_NAME;

public enum QueryTypeEnum {
    ObjectList(true), ComponentList(true), ObjectByPath(false), ComponentByPath(true), ComponentByField(true), Unknown(false);

    boolean multiple;
    QueryTypeEnum(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isMultiple() { return multiple; }
}
