package com.peregrine.sitemap;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class SiteMapEntry {

    private final Map<String, Object> properties = new LinkedHashMap<>();

    public String getUrl() {
        return getProperty(SiteMapConstants.LOC, String.class);
    }

    public void setUrl(final String url) {
        putProperty(SiteMapConstants.LOC, url);
    }

    public Object putProperty(final String name, final Object value) {
        if (isBlank(name)) {
            return null;
        }

        if (isNull(value)) {
            return properties.remove(name);
        }

        return properties.put(name, value);
    }

    public Object putProperty(final String name, final String value) {
        if (isBlank(value)) {
            return putProperty(name, null);
        }

        return putProperty(name, (Object)value);
    }

    private <Type> Type getProperty(final String name, final Class<? extends Type> type) {
        final Object value = properties.get(name);
        if (isNull(value)) {
            return null;
        }

        if (type.isAssignableFrom(value.getClass())) {
            return (Type) value;
        }

        return null;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

}
