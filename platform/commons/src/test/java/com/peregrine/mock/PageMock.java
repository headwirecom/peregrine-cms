package com.peregrine.mock;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.mockito.Mockito.when;

public class PageMock extends ResourceMock {

    private final ResourceMock content;

    public PageMock(final String name) {
        super(name);
        content = new ResourceMock(concatenateToDerivedName(name, "'s ", JCR_CONTENT));
        when(mock.getResourceType()).thenReturn(PAGE_PRIMARY_TYPE);
        when(mock.isResourceType(PAGE_PRIMARY_TYPE)).thenReturn(true);

        addChild(JCR_CONTENT, content);
        content.setParent(this);
    }

    public PageMock() {
        this(DEFAULT_NAME);
    }

    @Override
    protected void setPathImpl(final String path) {
        content.setPath(path + SLASH + JCR_CONTENT);
    }

    public ResourceMock getContent() {
        return content;
    }
}
