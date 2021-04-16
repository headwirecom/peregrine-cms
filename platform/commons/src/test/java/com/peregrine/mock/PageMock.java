package com.peregrine.mock;

import javax.jcr.RepositoryException;

import static com.peregrine.commons.util.PerConstants.*;
import static org.mockito.Mockito.when;

public class PageMock extends ResourceMock {

    private final PageContentMock content;

    public PageMock(final String name) {
        super(name);
        content = new PageContentMock(concatenateToDerivedName(name, "'s ", JCR_CONTENT));
        setPrimaryType(PAGE_PRIMARY_TYPE);
        setResourceType(PAGE_PRIMARY_TYPE);
        addChild(JCR_CONTENT, content);
        content.setParent(this);
        try {
            when(node.canAddMixin(PER_REPLICATION)).thenReturn(true);
        } catch (final RepositoryException e) { }
    }

    public PageMock() {
        this(DEFAULT_NAME);
    }

    @Override
    protected void setPathImpl(final String path) {
        content.setPath(path + SLASH + JCR_CONTENT);
    }

    public PageContentMock getContent() {
        return content;
    }
}
