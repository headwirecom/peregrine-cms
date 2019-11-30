package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import com.peregrine.TestingTools;
import org.apache.sling.api.resource.Resource;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ResourceUtilsTest extends SlingResourcesTest {

    private final String path = resource.getPath();

    @Before
    public void setUp() {
    }

    @Test
    public void testConstructor() {
        TestingTools.testUtilClassConstructor(ResourceUtils.class);
    }

    @Test
    public void getFirstExistingAncestorOnPath_emptyPath() {
        final Resource resource = ResourceUtils.getFirstExistingAncestorOnPath(resourceResolver, null);
        assertEquals(repoRoot, resource);
    }

    @Test
    public void getFirstExistingAncestorOnPath_noResourceUnderPath() {
        when(resourceResolver.getResource(path)).thenReturn(null);
        final Resource resource = ResourceUtils.getFirstExistingAncestorOnPath(resourceResolver, path);
        assertEquals(content, resource);
    }

    @Test
    public void getFirstExistingAncestorOnPath_findResource() {
        final Resource resource = ResourceUtils.getFirstExistingAncestorOnPath(resourceResolver, path);
        assertEquals(this.resource, resource);
    }

    @Test
    public void getOrCreateResource() {
    }
}