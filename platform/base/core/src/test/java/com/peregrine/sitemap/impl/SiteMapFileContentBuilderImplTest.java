package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.sitemap.SiteMapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapFileContentBuilderImplTest extends SlingResourcesTest {

    private final SiteMapFileContentBuilderImpl model = new SiteMapFileContentBuilderImpl();
    private final List<SiteMapEntry> entries = new LinkedList<>();
    private final String[] xmlnsMappings = { };
    private final Map<String, String> xmlns = new HashMap<>();

    @Mock
    private SiteMapFileContentBuilderImplConfig config;

    @Before
    public void setUp() {
        when(config.xmlnsMappings()).thenReturn(xmlnsMappings);
        model.activate(config);
    }

    @Test
    public void getBaseSiteMapLength() {
        final String xml = model.buildUrlSet(entries, xmlns);
        final int length = xml.length();
        final int diff = "</urlset>".length() - " /".length();
        final int baseSiteMapLength = model.getBaseSiteMapLength();
        assertTrue(length + diff == baseSiteMapLength || length == baseSiteMapLength);
    }

}