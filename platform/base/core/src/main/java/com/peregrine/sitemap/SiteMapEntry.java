package com.peregrine.sitemap;

import java.util.HashMap;
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

    public Map<String, Object> getProperties() {
        return properties;
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

    public <Parameter> void walk(final MapPropertiesVisitor<Parameter> visitor, final Parameter parameter) {
        walk(visitor, parameter, properties);
    }

    private <Parameter> void walk(
            final MapPropertiesVisitor<Parameter> visitor,
            final Parameter parameter,
            final Map<String, Object> properties) {
        final Map<String, Object> props = new HashMap<>();
        final Map<String, Map<String, Object>> children = new HashMap<>();
        for (final Map.Entry<String, Object> e : properties.entrySet()) {
            final Object value = e.getValue();
            if (value instanceof Map) {
                final Map<String, Object> map = (Map<String, Object>) value;
                children.put(e.getKey(), map);
            } else {
                props.put(e.getKey(), String.valueOf(value));
            }
        }

        visitor.visit(props, parameter);
        for (final Map.Entry<String, Map<String, Object>> e : children.entrySet()) {
            walk(visitor, visitor.visit(e.getKey(), parameter), e.getValue());
        }
    }

    public interface MapPropertiesVisitor<Parameter> {

        Parameter visit(String mapName, Parameter parameter);

        void visit(Map<String, Object> map, Parameter parameter);

    }
    }

}
