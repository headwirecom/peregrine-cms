package com.peregrine.sitemap.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peregrine.sitemap.*;
import org.apache.sling.api.resource.LoginException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.ResourceMock;

import junitx.util.PrivateAccessor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapFilesCacheImplTest extends SlingResourcesTest {

    private static final String LOCATION = "/var/sitemaps/files";
    private static final String VALUE = "<xml />";

    private final SiteMapFilesCacheImpl model = new SiteMapFilesCacheImpl();
    private final ResourceMock cache = new ResourceMock();
    private final List<SiteMapEntry> entries = new LinkedList<>();

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

    @Mock
    private SiteMapExtractor extractor;

    @Mock
    private SiteMapConfiguration siteMapConfiguration;

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

        cache.setPath(LOCATION + page.getPath());
        init(cache);

        when(siteMapExtractorsContainer.findFirstFor(page)).thenReturn(extractor);
        when(extractor.getConfiguration()).thenReturn(siteMapConfiguration);
    }

    private SiteMapEntry createEntry(final int size) {
        final SiteMapEntry result = new SiteMapEntry(page.getPath());
        when(siteMapBuilder.getSize(result)).thenReturn(size);
        return result;
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

    @Test
    public void get_cacheExists() {
        when(structureCache.get(page)).thenReturn(Collections.emptyList());
        assertNull(model.get(page, 0));
        cache.putProperty("0", VALUE);
        assertEquals(VALUE, model.get(page, 0));

        when(structureCache.get(page)).thenReturn(null);
        assertEquals(VALUE, model.get(page, 0));
        when(siteMapExtractorsContainer.findFirstFor(page)).thenReturn(extractor);
        assertEquals(VALUE, model.get(page, 0));
        when(structureCache.get(page)).thenReturn(Collections.emptyList());
        assertEquals(VALUE, model.get(page, 0));
    }

    @Test
    public void get_handleNullEntriesAndExtractor() {
        when(structureCache.get(page)).thenReturn(null);
        assertNull(model.get(page, 0));
        when(siteMapExtractorsContainer.findFirstFor(page)).thenReturn(null);
        assertNull(model.get(page, 0));
        when(structureCache.get(page)).thenReturn(Collections.emptyList());
        assertNull(model.get(page, 0));
    }

    @Test
    public void splitEntries() {
        when(config.maxEntriesCount()).thenReturn(2);
        when(config.maxFileSize()).thenReturn(10);
        model.activate(config);
        when(structureCache.get(page)).thenReturn(entries);
        entries.add(createEntry(10));
        entries.add(createEntry(1));
        entries.add(createEntry(1));
        entries.add(createEntry(1));
        assertNull(model.get(page, 0));
    }

}