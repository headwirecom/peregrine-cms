package com.peregrine.nodetypes.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import java.util.Collections;
import java.util.List;

/**
 * Created by rr on 12/2/2016.
 */
public class AbstractComponent implements IComponent {

    private final Resource resource;

    public AbstractComponent(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public String getPath() {
        return resource.getPath();
    }

    public String getComponent() {
        String resourceType = resource.getResourceType();
        if (resourceType != null) {
            if(resourceType.startsWith("/")) {
                resourceType = StringUtils.substringAfter(resourceType, "/");
            }
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, resourceType.replaceAll("/", "-"));
        } else {
            return "";
        }
    }

    @Override
    @JsonIgnore
    public List<IComponent> getChildren() {
        return Collections.emptyList();
    }

}