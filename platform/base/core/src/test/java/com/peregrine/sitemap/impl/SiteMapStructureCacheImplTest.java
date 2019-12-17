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

import java.util.List;

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

    private ResourceMock addEntryCache() {
        final ResourceMock result = new ResourceMock();
        final ResourceMock content = cache.getContent();
        final String path = content.getPath();
        final String name = String.valueOf(content.getChildrenCount());
        result.setPath(path + SLASH + name);
        content.addChild(result);
        init(result);
        return result;
    }

    @Test
    public void get_cacheExists() {
        addEntryCache();
        addEntryCache();
        addEntryCache();
        final List<SiteMapEntry> entries = model.get(page);
        assertNotNull(entries);
        assertEquals(3, entries.size());
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