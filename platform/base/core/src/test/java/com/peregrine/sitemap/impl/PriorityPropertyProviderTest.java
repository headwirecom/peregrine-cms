package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Page;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.sitemap.SiteMapConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public final class PriorityPropertyProviderTest extends SlingResourcesTest {

    private static final String VALUE = "0.2";

    private final PriorityPropertyProvider model = new PriorityPropertyProvider();
    private final Page candidate = new Page(page);

    @Test
    public void extractValue() {
        assertEquals(SiteMapConstants.DEFAULT_PRIORITY, model.extractValue(candidate));
        page.putProperty(PerConstants.PRIORITY, VALUE);
        assertEquals(VALUE, model.extractValue(candidate));
    }

}