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
public final class ChangeFreqPropertyProviderTest extends SlingResourcesTest {

    private static final String MONTHLY = "monthly";

    private final ChangeFreqPropertyProvider model = new ChangeFreqPropertyProvider();
    private final Page candidate = new Page(page);

    @Test
    public void extractValue() {
        assertEquals(SiteMapConstants.WEEKLY, model.extractValue(candidate));
        page.putProperty(PerConstants.CHANGE_FREQUENCY, MONTHLY);
        assertEquals(MONTHLY, model.extractValue(candidate));
    }

}