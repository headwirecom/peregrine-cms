package com.peregrine.admin.data.impl;

import com.peregrine.admin.data.PerBase;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Calendar;

import static com.peregrine.admin.util.JcrUtil.JCR_CONTENT;
import static com.peregrine.admin.util.JcrUtil.JCR_LAST_MODIFIED;
import static com.peregrine.admin.util.JcrUtil.JCR_LAST_MODIFIED_BY;

/**
 * Created by schaefa on 6/4/17.
 */
public abstract class PerBaseImpl
    implements PerBase
{
    private Resource resource;
    private Resource jcrContent;

    public PerBaseImpl(Resource resource) {
        if(resource == null) {
            throw new IllegalArgumentException("Resource must be provided");
        }
        this.resource = resource;
        jcrContent = getContentResource();
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public String getPath() {
        return resource.getPath();
    }

    @Override
    public String getName() {
        return resource.getName();
    }

    @Override
    public Calendar getLastModified() {
        return getContentProperty(JCR_LAST_MODIFIED, Calendar.class);
    }

    @Override
    public String getLastModifiedBy() {
        return getContentProperty(JCR_LAST_MODIFIED_BY, String.class);
    }

    @Override
    public boolean hasContent() {
        return getContentResource() != null;
    }

    @Override
    public boolean isValid() {
        return hasContent();
    }

    @Override
    public Resource getContentResource() {
        if(jcrContent == null) {
            jcrContent = resource.getChild(JCR_CONTENT);
        }
        return jcrContent;
    }

    @Override
    public ValueMap getProperties() {
        Resource content = getContentResource();
        return content != null ?
            content.getValueMap() :
            null;
    }

    @Override
    public ModifiableValueMap getModifiableProperties() {
        Resource content = getContentResource();
        return content != null ?
            content.adaptTo(ModifiableValueMap.class) :
            null;
    }

    <T> T getContentProperty(String propertyName, Class<T> type) {
        T answer = null;
        ValueMap properties = getProperties();
        if(properties != null) {
            answer = properties.get(JCR_LAST_MODIFIED_BY, type);
        }
        return answer;
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if(type.equals(Resource.class)) {
            return (AdapterType) resource;
        } else {
            return null;
        }
    }
}
