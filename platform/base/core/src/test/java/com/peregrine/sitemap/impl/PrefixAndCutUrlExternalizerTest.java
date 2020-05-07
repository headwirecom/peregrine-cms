package com.peregrine.sitemap.impl;

import com.peregrine.commons.util.PerConstants;
import com.peregrine.sitemap.PrefixAndCutUrlExternalizerBaseTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junitx.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PrefixAndCutUrlExternalizerTest
        extends PrefixAndCutUrlExternalizerBaseTestBase<PrefixAndCutUrlExternalizer> {

    @Mock
    private PrefixAndCutUrlExternalizerConfig config;

    public PrefixAndCutUrlExternalizerTest() {
        super(new PrefixAndCutUrlExternalizer());
    }

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

    @Test
    public void map() {
        fullTest();
    }

}
