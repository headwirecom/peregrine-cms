package com.peregrine.admin.sitemap;

import com.peregrine.commons.util.PerConstants;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;

import java.util.Calendar;
import java.util.Date;

public final class Page extends ResourceWrapper {

    private final ValueMap properties;
    private final Resource content;
    private final ValueMap contentProperties;

    public Page(final Resource resource) {
        super(resource);
        properties = resource.getValueMap();
        content = resource.getChild(PerConstants.JCR_CONTENT);
        contentProperties = content == null ? null : content.getValueMap();
    }

    public boolean hasContent() {
        return content != null;
    }

    public boolean containsProperty(final String name) {
        if (contentProperties != null && contentProperties.containsKey(name)) {
            return true;
        }

        return properties.containsKey(name);
    }

    public Object getProperty(final String name) {
        if (contentProperties != null && contentProperties.containsKey(name)) {
            return contentProperties.get(name);
        }

        return properties.get(name);
    }

    public <Type> Type getProperty(final String name, final Class<Type> type) {
        final Object value = getProperty(name);
        return value == null ? null : type.cast(value);
    }

    public Calendar getLastModified() {
        final Calendar calendar = getProperty(JcrConstants.JCR_LASTMODIFIED, Calendar.class);
        return calendar == null ? getProperty(JcrConstants.JCR_CREATED, Calendar.class) : calendar;
    }

    public Date getLastModifiedDate() {
        final Calendar calendar = getLastModified();
        return calendar == null ? null : calendar.getTime();
    }
}
