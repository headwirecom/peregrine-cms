package com.peregrine.commons;

import com.peregrine.SlingResourcesTest;
import com.peregrine.TestingTools;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ResourceUtilsTest extends SlingResourcesTest {

    private final String path = resource.getPath();

    @Test
    public void confirmUtilClass() {
        TestingTools.testUtilClass(ResourceUtils.class);
    }

    @Test
    public void getFirstExistingAncestorOnPath_emptyPath() {
        final Resource resource = ResourceUtils.getFirstExistingAncestorOnPath(resourceResolver, null);
        assertEquals(repo.getRoot(), resource);
    }

    @Test
    public void getFirstExistingAncestorOnPath_noResourceUnderPath() {
        when(resourceResolver.getResource(path)).thenReturn(null);
        final Resource resource = ResourceUtils.getFirstExistingAncestorOnPath(resourceResolver, path);
        assertEquals(jcrContent, resource);
    }

    @Test
    public void getFirstExistingAncestorOnPath_findResource() {
        final Resource resource = ResourceUtils.getFirstExistingAncestorOnPath(resourceResolver, path);
        assertEquals(this.resource, resource);
    }

    @Test
    public void getOrCreateResource_resourceAlreadyExists() throws PersistenceException {
        final Resource resource = ResourceUtils.getOrCreateResource(resourceResolver, path, null);
        assertEquals(this.resource, resource);
    }

    @Test
    public void getOrCreateResource_resourceGetsCreated() throws PersistenceException {
        when(resourceResolver.getResource(path)).thenReturn(null);
        when(resourceResolver.create(eq(jcrContent), eq(NN_RESOURCE), any())).thenAnswer(invocation -> {
            when(resourceResolver.getResource(path)).thenReturn(resource);
            return resource;
        });
        final Resource resource = ResourceUtils.getOrCreateResource(resourceResolver, path, null);
        assertEquals(this.resource, resource);
    }

}
