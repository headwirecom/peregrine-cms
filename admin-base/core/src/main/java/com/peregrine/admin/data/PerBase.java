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
    /** @return Underlying resource which is never null **/
    public Resource getResource();
    /** @return Path of the underlying resource **/
    public String getPath();
    /** @return Name of the underlying resource **/
    public String getName();
    /** @return Last Modified Date of the resource if set otherwise null **/
    public Calendar getLastModified();
    /** @return User who Last Modified the resource if set otherwise null **/
    public String getLastModifiedBy();
    /** @return If the page has a content node **/
    public boolean hasContent();
    /** @return If the page is valid **/
    public boolean isValid();
    /** @return The Content Resource node if available otherwise null **/
    public Resource getContentResource();
    /** @eturn Value Map of the Content Resource if the page has a content **/
    public ValueMap getProperties();
    /** @eturn Modifiable Value Map of the Content Resource if the page has a content **/
    public ModifiableValueMap getModifiableProperties();

    /**
     * Provide Content Property
     * @param propertyName Name of the Property
     * @param type Response Type
     * @return Content Property Value converted to the given type or null if not found or could not convert
     */
    public <T> T getContentProperty(String propertyName, Class<T> type);

    /**
     * Provide Content Property
     * @param propertyName Name of the Property
     * @param defaultValue Value to be returned if not found and cannot be null
     * @return The content property of the type of the default value or if not found the default value instead
     */
    public <T> T getContentProperty(String propertyName, T defaultValue);
}
