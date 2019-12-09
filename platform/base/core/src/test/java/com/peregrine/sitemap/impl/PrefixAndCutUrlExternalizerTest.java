package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.util.PerConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junitx.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PrefixAndCutUrlExternalizerTest extends SlingResourcesTest {

    private final PrefixAndCutUrlExternalizer model = new PrefixAndCutUrlExternalizer();

    @Mock
    private PrefixAndCutUrlExternalizerConfig config;

    @Before
    public void setUp()  {
        when(config.cutCount()).thenReturn(2);
        when(config.prefix()).thenReturn("http://www.example.com");
        model.activate(config);
        when(config.name()).thenReturn(PerConstants.NAME);
    }

	@Test
    public void getName() {
        assertEquals(PerConstants.NAME, model.getName());
    }

    @Test
    public void map() {
        assertEquals("http://www.example.com", model.map(resourceResolver, ""));
        assertEquals("http://www.example.com", model.map(resourceResolver, "/content/sites.html"));
        assertEquals("http://www.example.com/page.html", model.map(resourceResolver, "/content/sites/page.html"));
        assertEquals("http://www.example.com/page/sub.html", model.map(resourceResolver, "/content/sites/page/sub.html"));
        assertEquals("http://www.example.com", model.map(resourceResolver, "/content/sites.x.html"));
        assertEquals("http://www.example.com", model.map(resourceResolver, "/content/sites.sitemap.html"));
        assertEquals("http://www.example.com/sites.sitemap.xml", model.map(resourceResolver, "/content/sites.sitemap.xml"));
        assertEquals("http://www.example.com/sites.sitemap.1.xml", model.map(resourceResolver, "/content/sites.sitemap.1.xml"));
    }

}
