package com.peregrine.admin.resource;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.test.mock.ResourceMock;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public final class AdminResourceHandlerServiceTest {

    public static final String CHILD = "child";
    public static final String PER_TEST = "per:Test";
    public static final String TEST_COMPONENT = "test/component";

    private final AdminResourceHandlerService model = new AdminResourceHandlerService();
    // private final AdminResourceHandlerServiceOld model = new AdminResourceHandlerServiceOld();

    private final ResourceResolver resourceResolver = mock(ResourceResolver.class);
    private final ResourceMock parent = new ResourceMock();
    private final ResourceMock resource = new ResourceMock();

    @Before
    public void setUp() {
        parent.setResourceResolver(resourceResolver);
        resource.setResourceResolver(resourceResolver);
    }

    @Test
    public void addImageMetadataSelector() {
    }

    @Test
    public void removeImageMetadataSelector() {
    }

    @Test
    public void createFolder() {
    }

    @Test
    public void createObject() {
    }

    @Test
    public void createPage() {
    }

    @Test
    public void createTemplate() {
    }

    @Test
    public void deleteResource() {
    }

    @Test
    public void deleteResource1() {
    }

    @Test
    public void insertNode() {
    }

    @Test
    public void moveNode() {
    }

    @Test
    public void rename() {
    }

    @Test
    public void createAssetFromStream() {
    }

    @Test(expected = ManagementException.class)
    public void createNode_PersistenceException() throws PersistenceException, ManagementException {
        when(resourceResolver.create(any(), any(), any())).thenThrow(PersistenceException.class);
        model.createNode(parent, CHILD, PER_TEST, TEST_COMPONENT);
    }

    @Test(expected = ManagementException.class)
    public void createNode_RuntimeException() throws PersistenceException, ManagementException {
        when(resourceResolver.create(any(), any(), any())).thenThrow(RuntimeException.class);
        model.createNode(parent, CHILD, PER_TEST, TEST_COMPONENT);
    }

    @Test
    public void createNode_noResourceType() throws PersistenceException, ManagementException {
        when(resourceResolver.create(any(), any(), any())).then(invocationOnMock ->
        {
            final Map<String, Object> properties = (Map<String, Object>) invocationOnMock.getArguments()[2];
            assertFalse(properties.containsKey(SLING_RESOURCE_TYPE));
            return resource;
        });

        assertEquals(resource, model.createNode(parent, CHILD, PER_TEST, EMPTY));
    }

    @Test
    public void createNode() throws PersistenceException, ManagementException {
        when(resourceResolver.create(any(), any(), any())).then(invocationOnMock ->
        {
            final Map<String, Object> properties = (Map<String, Object>) invocationOnMock.getArguments()[2];
            assertTrue(properties.containsKey(SLING_RESOURCE_TYPE));
            assertEquals(TEST_COMPONENT, properties.get(SLING_RESOURCE_TYPE));
            return resource;
        });

        assertEquals(resource, model.createNode(parent, CHILD, PER_TEST, TEST_COMPONENT));
    }

    @Test
    public void copyNode() {
    }

    @Test
    public void copySite() {
    }

    @Test
    public void deleteSite() {
    }

    @Test
    public void updateResource() {
    }

    @Test
    public void handleAssetDimensions() {
    }
}