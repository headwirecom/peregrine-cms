package com.peregrine.admin.replication;

import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by schaefa on 5/25/17.
 */
public class Reference {
    private Resource resource;
    private Resource propertyResource;
    private String propertyName;

    public Reference(Resource resource, String propertyName, Resource propertyResource) {
        this.resource = resource;
        this.propertyName = propertyName;
        this.propertyResource = propertyResource;
    }

    public Resource getResource() {
        return resource;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Resource getPropertyResource() {
        return propertyResource;
    }
}
