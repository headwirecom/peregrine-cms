package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.sitemap.SiteMapConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapExtractorsContainerImplTest extends SlingResourcesTest {

    private final SiteMapExtractorsContainerImpl model = new SiteMapExtractorsContainerImpl();
    private final Pattern pattern = Pattern.compile(page.getPath());

    @Mock
    private SiteMapConfiguration configuration;

    @Test
    public void addRemove() {
        assertFalse(model.remove(configuration));
        assertFalse(model.add(configuration));
        when(configuration.getPagePathPattern()).thenReturn(pattern);
        assertTrue(model.add(configuration));
        assertTrue(model.remove(configuration));
    }

    @Test
    public void findFirstFor() {
        when(configuration.getPagePathPattern()).thenReturn(pattern);
        model.add(configuration);
        assertNull(model.findFirstFor(parent));
        assertNotNull(model.findFirstFor(page));
    }

}
