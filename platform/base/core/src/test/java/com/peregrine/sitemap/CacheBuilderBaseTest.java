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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.mock.MockTools.setParentChildRelationships;
import static com.peregrine.mock.MockTools.setPaths;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public final class CacheBuilderBaseTest extends SlingResourcesTest {

    private final ResourceMock var = repo.getVar();
    private final ResourceMock rootCache = new ResourceMock("Cache Root");
    private final ResourceMock parentCache = new ResourceMock("Cache Parent");
    private final PageMock pageCache = new PageMock("Cache Page");
    private final ResourceMock contentCache = pageCache.getContent();
    private final ResourceMock resourceCache = new ResourceMock("Cache Resource");

    {
        setPaths(var.getPath() + PAGE_PATH, rootCache, parentCache, pageCache);
        resourceCache.setPath(contentCache.getPath() + SLASH + NN_RESOURCE);
        setParentChildRelationships(var, rootCache, parentCache, pageCache);
        setParentChildRelationships(contentCache, resourceCache);
        init(rootCache);
        init(parentCache);
        init(pageCache);
        init(resourceCache);
    }

    private final CacheBuilderBase model = Mockito.spy(new CacheBuilderBase() {

        {
            setLocation(var.getPath());
        }

        @Override
        protected ResourceResolver getServiceResourceResolver() {
            return resourceResolver;
        }

        @Override
        protected Resource buildCache(final Resource rootPage, final Resource cache) {
            buildCacheCalled.put(rootPage, cache);
            return cache;
        }

        @Override
        protected void rebuildImpl(final String rootPagePath) {
            rebuildImplCalled.add(rootPagePath);
        }

    });

    private final Map<Resource, Resource> buildCacheCalled = new HashMap<>();
    private final Set<String> rebuildImplCalled = new HashSet<>();

    private void verifyCommits(final int wantedNumberOfInvocations) {
        try {
            verify(resourceResolver, times(wantedNumberOfInvocations)).commit();
        } catch (final PersistenceException e) {
        }
    }

    private void disableResolution(final Resource resource) {
        when(resourceResolver.getResource(resource.getPath())).thenReturn(null);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void rebuilds_throwLoginException() throws LoginException {
        when(model.getServiceResourceResolver()).thenThrow(LoginException.class);

        model.rebuild(StringUtils.EMPTY);
        verifyCommits(0);

        model.rebuildAll();
        verifyCommits(0);

        model.buildCache(StringUtils.EMPTY);
        verifyCommits(0);
    }

    @Test
    public void throwPersistenceExceptionQuickly() throws PersistenceException {
        doThrow(PersistenceException.class).when(resourceResolver).commit();
        int invocationsCount = 1;

        disableResolution(parentCache);
        model.getCache(resourceResolver, parent);
        verifyCommits(invocationsCount++);

        model.rebuild(StringUtils.EMPTY);
        verifyCommits(invocationsCount++);

        model.rebuildAll();
        verifyCommits(invocationsCount++);

        model.buildCache(pageCache.getPath());
        verifyCommits(invocationsCount++);
    }

    @Test
    public void getCache_existsAlready() {
        final Resource cache = model.getCache(resourceResolver, parent);
        assertEquals(parentCache, cache);
        assertTrue(buildCacheCalled.isEmpty());
    }

    @Test
    public void getCache_doesNotExistYet() {
        disableResolution(parentCache);
        final Resource cache = model.getCache(resourceResolver, parent);
        assertNotEquals(parentCache, cache);
        verifyCommits(1);
        assertTrue(buildCacheCalled.containsKey(parent));
        assertEquals(cache, buildCacheCalled.get(parent));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void getCache_doesNotExistYet_handlePersistenceException() throws PersistenceException {
        disableResolution(parentCache);
        when(resourceResolver.create(any(), any(), any())).thenThrow(PersistenceException.class);
        final Resource cache = model.getCache(resourceResolver, parent);
        assertNull(cache);
        verifyCommits(1);
        assertTrue(buildCacheCalled.isEmpty());
    }

    @Test
    public void rebuild_cacheIsNull() {
        model.rebuild(resource.getPath() + SLASH + "not-cached");
        verifyCommits(1);
        assertTrue(rebuildImplCalled.contains(resource.getPath()));
        assertTrue(rebuildImplCalled.contains(jcrContent.getPath()));
        assertTrue(rebuildImplCalled.contains(page.getPath()));
        assertTrue(rebuildImplCalled.contains(parent.getPath()));
        assertTrue(rebuildImplCalled.contains(contentRoot.getPath()));
    }

    @Test
    public void rebuild_rootPageIsNull() {
        disableResolution(resource);
        model.rebuild(resource.getPath());
        verifyCommits(1);
        assertTrue(rebuildImplCalled.contains(resource.getPath()));
        assertTrue(rebuildImplCalled.contains(jcrContent.getPath()));
        assertTrue(rebuildImplCalled.contains(page.getPath()));
        assertTrue(rebuildImplCalled.contains(parent.getPath()));
        assertTrue(rebuildImplCalled.contains(contentRoot.getPath()));
    }

    @Test
    public void rebuild() {
        model.rebuild(jcrContent.getPath());
        verifyCommits(1);
        assertTrue(rebuildImplCalled.contains(jcrContent.getPath()));
        assertTrue(rebuildImplCalled.contains(page.getPath()));
        assertTrue(rebuildImplCalled.contains(parent.getPath()));
        assertTrue(rebuildImplCalled.contains(contentRoot.getPath()));
    }

    @Test
    public void buildCache() {
        final Resource cache = model.buildCache(jcrContent.getPath());
        verifyCommits(1);
        assertEquals(contentCache, cache);
        assertEquals(contentCache, buildCacheCalled.get(jcrContent));
    }

    @Test
    public void rebuildAll_nothingCachedYet() {
        disableResolution(var);
        model.rebuildAll();
        verifyCommits(1);
        assertTrue(rebuildImplCalled.isEmpty());
    }

    @Test
    public void rebuildAll() {
        model.rebuildAll();
        verifyCommits(1);
        assertTrue(rebuildImplCalled.contains(resource.getPath()));
        assertTrue(rebuildImplCalled.contains(jcrContent.getPath()));
        assertTrue(rebuildImplCalled.contains(page.getPath()));
        assertTrue(rebuildImplCalled.contains(parent.getPath()));
        assertTrue(rebuildImplCalled.contains(contentRoot.getPath()));
    }

}