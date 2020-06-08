package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Page;
import com.peregrine.commons.util.PerConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapExtractorBaseTest extends SlingResourcesTest
        implements SiteMapUrlBuilder, PageRecognizer, UrlExternalizer {

    private static final String DOMAIN = "http://www.example.com";
    private static final String PATH = "path";
    private static final String NAME = "name";
    private static final String CONTENT = "content";

    private final Pattern pattern = Pattern.compile(resource.getPath());

    private final SiteMapConfiguration config = Mockito.mock(SiteMapConfiguration.class);

    private final List<PropertyProvider> propertyProviders = new LinkedList<>();
    private final List<PropertyProvider> defaultPropertyProviders = new LinkedList<>();
    {
        propertyProviders.add(null);
        defaultPropertyProviders.add(null);

        PropertyProvider provider = new PropertyProviderBase(PATH) {
            @Override
            public Object extractValue(final Page page) {
                return page.getPath();
            }
        };
        propertyProviders.add(provider);

        provider = new PropertyProviderBase(NAME) {
            @Override
            public Object extractValue(final Page page) {
                return page.getName();
            }
        };
        propertyProviders.add(provider);
        defaultPropertyProviders.add(provider);

        provider = new PropertyProviderBase(CONTENT) {
            @Override
            public Object extractValue(final Page page) {
                return page.hasContent();
            }
        };
        defaultPropertyProviders.add(provider);
    }
    private final Set<Resource> pagesInSiteMap = new HashSet<>();
    private final Set<Resource> pages = new HashSet<>();
    {
        pagesInSiteMap.add(parent);
        pagesInSiteMap.add(page);
        pages.addAll(pagesInSiteMap);
        pages.add(page);
    }

    private final SiteMapExtractorBase model = new SiteMapExtractorBase() {

        @Override
        public SiteMapConfiguration getConfiguration() {
            return config;
        }

        @Override
        protected Iterable<? extends PropertyProvider> getDefaultPropertyProviders() {
            return defaultPropertyProviders;
        }

        @Override
        protected SiteMapUrlBuilder getUrlBuilder() {
            return SiteMapExtractorBaseTest.this;
        }

    };

    @Override
    public String buildSiteMapUrl(final Resource siteMapRoot, final int index) {
        return buildUrl(siteMapRoot) + PerConstants.SLASH + index;
    }

    private String buildUrl(final Resource resource) {
        return resource.getPath() + SiteMapConstants.DOT_HTML;
    }

    @Override
    public int getIndex(final SlingHttpServletRequest request) {
        return request.getResource().hashCode();
    }

    @Override
    public boolean isPage(final Page candidate) {
        return pagesInSiteMap.contains(candidate.getResource());
    }

    @Override
    public String map(final Resource page) {
        return map(page.getResourceResolver(), buildUrl(page));
    }

    @Override
    public String map(final ResourceResolver resourceResolver, final String url) {
        return DOMAIN + url;
    }

    @Before
    public void setUp() {
        when(config.getPageRecognizer()).thenReturn(this);
        when(config.getPropertyProviders()).thenReturn(propertyProviders);
        when(config.getUrlExternalizer()).thenReturn(this);
    }

    @Test
    public void getConfiguration() {
        assertEquals(config, model.getConfiguration());
    }

    @Test
    public void appliesTo() {
        assertFalse(model.appliesTo(null));
        assertTrue(model.appliesTo(page));
        assertTrue(model.appliesTo(resource));
        when(config.getPagePathPattern()).thenReturn(pattern);
        assertFalse(model.appliesTo(page));
        assertTrue(model.appliesTo(resource));
    }

    @Test
    public void extract_notAPage() {
        final List<SiteMapEntry> entries = model.extract(contentRoot);
        assertTrue(entries.isEmpty());
    }

    @Test
    public void extract_noRecognizer() {
        when(config.getPageRecognizer()).thenReturn(null);
        final List<SiteMapEntry> entries = model.extract(resource);
        assertEquals(1, entries.size());
        final SiteMapEntry entry = entries.get(0);
        assertEquals(resource.getPath(), entry.getProperty(PATH, String.class));
        assertEquals(resource.getName(), entry.getProperty(NAME, String.class));
        assertFalse(entry.getProperty(CONTENT, Boolean.class));
    }

    @Test
    public void extract_noExternalizer() {
        when(config.getUrlExternalizer()).thenReturn(null);
        final List<SiteMapEntry> entries = model.extract(parent);
        assertEquals(2, entries.size());
        final SiteMapEntry entry = entries.get(1);
        assertEquals(page.getPath() + SiteMapConstants.DOT_HTML, entry.getUrl());
        assertEquals(page.getPath(), entry.getProperty(PATH, String.class));
        assertEquals(page.getName(), entry.getProperty(NAME, String.class));
        assertTrue(entry.getProperty(CONTENT, Boolean.class));
    }

    @Test
    public void extract() {
        final List<SiteMapEntry> entries = model.extract(parent);
        assertEquals(2, entries.size());
        final SiteMapEntry entry = entries.get(0);
        assertTrue(StringUtils.startsWith(entry.getUrl(), DOMAIN));
    }

    @Test
    public void buildSiteMapUrl() {
        for (int i = 0; i < 10; i++) {
            final String expected = map(resourceResolver, buildSiteMapUrl(resource, i));
            assertEquals(expected, model.buildSiteMapUrl(resource, i));
        }

        when(config.getUrlExternalizer()).thenReturn(null);
        for (int i = 0; i < 10; i++) {
            final String expected = buildSiteMapUrl(resource, i);
            assertEquals(expected, model.buildSiteMapUrl(resource, i));
        }
    }

    @Test
    public void getIndex() {
        assertEquals(resource.hashCode(), model.getIndex(request));
    }

}