package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.sitemap.SiteMapConfiguration;
import junitx.util.PrivateAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class SiteMapConfigurationsContainerImplTest extends SlingResourcesTest {

    private final SiteMapConfigurationsContainerImpl model = new SiteMapConfigurationsContainerImpl();
    private final SiteMapExtractorsContainerImpl extractors = new SiteMapExtractorsContainerImpl();

    @Mock
    private SiteMapConfiguration configuration;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "extractors", extractors);
    }

    @Test
    public void testEverything() {
        Collection<SiteMapConfiguration> all = model.getAll();
        assertEquals(1, all.size());
        assertTrue(model.add(configuration));
        all = model.getAll();
        assertTrue(all.contains(configuration));
        assertTrue(model.remove(configuration));
        all = model.getAll();
        assertEquals(1, all.size());
    }

}
