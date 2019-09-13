package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet.RedirectResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.servlets.AbstractBaseServlet.TextResponse;
import com.peregrine.commons.test.mock.PeregrineRequestMock;
import com.peregrine.intra.IntraSlingCaller;
import com.peregrine.intra.IntraSlingCaller.CallException;
import com.peregrine.intra.IntraSlingCaller.CallerContext;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.IOException;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccessServletTest {

    @Test
    public void testRequest() throws CallException, IOException {
        String requestPath = "/perapi/admin/createFolder.json";
        PeregrineRequestMock mockRequest = PeregrineRequestMock.createInstance(requestPath);
        IntraSlingCaller mockIntraSlingCaller = mock(IntraSlingCaller.class);
        when(mockIntraSlingCaller.call(any(CallerContext.class))).thenReturn("".getBytes());
        CallerContext mockCallerContext = mock(CallerContext.class);
        when(mockIntraSlingCaller.createContext()).thenReturn(mockCallerContext);
        when(mockCallerContext.setResourceResolver(any(ResourceResolver.class))).thenReturn(mockCallerContext);
        when(mockCallerContext.setPath(any(String.class))).thenReturn(mockCallerContext);
        when(mockCallerContext.setSelectors(any(String.class))).thenReturn(mockCallerContext);
        when(mockCallerContext.setExtension(any(String.class))).thenReturn(mockCallerContext);

        AccessServlet servlet = new AccessServlet();
        Whitebox.setInternalState(servlet, "intraSlingCaller", mockIntraSlingCaller);
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Access Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof TextResponse);
    }

    @Test
    public void testIntraCallerException() throws CallException, IOException {
        String requestPath = "/perapi/admin/createFolder.json";
        PeregrineRequestMock mockRequest = PeregrineRequestMock.createInstance(requestPath);
        IntraSlingCaller mockIntraSlingCaller = mock(IntraSlingCaller.class);
        when(mockIntraSlingCaller.call(any(CallerContext.class))).thenThrow(
            new IntraSlingCaller.CallException("Test Failure")
        );
        CallerContext mockCallerContext = mock(CallerContext.class);
        when(mockIntraSlingCaller.createContext()).thenReturn(mockCallerContext);
        when(mockCallerContext.setResourceResolver(any(ResourceResolver.class))).thenReturn(mockCallerContext);
        when(mockCallerContext.setPath(any(String.class))).thenReturn(mockCallerContext);
        when(mockCallerContext.setSelectors(any(String.class))).thenReturn(mockCallerContext);
        when(mockCallerContext.setExtension(any(String.class))).thenReturn(mockCallerContext);

        AccessServlet servlet = new AccessServlet();
        Whitebox.setInternalState(servlet, "intraSlingCaller", mockIntraSlingCaller);
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Access Servlet", response);
        assertTrue("Response Type is not of type Text Response", response instanceof RedirectResponse);
    }
}
