package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import junitx.util.PrivateAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapExtractorDefaultsTest extends SlingResourcesTest {

    private final SiteMapExtractorDefaults model = new SiteMapExtractorDefaults();

    @Test
    public void getUrlBuilder() throws NoSuchFieldException {
        assertNull(model.getUrlBuilder());
        PrivateAccessor.setField(model, "urlBuilder", mock(SiteMapUrlBuilder.class));
        assertNotNull(model.getUrlBuilder());
    }

    @Test
    public void getUrlExternalizer() throws NoSuchFieldException {
        assertNull(model.getUrlExternalizer());
        PrivateAccessor.setField(model, "etcMapUrlExternalizer", new EtcMapUrlExternalizer());
        assertNotNull(model.getUrlExternalizer());
    }

    @Test
    public void getLastModPropertyProvider() throws NoSuchFieldException {
        assertNull(model.getLastModPropertyProvider());
        PrivateAccessor.setField(model, "lastModPropertyProvider", new LastModPropertyProvider());
        assertNotNull(model.getLastModPropertyProvider());
    }

    @Test
    public void getChangeFreqPropertyProvider() throws NoSuchFieldException {
        assertNull(model.getChangeFreqPropertyProvider());
        PrivateAccessor.setField(model, "changeFreqPropertyProvider", new ChangeFreqPropertyProvider());
        assertNotNull(model.getChangeFreqPropertyProvider());
    }

    @Test
    public void getPriorityPropertyProvider() throws NoSuchFieldException {
        assertNull(model.getPriorityPropertyProvider());
        PrivateAccessor.setField(model, "priorityPropertyProvider", new PriorityPropertyProvider());
        assertNotNull(model.getPriorityPropertyProvider());
    }

}
