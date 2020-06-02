package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Page;
import com.peregrine.commons.util.PerConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public final class LastModPropertyProviderTest extends SlingResourcesTest {

    private final LastModPropertyProvider model = new LastModPropertyProvider();
    private final Page candidate = new Page(page);

    @Test
    public void extractValue() {
        assertNull(model.extractValue(candidate));
        page.putProperty(PerConstants.JCR_LAST_MODIFIED, Calendar.getInstance());
        assertNotNull(model.extractValue(candidate));
    }

}