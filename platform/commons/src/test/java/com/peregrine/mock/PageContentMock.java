package com.peregrine.mock;

import javax.jcr.RepositoryException;

import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATION;
import static org.mockito.Mockito.when;

public class PageContentMock extends ResourceMock {

    public PageContentMock(final String name) {
        super(name);
        setPrimaryType(PAGE_CONTENT_TYPE);
        setResourceType(PAGE_CONTENT_TYPE);
        try {
            when(node.canAddMixin(PER_REPLICATION)).thenReturn(true);
        } catch (final RepositoryException e) { }
    }

}
