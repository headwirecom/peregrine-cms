package com.peregrine.graphql.schema.util;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.io.InputStream;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;

public class Utils {

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
}
