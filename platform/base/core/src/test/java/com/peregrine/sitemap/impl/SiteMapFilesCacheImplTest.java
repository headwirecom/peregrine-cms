package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.sitemap.ResourceResolverFactoryProxy;
import com.peregrine.sitemap.SiteMapExtractorsContainer;
import com.peregrine.sitemap.SiteMapFileContentBuilder;
import com.peregrine.sitemap.SiteMapStructureCache;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapFilesCacheImplTest extends SlingResourcesTest {

    private static final String LOCATION = "/var/sitemaps/files";

    private final SiteMapFilesCacheImpl model = new SiteMapFilesCacheImpl();

    @Mock
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Mock
    private SiteMapStructureCache structureCache;

    @Mock
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    @Mock
    private SiteMapFileContentBuilder siteMapBuilder;

    @Mock
    private SiteMapFilesCacheImplConfig config;

    @Before
    public void setUp() throws NoSuchFieldException, LoginException {
        PrivateAccessor.setField(model, "resourceResolverFactory", resourceResolverFactory);
        PrivateAccessor.setField(model, "structureCache", structureCache);
        PrivateAccessor.setField(model, "siteMapExtractorsContainer", siteMapExtractorsContainer);
        PrivateAccessor.setField(model, "siteMapBuilder", siteMapBuilder);

        when(config.location()).thenReturn(LOCATION);
        when(config.maxEntriesCount()).thenReturn(0);
        when(config.maxFileSize()).thenReturn(0);

        when(resourceResolverFactory.getServiceResourceResolver()).thenReturn(resourceResolver);

        model.activate(config);
    }

    @Test
    public void deactivate() {
        verify(structureCache, times(1)).addRefreshListener(model);
        model.deactivate();
        verify(structureCache, times(1)).removeRefreshListener(model);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void get_throwLoginException() throws LoginException {
        when(resourceResolverFactory.getServiceResourceResolver()).thenThrow(LoginException.class);
        assertNull(model.get(page, 0));
    }

}