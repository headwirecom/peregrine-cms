package com.peregrine.graphql.schema.json;

import com.peregrine.graphql.schema.model.ScalarEnum;

public interface DialogJsonConstants {

    int DIALOG_TYPE = 20;

    String DIALOG_NODE_NAME = "dialog.json";
    String DIALOG_GROUPS_PROPERTY = "groups";
    String DIALOG_LEGEND_PROPERTY = "legend";
    String DIALOG_CONTENT_VALUE = "content";
    String DIALOG_FIELDS_PROPERTY = "fields";
    String DIALOG_FIELD_TYPE = "type";
    String DIALOG_FIELD_INPUT_TYPE = "inputType";
    String DIALOG_FIELD_MODEL = "model";
//    String DIALOG_FIELD_PLACEHOLDER = "placeholder";
    String DIALOG_FIELD_VALUES = "values";
    String DIALOG_FIELD_VALUES_VALUE = "value";

    enum TYPE {
        input, collection, material_textarea,
        material_radios, pathbrowser, object_definition_reference,
        material_select, materialswitch, texteditor,
        unknown;
    }

    enum INPUT_TYPE {
        string(ScalarEnum.String),
        number(ScalarEnum.Int),
        material_select(ScalarEnum.String),
        material_range(ScalarEnum.Int),
        material_radios(ScalarEnum.Int),
        unknown(ScalarEnum.String);

        ScalarEnum scalar;
        INPUT_TYPE(ScalarEnum scalar) {
            this.scalar = scalar;
        }

        public ScalarEnum getScalar() {
            return scalar;
        }
    }
}
