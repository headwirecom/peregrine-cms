package com.peregrine.graphql.schema.util;

import com.peregrine.graphql.schema.model.ScalarEnum;
import com.peregrine.graphql.schema.model.TypeModelType.Variable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.io.InputStream;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.graphql.schema.GraphQLConstants.ID_TYPE;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_FIELD_NAME;
import static com.peregrine.graphql.schema.GraphQLConstants.PATH_PAGE_NAME;

public class Utils {

    public static final Variable PATH_SYSTEM_VARIABLE = new VariableImpl(PATH_FIELD_NAME, ID_TYPE, false);
    public static final Variable PAGE_PATH_SYSTEM_VARIABLE = new VariableImpl(PATH_PAGE_NAME, ScalarEnum.String, false);

    public static void getChildResources(Resource resource, String fileName, List<Resource> result) {
        String resourceName = resource.getName();
        if(resourceName != null && resourceName.equals(fileName)) {
            result.add(resource);
        }
        for(Resource child: resource.getChildren()) {
            getChildResources(child, fileName, result);
        }
    }

    public static InputStream getSchemaContent(Resource resource) {
        Resource jcrContent = resource != null ?
            resource.getName().equals(JCR_CONTENT) ?
                resource :
                resource.getChild(JCR_CONTENT) :
            null;
        if(jcrContent != null) {
            ValueMap properties = jcrContent.getValueMap();
            return properties.get(JCR_DATA, InputStream.class);
        } else {
            return null;
        }
    }

    public static class VariableImpl implements Variable {
        private String name;
        private String type;
        private boolean mandatory;

        public VariableImpl(String name, String type, boolean mandatory) {
            this.name = name;
            this.type = type;
            this.mandatory = mandatory;
        }

        public VariableImpl(String name, ScalarEnum type, boolean mandatory) {
            this.name = name;
            this.type = type.toString();
            this.mandatory = mandatory;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public boolean isMandatory() {
            return mandatory;
        }
    }
}
