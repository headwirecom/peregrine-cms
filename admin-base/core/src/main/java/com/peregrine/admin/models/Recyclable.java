package com.peregrine.admin.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.inject.Inject;


@Model(adaptables= Resource.class)
public class Recyclable {

    @Inject private String frozenNodePath;
    @Inject private String resourcePath;
    @Inject @SlingObject private Resource resource;

    public String getFrozenNodePath() {
        return frozenNodePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public Resource getResource() {
        return resource;
    }

}
