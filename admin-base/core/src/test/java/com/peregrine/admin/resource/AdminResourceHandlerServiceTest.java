package com.peregrine.admin.resource;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.test.SlingResourcesTest;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.rendition.BaseResourceHandler;
import junitx.util.PrivateAccessor;
import org.apache.sling.api.resource.PersistenceException;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public final class AdminResourceHandlerServiceTest extends SlingResourcesTest {

    private static final String CHILD = "child";
    private static final String STRING = "string";
    private static final String INT = "int";

    // private final AdminResourceHandlerService model = new AdminResourceHandlerService();
    private final AdminResourceHandlerServiceOld model = new AdminResourceHandlerServiceOld();

    private final ResourceRelocation resourceRelocation = mock(ResourceRelocation.class);
    private final BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);

    private final Node resourceNode = resource.getNode();
    private final ResourceMock child = new ResourceMock("Child");

    private final Map<String, Object> properties = new HashMap<>();

    @Before
    public void setUp() throws NoSuchFieldException, RepositoryException {
        PrivateAccessor.setField(model, "resourceRelocation", resourceRelocation);
        PrivateAccessor.setField(model, "baseResourceHandler", baseResourceHandler);
        init(child);
        when(resourceNode.addNode(any(), any())).then(invocation -> {
            final String relPath = (String) invocation.getArguments()[0];
            child.setPath(resource.getPath() + SLASH + relPath);
            return child.getNode();
        });
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

    private void checkInsertNode(
            final boolean addAsChild,
            final boolean orderBefore,
            final String variation
    ) throws ManagementException {
        assertEquals(child, model.insertNode(resource, properties, addAsChild, orderBefore, variation));
    }

    @Test
    public void insertNode_addAsChild_doNotOrderBefore_nullVariation_noComponent() throws ManagementException {
        checkInsertNode(true, false, null);
    }

    @Test
    public void insertNode_addAsChild_doNotOrderBefore_nullVariation_slashedComponent() throws ManagementException {
        final String resourceType = SLASH + RESOURCE_TYPE;
        properties.put(COMPONENT, resourceType);
        properties.put(STRING, STRING);
        properties.put(INT, 0);
        checkInsertNode(true, false, null);
        assertEquals(STRING, child.getProperty(STRING));
        assertNull(child.getProperty(INT));
    }
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
        model.createNode(parent, CHILD, PRIMARY_TYPE, RESOURCE_TYPE);
    }

    @Test(expected = ManagementException.class)
    public void createNode_RuntimeException() throws PersistenceException, ManagementException {
        when(resourceResolver.create(any(), any(), any())).thenThrow(RuntimeException.class);
        model.createNode(parent, CHILD, PRIMARY_TYPE, RESOURCE_TYPE);
    }

    @Test
    public void createNode_noResourceType() throws PersistenceException, ManagementException {
        when(resourceResolver.create(any(), any(), any())).then(invocation ->
        {
            final Map<String, Object> properties = (Map<String, Object>) invocation.getArguments()[2];
            assertFalse(properties.containsKey(SLING_RESOURCE_TYPE));
            return resource;
        });

        assertEquals(resource, model.createNode(parent, CHILD, PRIMARY_TYPE, EMPTY));
    }

    @Test
    public void createNode() throws PersistenceException, ManagementException {
        when(resourceResolver.create(any(), any(), any())).then(invocation ->
        {
            final Map<String, Object> properties = (Map<String, Object>) invocation.getArguments()[2];
            assertTrue(properties.containsKey(SLING_RESOURCE_TYPE));
            assertEquals(RESOURCE_TYPE, properties.get(SLING_RESOURCE_TYPE));
            return resource;
        });

        assertEquals(resource, model.createNode(parent, CHILD, PRIMARY_TYPE, RESOURCE_TYPE));
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