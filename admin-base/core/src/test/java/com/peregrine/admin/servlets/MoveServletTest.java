package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.ResourceRelocation;
import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.RedirectResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.rendition.BaseResourceHandler;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.admin.resource.AdminResourceHandlerService.MOVE_TO_RESOURCE_MISSING;
import static com.peregrine.admin.resource.AdminResourceHandlerService.NAME_CONSTRAINT_VIOLATION;
import static com.peregrine.admin.resource.AdminResourceHandlerService.NAME_TO_BE_RENAMED_TO_CANNOT_CONTAIN_A_SLASH;
import static com.peregrine.admin.servlets.MoveServlet.TARGET_PATH;
import static com.peregrine.admin.servlets.MoveServlet.TO;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
        ResourceMock mockTo = new ResourceMock()
            .setPath(toPath)
            .setParent(mockParent)
            .setResourceResolver(mockResourceResolver);

        MoveServlet servlet = new MoveServlet();
        setupServlet(servlet);
        ResourceRelocation mockResourceRelocation = mock(ResourceRelocation.class);
        BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);
        Whitebox.setInternalState(adminResourceHandler, "resourceRelocation", mockResourceRelocation);
        Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", baseResourceHandler);
        when(mockResourceRelocation.hasSameParent(any(Resource.class), any(Resource.class))).thenReturn(true);

        Response response = servlet.handleRequest(mockRequest);
        checkJsonResponse(response, SOURCE_PATH, contentPath, TARGET_PATH, contentPath);
    }

    @Test
    public void testMoveRequestFailure() throws Exception {
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
        checkErrorResponse(response, MOVE_TO_RESOURCE_MISSING);
    }

    @Test
    public void testRenameRequest() throws Exception {
        String requestPath = "/perapi/admin/rename";
        String contentPath = "/content/test/rename";
        String toPath = "new-name";
        String type = ORDER_BEFORE_TYPE;
        setupCreation(requestPath, PATH, contentPath, TO, toPath, TYPE, type);

        ResourceMock mockParent = new ResourceMock()
            .setPath("/content/test");
        when(mockParentResource.getParent()).thenReturn(mockParent);
        ResourceMock mockTo = new ResourceMock()
            .setPath(toPath)
            .setParent(mockParent)
            .setResourceResolver(mockResourceResolver);

        MoveServlet servlet = new MoveServlet();
        setupServlet(servlet);
        ResourceRelocation mockResourceRelocation = mock(ResourceRelocation.class);
        BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);
        Whitebox.setInternalState(adminResourceHandler, "resourceRelocation", mockResourceRelocation);
        Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", baseResourceHandler);
        when(mockResourceRelocation.rename(any(Resource.class), anyString(), anyBoolean())).thenReturn(mockTo);

        Response response = servlet.handleRequest(mockRequest);
        checkJsonResponse(response, SOURCE_PATH, contentPath, TARGET_PATH, toPath);
    }

    @Test
    public void testRenameRequestFailure() throws Exception {
        String requestPath = "/perapi/admin/rename";
        String contentPath = "/content/test/rename";
        String toPath = "new-name/failure";
        String type = ORDER_BEFORE_TYPE;
        setupCreation(requestPath, PATH, contentPath, TO, toPath, TYPE, type);

        ResourceMock mockParent = new ResourceMock()
            .setPath("/content/test");
        when(mockParentResource.getParent()).thenReturn(mockParent);
        ResourceMock mockTo = new ResourceMock()
            .setPath(toPath)
            .setParent(mockParent)
            .setResourceResolver(mockResourceResolver);

        MoveServlet servlet = new MoveServlet();
        setupServlet(servlet);
        ResourceRelocation mockResourceRelocation = mock(ResourceRelocation.class);
        BaseResourceHandler baseResourceHandler = mock(BaseResourceHandler.class);
        Whitebox.setInternalState(adminResourceHandler, "resourceRelocation", mockResourceRelocation);
        Whitebox.setInternalState(adminResourceHandler, "baseResourceHandler", baseResourceHandler);
        when(mockResourceRelocation.rename(any(Resource.class), anyString(), anyBoolean())).thenReturn(mockTo);

        Response response = servlet.handleRequest(mockRequest);
        checkErrorResponse(response, String.format(NAME_CONSTRAINT_VIOLATION, toPath));
    }
}