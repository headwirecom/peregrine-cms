package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.ResourceMock;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public final class CacheBuilderBaseTest extends SlingResourcesTest {

    private static final String CACHE_LOCATION = "/var";

    private final ResourceMock rootCache = new ResourceMock("Cache Root");
    private final ResourceMock parentCache = new ResourceMock("Cache Parent");
    private final PageMock pageCache = new PageMock("Cache Page");
    private final ResourceMock contentCache = pageCache.getContent();
    private final ResourceMock resourceCache = new ResourceMock("Cache Resource");
    {
        setPaths(CACHE_LOCATION + PAGE_PATH, rootCache, parentCache, pageCache);
        resourceCache.setPath(contentCache.getPath() + SLASH + NN_RESOURCE);
        setParentChildRelationships(repoRoot, rootCache, parentCache, pageCache);
        setParentChildRelationships(contentCache, resourceCache);
        init(rootCache);
        init(parentCache);
        init(pageCache);
        init(resourceCache);
    }

    private final CacheBuilderBase model = Mockito.spy(new CacheBuilderBase() {

        {
            setLocation(CACHE_LOCATION);
        }

        @Override
        protected ResourceResolver getServiceResourceResolver() {
            return resourceResolver;
        }

        @Override
        protected Resource buildCache(final Resource rootPage, final Resource cache) {
            buildCacheCalled = true;
            return cache;
        }

        @Override
        protected void rebuildImpl(final String rootPagePath) { }

    });

    private boolean buildCacheCalled = false;

    @SuppressWarnings("unchecked")
	@Test
    public void rebuilds_throwLoginException() throws LoginException, PersistenceException {
        when(model.getServiceResourceResolver()).thenThrow(LoginException.class);

        model.rebuild(StringUtils.EMPTY);
        verify(resourceResolver, times(0)).commit();

        model.rebuildAll();
        verify(resourceResolver, times(0)).commit();
    }

    @Test
    public void throwPersistenceException() throws PersistenceException {
        doThrow(PersistenceException.class).when(resourceResolver).commit();
        int invocationsCount = 1;

        when(resourceResolver.getResource(parentCache.getPath())).thenReturn(null);
        model.getCache(resourceResolver, parent);
        verify(resourceResolver, times(invocationsCount++)).commit();

        model.rebuild(StringUtils.EMPTY);
        verify(resourceResolver, times(invocationsCount++)).commit();

        model.rebuildAll();
        verify(resourceResolver, times(invocationsCount++)).commit();
    }

    @Test
    public void getCache_existsAlready() {
        final Resource cache = model.getCache(resourceResolver, parent);
        assertEquals(parentCache, cache);
        assertFalse(buildCacheCalled);
    }

    @Test
    public void getCache_doesNotExistYet() throws PersistenceException {
        when(resourceResolver.getResource(parentCache.getPath())).thenReturn(null);
        final Resource cache = model.getCache(resourceResolver, parent);
        assertNotEquals(parentCache, cache);
        verify(resourceResolver, times(1)).commit();
        assertTrue(buildCacheCalled);
    }

    @Test
    public void getCache_doesNotExistYet_handlePersistenceException() throws PersistenceException {
        when(resourceResolver.getResource(parentCache.getPath())).thenReturn(null);
        when(resourceResolver.create(any(), any(), any())).thenThrow(PersistenceException.class);
        final Resource cache = model.getCache(resourceResolver, parent);
        assertNull(cache);
        verify(resourceResolver, times(1)).commit();
        assertFalse(buildCacheCalled);
    }

}