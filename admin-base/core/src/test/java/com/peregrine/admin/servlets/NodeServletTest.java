package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.ResourceRelocation;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.intra.IntraSlingCaller;
import com.peregrine.intra.IntraSlingCaller.CallException;
import com.peregrine.intra.IntraSlingCaller.CallerContext;
import com.peregrine.rendition.BaseResourceHandler;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.admin.resource.AdminResourceHandlerService.MOVE_TO_RESOURCE_MISSING;
import static com.peregrine.admin.resource.AdminResourceHandlerService.NAME_TO_BE_RENAMED_TO_CANNOT_CONTAIN_A_SLASH;
import static com.peregrine.admin.servlets.MoveServlet.TARGET_PATH;
import static com.peregrine.admin.servlets.MoveServlet.TO;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.DATA_JSON_EXTENSION;
import static com.peregrine.commons.util.PerConstants.JSON_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(MoveNodeToServletTest.class.getName());

    @Override
    public Logger getLogger () {
    return logger;
}

    @Test
    public void testRequest() throws Exception {
        String requestPath = "/perapi/admin/node";
        String contentPath = "/content/test/node";
        String responseText = "{'test':'one'}";
        setupCreation(requestPath, PATH, contentPath);

        NodeServlet servlet = new NodeServlet();
        setupServlet(servlet);
        IntraSlingCaller mockIntraSlingCaller = mock(IntraSlingCaller.class);
        Whitebox.setInternalState(servlet, "intraSlingCaller", mockIntraSlingCaller);
        CallerContext mockCallerContext = mock(CallerContext.class);
        when(mockIntraSlingCaller.createContext()).thenReturn(mockCallerContext);
        when(mockCallerContext.setResourceResolver(any(ResourceResolver.class))).thenReturn(mockCallerContext);
        when(mockCallerContext.setPath(anyString())).thenReturn(mockCallerContext);
        when(mockCallerContext.setExtension(anyString())).thenReturn(mockCallerContext);
        when(mockIntraSlingCaller.call(eq(mockCallerContext))).thenReturn(responseText.getBytes());

        Response response = servlet.handleRequest(mockRequest);
        checkTextResponse(response, responseText, JSON_MIME_TYPE);
    }

    @Test
    public void testRequestFailure() throws Exception {
        String requestPath = "/perapi/admin/node";
        String contentPath = "/content/test/node";
        setupCreation(requestPath, PATH, contentPath);

        NodeServlet servlet = new NodeServlet();
        setupServlet(servlet);
        IntraSlingCaller mockIntraSlingCaller = mock(IntraSlingCaller.class);
        Whitebox.setInternalState(servlet, "intraSlingCaller", mockIntraSlingCaller);
        CallerContext mockCallerContext = mock(CallerContext.class);
        when(mockIntraSlingCaller.createContext()).thenReturn(mockCallerContext);
        when(mockCallerContext.setResourceResolver(any(ResourceResolver.class))).thenReturn(mockCallerContext);
        when(mockCallerContext.setPath(anyString())).thenReturn(mockCallerContext);
        when(mockCallerContext.setExtension(anyString())).thenReturn(mockCallerContext);
        when(mockIntraSlingCaller.call(eq(mockCallerContext))).thenThrow(new CallException("Test Failure"));

        Response response = servlet.handleRequest(mockRequest);
        checkRedirectResponse(response, contentPath + DATA_JSON_EXTENSION);
    }
}