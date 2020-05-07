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
        when(config.cutCount()).thenReturn(3);
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
        mapAndCompare("http://www.example.com", "/content/example/pages.html");
    }

}
