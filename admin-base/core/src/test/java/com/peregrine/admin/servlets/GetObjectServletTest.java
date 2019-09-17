package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.ForwardResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.PeregrineRequestMock;
import org.junit.Test;

import static com.peregrine.commons.util.PerConstants.DATA_JSON_EXTENSION;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class GetObjectServletTest {

    @Test
    public void testRequest() throws Exception {
        String requestPath = "/perapi/admin/getObject.json";
        String contentPath = "/content/test/getObject";
        PeregrineRequestMock mockRequest = PeregrineRequestMock.createInstance(requestPath, null, contentPath, false);

        GetObjectServlet servlet = new GetObjectServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ForwardResponse);
    }

    @Test
    public void testRequestNoResourceFound() throws Exception {
        String requestPath = "/perapi/admin/getObject.json";
        String contentPath = "/content/test/getObject";
        PeregrineRequestMock mockRequest = PeregrineRequestMock.createInstance(requestPath, null, contentPath, false);
        // Make the getResourceByPath fail
        when(mockRequest.getResourceResolver().getResource(eq(contentPath))).thenReturn(null);

        GetObjectServlet servlet = new GetObjectServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ErrorResponse);
    }
}
