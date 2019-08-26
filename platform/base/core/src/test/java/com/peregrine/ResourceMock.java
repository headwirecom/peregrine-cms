package com.peregrine;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceMock extends ResourceWrapper {

    protected final Resource mock;

    public ResourceMock() {
        super(mock(Resource.class));
        mock = getResource();
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

    public final void addChild(final String name, final Resource child) {
        when(mock.getChild(name)).thenReturn(child);
    }

}
