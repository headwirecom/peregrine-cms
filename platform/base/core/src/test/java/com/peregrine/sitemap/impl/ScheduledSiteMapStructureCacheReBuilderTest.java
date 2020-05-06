package com.peregrine.sitemap.impl;

import com.peregrine.sitemap.SiteMapFilesCache;
import junitx.util.PrivateAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public final class ScheduledSiteMapStructureCacheReBuilderTest {

    private final ScheduledSiteMapStructureCacheReBuilder model = new ScheduledSiteMapStructureCacheReBuilder();

    @Mock
    private SiteMapFilesCache cache;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "cache", cache);
    }

    @Test
    public void run() {
        model.run();
        verify(cache, times(1)).rebuildAll();
    }

}