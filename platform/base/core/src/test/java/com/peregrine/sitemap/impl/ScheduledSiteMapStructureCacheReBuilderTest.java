package com.peregrine.sitemap.impl;

import com.peregrine.sitemap.SiteMapFilesCache;
import com.peregrine.sitemap.SiteMapStructureCache;
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
    private SiteMapFilesCache files;

    @Mock
    private SiteMapStructureCache structure;

    @Before
    public void setUp() throws NoSuchFieldException {
        PrivateAccessor.setField(model, "files", files);
        PrivateAccessor.setField(model, "structure", structure);
    }

    @Test
    public void run() {
        model.run();
        verify(files, times(1)).rebuildAll();
        verify(structure, times(1)).rebuildAll();
    }

}