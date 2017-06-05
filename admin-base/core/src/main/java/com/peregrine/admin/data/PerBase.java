package com.peregrine.admin.data;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Calendar;

/**
 * Created by schaefa on 6/4/17.
 */
public interface PerBase
    extends Adaptable
{
    public Resource getResource();
    public String getPath();
    public String getName();
    public Calendar getLastModified();
    public String getLastModifiedBy();
    public boolean hasContent();
    public boolean isValid();
    public Resource getContentResource();
    public ValueMap getProperties();
    public ModifiableValueMap getModifiableProperties();
    public <T> T getContentProperty(String propertyName, Class<T> type);
    public <T> T getContentProperty(String propertyName, T defaultValue);
}
