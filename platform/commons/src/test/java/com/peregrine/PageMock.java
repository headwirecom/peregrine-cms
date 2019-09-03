package com.peregrine;

import static com.peregrine.commons.util.PerConstants.*;
import static org.mockito.Mockito.when;

public final class PageMock extends ResourceMock {

    private final ResourceMock content = new ResourceMock();

    public PageMock() {
        when(mock.getResourceType()).thenReturn(PAGE_PRIMARY_TYPE);
        when(mock.isResourceType(PAGE_PRIMARY_TYPE)).thenReturn(true);

        addChild(JCR_CONTENT, content);
        content.setParent(this);
    }

    @Override
    protected void setPathImpl(final String path) {
        content.setPath(path + SLASH + JCR_CONTENT);
    }

    public ResourceMock getContent() {
        return content;
    }
}
