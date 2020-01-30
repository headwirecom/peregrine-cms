package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Strings;
import com.peregrine.sitemap.SiteMapEntry;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junitx.framework.StringAssert.assertContains;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapFileContentBuilderImplTest extends SlingResourcesTest {

    private final SiteMapFileContentBuilderImpl model = new SiteMapFileContentBuilderImpl();
    private final List<SiteMapEntry> entries = new LinkedList<>();
    private final SiteMapEntry entry = createEntry();
    {
        entries.add(entry);
    }
    private final String[] xmlnsMappings = { };
    private final Map<String, String> xmlns = new HashMap<>();

    @Mock
    private SiteMapFileContentBuilderImplConfig config;

    @Mock
    private SiteMapUrlBuilder urlBuilder;

    @Before
    public void setUp() {
        when(config.xmlnsMappings()).thenReturn(xmlnsMappings);
        model.activate(config);

        when(urlBuilder.buildSiteMapUrl(eq(page), anyInt()))
                .thenAnswer(invocation -> {
                    final Object[] args = invocation.getArguments();
                    return page.getPath() + args[1];
                });
    }

    @Test
    public void buildSiteMapIndex() {
        final List<List<SiteMapEntry>> splitEntries = new LinkedList<>();
        splitEntries.add(entries);
        final List<SiteMapEntry> entries = new LinkedList<>();
        splitEntries.add(entries);
        entries.add(entry);
        entries.add(createEntry("2001-01-01"));
        entries.add(entry);
        entries.add(createEntry("2000-01-01"));
        entries.add(createEntry("2002-01-01"));
        final String result = model.buildSiteMapIndex(page, urlBuilder, splitEntries);
        assertNotNull(result);
        assertTrue(result.contains(page.getPath() + 1));
        assertTrue(result.contains(page.getPath() + 2));
        assertFalse(result.contains(page.getPath() + 3));
        assertTrue(result.contains("2002-01-01"));
    }

    @Test
    public void getBaseSiteMapLength() {
        entries.clear();
        final String xml = model.buildUrlSet(entries, xmlns);
        final int length = xml.length();
        final int diff = "</urlset>".length() - " /".length();
        final int baseSiteMapLength = model.getBaseSiteMapLength();
        assertTrue(length + diff == baseSiteMapLength || length == baseSiteMapLength);
    }

    @Test
    public void getSize() {
        assertEquals(0, model.getSize(entry));
        entry.setUrl("URL");
        assertEquals(36, model.getSize(entry));
    }

    private SiteMapEntry createEntry() {
        return new SiteMapEntry(page.getPath());
    }

    private SiteMapEntry createEntry(final String lastModified) {
        final SiteMapEntry result = createEntry();
        result.setLastModified(lastModified);
        return result;
    }

    @Test
    public void buildUrlSet() {
        final SiteMapEntry entry = createEntry();
        entry.setUrl("URL");
        entries.add(entry);
        String urlSet = model.buildUrlSet(entries, xmlns);
        urlSet = Strings.removeWhitespaces(urlSet);
        assertContains("<loc>URL</loc>", urlSet);
    }

}