package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.PeregrineRequestMock;
import com.peregrine.commons.test.mock.ResourceMock;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;

import java.io.InputStream;

import static com.peregrine.admin.servlets.ComponentDefinitionServlet.DIALOG_JSON;
import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.COMPONENT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_SUPER_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class ComponentDefinitionServletTest {

    private String path = "/content/test/myComponent";
    private PeregrineRequestMock mockRequest;
    private ResourceResolver resourceResolver;
    private ResourceMock componentResourceMock;

    private void init(String path, String resourceType, String componentResourceType) throws Exception {
        String requestPath = "/perapi/admin/getComponentDefinition.json";

        mockRequest = path != null ?
            PeregrineRequestMock.createInstance(requestPath, PATH, path) :
            PeregrineRequestMock.createInstance(requestPath);
        if(path != null) {
            ResourceMock resourceMock = new ResourceMock()
                .setPath(path)
                .putProperty(SLING_RESOURCE_TYPE, componentResourceType);
            resourceResolver = mockRequest.getResourceResolver();
            when(resourceResolver.getResource(eq(path))).thenReturn(resourceMock);

            Resource resource = resourceMock.getResource();
            when(resource.getResourceType()).thenReturn(resourceType);
            when(resource.getChild(DIALOG_JSON)).thenReturn(null);
            componentResourceMock = new ResourceMock()
                .setPath(APPS_ROOT + SLASH + componentResourceType);
            when(resourceResolver.getResource(APPS_ROOT + SLASH + componentResourceType)).thenReturn(componentResourceMock);
        }
    }

    @Test
    public void testRequest() throws Exception {
        String appsPath = APPS_ROOT + SLASH + "no/component/path";
        String componentResourceType = "test/component";
        init(appsPath, COMPONENT_PRIMARY_TYPE, componentResourceType);

        ComponentDefinitionServlet servlet = new ComponentDefinitionServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof JsonResponse);
    }

    @Test
    public void testRequestWithDialogNoSuperType() throws Exception {
        String componentResourceType = "test/component";
        init(path, COMPONENT_PRIMARY_TYPE, componentResourceType);
        ResourceMock dialogResourceMock = new ResourceMock()
            .setPath(componentResourceMock.getPath() + "/" + DIALOG_JSON);
        componentResourceMock.addChild(dialogResourceMock);
        // Create an Input Stream to adapt the Dialog to it
        InputStream is = new StringInputStream("");
        Resource resource = dialogResourceMock.getResource();
        when(resource.adaptTo(InputStream.class)).thenReturn(is);

        ComponentDefinitionServlet servlet = new ComponentDefinitionServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof JsonResponse);
    }

    @Test
    public void testRequestWithDialogSuperTypeMissing() throws Exception {
        String componentResourceType = "test/component";
        init(path, COMPONENT_PRIMARY_TYPE, componentResourceType);
        ResourceMock dialogResourceMock = new ResourceMock()
            .setPath(componentResourceMock.getPath() + "/" + DIALOG_JSON)
            .putProperty(SLING_RESOURCE_SUPER_TYPE, "test/super")
            .setResourceResolver(resourceResolver);
        componentResourceMock.addChild(dialogResourceMock);
        // Create an Input Stream to adapt the Dialog to it
        InputStream is = new StringInputStream("");
        Resource resource = dialogResourceMock.getResource();
        when(resource.adaptTo(InputStream.class)).thenReturn(is);

        ComponentDefinitionServlet servlet = new ComponentDefinitionServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof JsonResponse);
    }

    @Test
    public void testRequestWithDialogSuperType() throws Exception {
        String componentResourceType = "test/component";
        init(path, COMPONENT_PRIMARY_TYPE, componentResourceType);
        ResourceMock dialogResourceMock = new ResourceMock()
            .setPath(componentResourceMock.getPath() + "/" + DIALOG_JSON)
            .putProperty(SLING_RESOURCE_SUPER_TYPE, "test/super")
            .setResourceResolver(resourceResolver);
        componentResourceMock.addChild(dialogResourceMock);
        ResourceMock dialogSuperTypeMock = new ResourceMock()
            .setPath(APPS_ROOT + SLASH + "test/super");
        when(resourceResolver.getResource(eq(APPS_ROOT + SLASH + "test/super"))).thenReturn(dialogResourceMock);
        // Create an Input Stream to adapt the Dialog to it
        InputStream is = new StringInputStream("");
        Resource resource = dialogResourceMock.getResource();
        when(resource.adaptTo(InputStream.class)).thenReturn(is);

        ComponentDefinitionServlet servlet = new ComponentDefinitionServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof JsonResponse);
    }

    @Test
    public void testNoResourceFailure() throws Exception {
        String componentResourceType = "test/component";
        init(null, COMPONENT_PRIMARY_TYPE, componentResourceType);

        ComponentDefinitionServlet servlet = new ComponentDefinitionServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof ErrorResponse);
    }

    @Test
    public void testNoPageContentFailure() throws Exception {
        String componentResourceType = "test/component";
        init(path, PAGE_PRIMARY_TYPE, componentResourceType);

        ComponentDefinitionServlet servlet = new ComponentDefinitionServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof ErrorResponse);
    }

    @Test
    public void testNoComponentFailure() throws Exception {
        String componentResourceType = "test/component";
        init(path, COMPONENT_PRIMARY_TYPE, componentResourceType);
        when(resourceResolver.getResource(APPS_ROOT + SLASH + componentResourceType)).thenReturn(null);
        ComponentDefinitionServlet servlet = new ComponentDefinitionServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof ErrorResponse);
    }
}
