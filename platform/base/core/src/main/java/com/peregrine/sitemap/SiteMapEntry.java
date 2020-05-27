package com.peregrine.sitemap;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class SiteMapEntry {

    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final String path;

    public SiteMapEntry(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

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
            return putProperty(name, (Object) null);
        }

        return putProperty(name, (Object) value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    @SuppressWarnings("unchecked")
    public <Type> Type getProperty(final String name, final Class<? extends Type> type) {
        final Object value = getProperty(name);
        if (isNull(value)) {
            return null;
        }

        if (type.isAssignableFrom(value.getClass())) {
            return (Type) value;
        }

        return null;
    }

    public String getLastModified() {
        return getProperty(SiteMapConstants.LAST_MOD, String.class);
    }

    public void setLastModified(final String lastModified) {
        putProperty(SiteMapConstants.LAST_MOD, lastModified);
    }

    public <Parameter> Parameter walk(final Visitor<Parameter> visitor, final Parameter parameter) {
        return walk(visitor, parameter, null);
    }

    public <Parameter> Parameter walk(final Visitor<Parameter> visitor, final Parameter parameter, final String rootName) {
        return walk(visitor, parameter, rootName, properties);
    }

    private <Parameter> Parameter walk(
            final Visitor<Parameter> visitor,
            final Parameter parameter,
            final String mapName,
            final Map<String, Object> properties) {
        final Map<String, String> props = new HashMap<>();
        final Map<String, Map<String, Object>> children = new HashMap<>();
        for (final Map.Entry<String, Object> e : properties.entrySet()) {
            final Object value = e.getValue();
            final String key = e.getKey();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked") final Map<String, Object> map = (Map<String, Object>) value;
                children.put(key, map);
            } else {
                final String string = String.valueOf(value);
                props.put(key, string);
            }
        }

        Parameter result = visitor.visit(mapName, Collections.unmodifiableMap(props), parameter);
        for (final Map.Entry<String, String> e : props.entrySet()) {
            result = visitor.visit(e.getKey(), e.getValue(), result);
        }

        for (final Map.Entry<String, Map<String, Object>> e : children.entrySet()) {
            result = walk(visitor, result, e.getKey(), e.getValue());
        }

        return visitor.endVisit(mapName, result);
    }

    public interface Visitor<Parameter> {

        Parameter visit(String mapName, Map<String, String> properties, Parameter parameter);

        Parameter visit(String propertyName, String propertyValue, Parameter parameter);

        Parameter endVisit(String mapName, Parameter parameter);

    }

}
