package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.ResourceMock;
import com.peregrine.sitemap.*;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapStructureCacheImplTest extends SlingResourcesTest implements SiteMapStructureCache.RefreshListener {

    private static final String LOCATION = "/var/sitemaps/structure";
    private static final String X = "x";
    private static final String Y = "y";

    private final SiteMapStructureCacheImpl model = new SiteMapStructureCacheImpl();
    private final PageMock cacheParent = new PageMock();
    private final ResourceMock cache = cacheParent.getContent();
    private final Map<Resource, List<SiteMapEntry>> onCacheRefreshedMap = new HashMap<>();
    private final List<SiteMapEntry> entries = new LinkedList<>();

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

        when(siteMapConfigurationsContainer.getAll()).thenReturn(Arrays.asList(siteMapConfiguration));

        model.activate(config);

        cacheParent.setPath(LOCATION + page.getPath());
        init(cacheParent);

        when(siteMapExtractorsContainer.findFirstFor(page)).thenReturn(extractor);
        when(extractor.getConfiguration()).thenReturn(siteMapConfiguration);

        model.addRefreshListener(this);
    }

    @Override
    public void onCacheRefreshed(final Resource rootPage, final List<SiteMapEntry> entries) {
        onCacheRefreshedMap.put(rootPage, entries);
    }

    @Test
    public void deactivate() {
        model.removeRefreshListener(this);
        model.deactivate();
        assertTrue(onCacheRefreshedMap.isEmpty());
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
        return addEntryCache(cache);
    }

    private ResourceMock addEntryCache(final String propertyName, final Object propertyValue) {
        final ResourceMock result = addEntryCache();
        result.putProperty(propertyName, propertyValue);
        return result;
    }

    @SuppressWarnings("rawtypes")
	@Test
    public void get_cacheExists() {
        final ResourceMock _0 = addEntryCache(X, 0);
        _0.putProperty("_u", 1);
        _0.putProperty("_w_", 2);
        _0.putProperty("_abc_z", 3);
        _0.putProperty(JCR_PRIMARY_TYPE, "per:X");
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
        assertNotNull(entry.getProperty("_u"));
        assertNotNull(entry.getProperty("_w_"));
        assertNull(entry.getProperty("w:"));
        assertNotNull(entry.getProperty("abc:z"));
        assertNull(entry.getProperty(JCR_PRIMARY_TYPE));
        final Object map = entry.getProperty(Y);
        assertNotNull(map);
        assertTrue(map instanceof Map);
        assertEquals(child.getProperty(X), ((Map)map).get(X));
    }

    @Test
    public void get_catchPersistenceException() throws PersistenceException {
        repo.mockResourceResolverCreate();
        disableCacheResolution();
        doThrow(PersistenceException.class).when(resourceResolver).commit();
        assertNotNull(model.get(page));
    }

    private void disableCacheResolution() {
        when(resourceResolver.getResource(cache.getPath())).thenReturn(null);
    }

    @Test
    public void get_extractorIsNull() throws PersistenceException {
        final ResourceMock _0 = cache.createChild("0");
        repo.mockResourceResolverCreate();
        disableCacheResolution();
        when(siteMapExtractorsContainer.findFirstFor(page)).thenReturn(null);
        assertNull(model.get(page));
        assertOnCacheRefreshedMapContains(page);
        verify(resourceResolver, times(1)).delete(_0);
    }

    private void assertOnCacheRefreshedMapContains(final Object key) {
        for (int i = 0; i < 10 && !onCacheRefreshedMap.containsKey(key); i++) {
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
            }
        }

        assertTrue(onCacheRefreshedMap.containsKey(key));
    }

    @Test
    public void putSiteMapsInCache() {
        repo.mockResourceResolverCreate();
        disableCacheResolution();
        when(extractor.extract(page)).thenReturn(entries);
        cache.createChild("0");
        SiteMapEntry entry = createEntry();
        entry.putProperty("x:y", 0);
        entries.add(entry);
        entry = createEntry();
        entry.putProperty("x:", 0);
        entries.add(entry);
        entry = createEntry();
        entry.putProperty("x", 0);
        entries.add(entry);
        assertNotNull(model.get(page));
    }

    private SiteMapEntry createEntry() {
        return new SiteMapEntry(page.getPath());
    }

    @Test
    public void rebuildMandatoryContent() {
        final HashSet<String> mandatoryPaths = new HashSet<>(Arrays.asList(page.getPath()));
        when(siteMapConfiguration.getMandatoryCachedPaths()).thenReturn(mandatoryPaths);
        model.rebuildAll();
        assertOnCacheRefreshedMapContains(page);
    }

    @Test
    public void getOriginalPath() {
        assertNull(model.getOriginalPath(LOCATION));
        assertEquals(page.getPath(), model.getOriginalPath(cache.getPath()));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void visit() throws PersistenceException {
        when(resourceResolver.create(any(), anyString(), any())).thenThrow(PersistenceException.class);
        assertNull(model.visit("0", new HashMap<>(), cache));
    }

}