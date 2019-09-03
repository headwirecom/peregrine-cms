package com.peregrine;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceMock extends ResourceWrapper {

    protected final Resource mock;

    protected final Map<String, Object> properties = new HashMap<>();

    private final Map<String, Resource> children = new TreeMap<>();

    public ResourceMock() {
        super(mock(Resource.class));
        mock = getResource();
        final ValueMap valueMap = new ValueMapDecorator(properties);
        when(mock.getValueMap()).thenReturn(valueMap);
    }

    public final Map<String, Object> getProperties() {
        return properties;
    }

    public final void putProperty(final String name, final Object property) {
        properties.put(name, property);
    }

    public final void setPath(final String path) {
        when(mock.getPath()).thenReturn(path);
        setPathImpl(path);
    }

    protected void setPathImpl(final String path) { }

    @Override
    public final String getName() {
        return StringUtils.substringAfterLast(getPath(), "/");
    }

    public final void setParent(final Resource parent) {
        when(mock.getParent()).thenReturn(parent);
    }

    @Override
    public final Resource getChild(final String name) {
        return children.get(name);
    }

    public final void addChild(final String name, final Resource child) {
        children.put(name, child);
    }

    public final void addChild(final Resource child) {
        addChild(child.getName(), child);
    }

    @Override
    public Iterable<Resource> getChildren() {
        return children.values();
    }

    @Override
    public Iterator<Resource> listChildren() {
        return getChildren().iterator();
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
