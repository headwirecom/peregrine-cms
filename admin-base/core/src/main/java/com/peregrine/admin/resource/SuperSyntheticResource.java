package com.peregrine.admin.resource;

import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SuperSyntheticResource
    extends SyntheticResource
{
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String resourceSuperType;
    public SuperSyntheticResource(ResourceResolver resourceResolver, String path, String resourceType, String resourceSuperType) {
        super(resourceResolver, path, resourceType);
        this.resourceSuperType = resourceSuperType;
    }

    public SuperSyntheticResource(ResourceResolver resourceResolver, ResourceMetadata rm, String resourceType, String resourceSuperType) {
        super(resourceResolver, rm, resourceType);
        if(rm.getParameterMap().isEmpty()) {
            logger.warn("Resource: '{}' does not contain a parameter map", rm.getResolutionPath());
        }
        if(getResourceMetadata().getParameterMap().isEmpty()) {
            logger.warn("Resource: '{}' does not return a parameter map", rm.getResolutionPath());
        }
        this.resourceSuperType = resourceSuperType;
    }

    @Override
    public String getResourceSuperType() {
        return resourceSuperType;
    }

    @Override
    public ValueMap getValueMap() {
        Map<String, String> map = getResourceMetadata().getParameterMap();
        if(map.isEmpty()) {
            logger.warn("Synthetic Resource: '{}' does not return a parameter map", getResourceMetadata().getResolutionPath());
        }
        Map<String, Object> parameters = new HashMap<>(map);
        return new ValueMapDecorator(parameters);
    }
}
