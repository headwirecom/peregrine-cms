package com.peregrine.commons.test.mock;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.mockito.Mockito.when;

public final class PageMock extends ResourceMock {

    private final ResourceMock content = new ResourceMock();

    public PageMock() {
        Mockito.when(mock.getResourceType()).thenReturn(PerConstants.PAGE_PRIMARY_TYPE);
        Mockito.when(mock.isResourceType(PerConstants.PAGE_PRIMARY_TYPE)).thenReturn(true);

        addChild(PerConstants.JCR_CONTENT, content);
        content.setParent(this);
    }

    @Override
    protected void setPathImpl(final String path) {
        content.setPath(path + PerConstants.SLASH + PerConstants.JCR_CONTENT);
    }

    public ResourceMock getContent() {
        return content;
    }
}
