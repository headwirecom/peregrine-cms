package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.sitemap.PropertyProvider;
import com.peregrine.sitemap.SiteMapConfiguration;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import com.peregrine.sitemap.UrlExternalizer;
import junitx.util.PrivateAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapExtractorImplTest extends SlingResourcesTest {

    private final EtcMapUrlExternalizer externalizer = new EtcMapUrlExternalizer();
    private final ChangeFreqPropertyProvider changeFreq = new ChangeFreqPropertyProvider();
    private final LastModPropertyProvider lastMod = new LastModPropertyProvider();
    private final PriorityPropertyProvider priority = new PriorityPropertyProvider();

    private final SiteMapExtractorDefaults extractorDefaults = new SiteMapExtractorDefaults();

    private final Pattern pattern = Pattern.compile(resource.getPath());

    private SiteMapExtractorImpl model;

    @Mock
    private SiteMapConfiguration config;

    @Mock
    private SiteMapUrlBuilder urlBuilder;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(extractorDefaults, "urlBuilder", urlBuilder);
        PrivateAccessor.setField(extractorDefaults, "etcMapUrlExternalizer", externalizer);
        PrivateAccessor.setField(extractorDefaults, "lastModPropertyProvider", lastMod);
        PrivateAccessor.setField(extractorDefaults, "changeFreqPropertyProvider", changeFreq);
        PrivateAccessor.setField(extractorDefaults, "priorityPropertyProvider", priority);
        model = new SiteMapExtractorImpl(config, extractorDefaults);
    }

    @Test
    public void getUrlBuilder() {
        assertEquals(urlBuilder, model.getUrlBuilder());
    }

    @Test
    public void getExternalizer() {
        assertEquals(externalizer, model.getExternalizer());
        when(config.getUrlExternalizer()).thenReturn(mock(UrlExternalizer.class));
        assertNotEquals(externalizer, model.getExternalizer());
    }

    @Test
    public void appliesTo() {
        assertTrue(model.appliesTo(page));
        assertTrue(model.appliesTo(resource));
        when(config.getPagePathPattern()).thenReturn(pattern);
        assertFalse(model.appliesTo(page));
        assertTrue(model.appliesTo(resource));
    }

    @Test
    public void getDefaultPropertyProviders() {
        List<PropertyProvider> list = new ArrayList<>();
        model.getDefaultPropertyProviders().forEach(list::add);
        assertTrue(list.contains(changeFreq));
        assertTrue(list.contains(lastMod));
        assertTrue(list.contains(priority));
    }

}
