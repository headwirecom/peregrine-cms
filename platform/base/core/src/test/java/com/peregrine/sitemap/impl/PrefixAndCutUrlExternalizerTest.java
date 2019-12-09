package com.peregrine.sitemap.impl;

import com.peregrine.commons.util.PerConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junitx.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PrefixAndCutUrlExternalizerTest {

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

    private void mapAndCompare(final String expected, final String input) {
        assertEquals(expected, model.map(null, input));
    }

    @Test
    public void map() {
        mapAndCompare("http://www.example.com", "");
        mapAndCompare("http://www.example.com", "/content/sites.html");
        mapAndCompare("http://www.example.com/page.html", "/content/sites/page.html");
        mapAndCompare("http://www.example.com/page/sub.html", "/content/sites/page/sub.html");
        mapAndCompare("http://www.example.com", "/content/sites.x.html");
        mapAndCompare("http://www.example.com", "/content/sites.sitemap.html");
        mapAndCompare("http://www.example.com/sitemap.xml", "/content/sites.sitemap.xml");
        mapAndCompare("http://www.example.com/sitemap.1.xml", "/content/sites.sitemap.1.xml");
    }

}
