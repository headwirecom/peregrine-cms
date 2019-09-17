package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.ResourceRelocation;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.RedirectResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.rendition.BaseResourceHandler;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.admin.servlets.MoveServlet.TO;
import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MoveServletTest
    extends AbstractServletTest
{
        private static final Logger logger = LoggerFactory.getLogger(MoveNodeToServletTest.class.getName());

        @Override
        public Logger getLogger () {
        return logger;
    }

    @Test
    public void testMoveRequest() throws Exception {
        String requestPath = "/perapi/admin/move";
        String contentPath = "/content/test/move";
        String toPath = "/content/test/move-to";
        String type = ORDER_BEFORE_TYPE;
        setupCreation(requestPath, PATH, contentPath, TO, toPath, TYPE, type);

        ResourceMock mockParent = new ResourceMock()
            .setPath("/content/test");
        when(mockParentResource.getParent()).thenReturn(mockParent);

        MoveServlet servlet = new MoveServlet();
        setupServlet(servlet);
        ResourceRelocation mockResourceRelocation = mock(ResourceRelocation.class);
        BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);
        Whitebox.setInternalState(adminResourceHandler, "resourceRelocation", mockResourceRelocation);
        Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", baseResourceHandler);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof JsonResponse);
    }

    @Test
    public void testRenameRequest() throws Exception {
        String requestPath = "/perapi/admin/rename";
        String contentPath = "/content/test/rename";
        String toPath = "/content/test/new-name";
        String type = ORDER_BEFORE_TYPE;
        setupCreation(requestPath, PATH, contentPath, TO, toPath, TYPE, type);

        ResourceMock mockParent = new ResourceMock()
            .setPath("/content/test");
        when(mockParentResource.getParent()).thenReturn(mockParent);

        MoveServlet servlet = new MoveServlet();
        setupServlet(servlet);
        ResourceRelocation mockResourceRelocation = mock(ResourceRelocation.class);
        BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);
        Whitebox.setInternalState(adminResourceHandler, "resourceRelocation", mockResourceRelocation);
        Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", baseResourceHandler);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof JsonResponse);
    }
}