package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.ResourceMock;
import com.peregrine.sitemap.*;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapStructureCacheImplTest extends SlingResourcesTest {

    private static final String LOCATION = "/var/sitemaps/structure";
    private static final String X = "x";
    private static final String Y = "y";

    private final SiteMapStructureCacheImpl model = new SiteMapStructureCacheImpl();
    private final PageMock cache = new PageMock();

    @Mock
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Mock
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    @Mock
    private SiteMapConfigurationsContainer siteMapConfigurationsContainer;

    @Mock
    private SiteMapStructureCacheImplConfig config;

    @Mock
    private SiteMapExtractor extractor;

    @Mock
    private SiteMapConfiguration siteMapConfiguration;

    @Before
    public void setUp() throws NoSuchFieldException, LoginException {
        PrivateAccessor.setField(model, "resourceResolverFactory", resourceResolverFactory);
        PrivateAccessor.setField(model, "siteMapExtractorsContainer", siteMapExtractorsContainer);
        PrivateAccessor.setField(model, "siteMapConfigurationsContainer", siteMapConfigurationsContainer);

        when(config.debounceInterval()).thenReturn(0);
        when(config.location()).thenReturn(LOCATION);

        when(resourceResolverFactory.getServiceResourceResolver()).thenReturn(resourceResolver);

        model.activate(config);

        cache.setPath(LOCATION + page.getPath());
        init(cache);

        when(siteMapExtractorsContainer.findFirstFor(page)).thenReturn(extractor);
        when(extractor.getConfiguration()).thenReturn(siteMapConfiguration);
    }

    @Test
    public void deactivate() {
        model.deactivate();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void get_throwLoginException() throws LoginException {
        when(resourceResolverFactory.getServiceResourceResolver()).thenThrow(LoginException.class);
        assertNull(model.get(page));
    }

    private ResourceMock addEntryCache(final ResourceMock parent) {
        final String name = String.valueOf(parent.getChildrenCount());
        final ResourceMock result = parent.createChild(name);
        return init(result);
    }

    private ResourceMock addEntryCache() {
        return addEntryCache(cache.getContent());
    }

    private ResourceMock addEntryCache(final String propertyName, final Object propertyValue) {
        final ResourceMock result = addEntryCache();
        result.putProperty(propertyName, propertyValue);
        return result;
    }

    @Test
    public void get_cacheExists() {
        final ResourceMock _0 = addEntryCache(X, 0);
        final ResourceMock child = _0.createChild(Y);
        child.putProperty(X, false);

        final List<ResourceMock> items = new ArrayList<>();
        items.add(_0);
        items.add(addEntryCache(X, true));
        items.add(addEntryCache(X, X));

        final List<SiteMapEntry> entries = model.get(page);
        assertNotNull(entries);

        final int size = items.size();
        assertEquals(size, entries.size());

        for (int i = 0; i < size; i++) {
            assertEquals(items.get(i).getProperty(X), entries.get(i).getProperty(X));
        }

        final SiteMapEntry entry = entries.get(0);
        final Object map = entry.getProperty(Y);
        assertNotNull(map);
        assertTrue(map instanceof Map);
        assertEquals(child.getProperty(X), ((Map)map).get(X));
    }

    @Test
    public void get_catchPersistenceException() throws PersistenceException {
        doThrow(PersistenceException.class).when(resourceResolver).commit();
        final ResourceMock content = cache.getContent();
        when(resourceResolver.getResource(content.getPath())).thenReturn(null);
        when(resourceResolver.create(eq(cache), eq(JCR_CONTENT), any())).thenAnswer(invocation -> {
            init(content);
            return content;
        });
        assertNull(model.get(page));
    }

}