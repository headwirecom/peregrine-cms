package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.ResourceRelocation;
import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.RedirectResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.rendition.BaseResourceHandler;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.DROP;
import static com.peregrine.commons.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MoveNodeToServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(MoveNodeToServletTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testRequest() throws Exception {
        String requestPath = "/perapi/admin/moveTo.json";
        String contentPath = "/content/test/to";
        String fromPath = "/content/test/from";
        String type = ORDER_BEFORE_TYPE;
        setupCreation(requestPath, PATH, contentPath, COMPONENT, fromPath, TYPE, type);

        ResourceMock fromResourceMock = new ResourceMock()
            .setPath(fromPath)
            .setResourceResolver(mockRequest.getResourceResolver());
        ResourceMock mockParent = new ResourceMock()
            .setPath("/content/test");
        when(mockParentResource.getParent()).thenReturn(mockParent);

        MoveNodeTo servlet = new MoveNodeTo();
        setupServlet(servlet);
        ResourceRelocation mockResourceRelocation = mock(ResourceRelocation.class);
        BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);
        Whitebox.setInternalState(adminResourceHandler, "resourceRelocation", mockResourceRelocation);
        Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", baseResourceHandler);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof RedirectResponse);
    }

    @Test
    public void testRequestForDrop() throws Exception {
        String requestPath = "/perapi/admin/moveTo.json";
        String contentPath = "/content/test/to";
        String fromPath = "/content/test/from";
        String type = ORDER_BEFORE_TYPE;
        setupCreation(requestPath, PATH, contentPath, COMPONENT, fromPath, DROP, type);

        ResourceMock fromResourceMock = new ResourceMock()
            .setPath(fromPath)
            .setResourceResolver(mockRequest.getResourceResolver());
        ResourceMock mockParent = new ResourceMock()
            .setPath("/content/test");
        when(mockParentResource.getParent()).thenReturn(mockParent);

        MoveNodeTo servlet = new MoveNodeTo();
        setupServlet(servlet);
        ResourceRelocation mockResourceRelocation = mock(ResourceRelocation.class);
        BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);
        Whitebox.setInternalState(adminResourceHandler, "resourceRelocation", mockResourceRelocation);
        Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", baseResourceHandler);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof RedirectResponse);
    }

    @Test
    public void testRequestNoParent() throws Exception {
        String requestPath = "/perapi/admin/moveTo.json";
        String contentPath = "/content/test/to";
        String fromPath = "/content/test/from";
        String type = ORDER_BEFORE_TYPE;
        setupCreation(requestPath, PATH, contentPath, COMPONENT, fromPath, DROP, type);

        ResourceMock fromResourceMock = new ResourceMock()
            .setPath(fromPath)
            .setResourceResolver(mockRequest.getResourceResolver());

        MoveNodeTo servlet = new MoveNodeTo();
        setupServlet(servlet);
        ResourceRelocation mockResourceRelocation = mock(ResourceRelocation.class);
        BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);
        Whitebox.setInternalState(adminResourceHandler, "resourceRelocation", mockResourceRelocation);
        Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", baseResourceHandler);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ErrorResponse);
    }
}
