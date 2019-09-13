package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet.ForwardResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.PeregrineRequestMock;
import org.junit.Test;

import static com.peregrine.commons.util.PerConstants.DATA_JSON_EXTENSION;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContentServletTest {

    @Test
    public void testRequest() throws Exception {
        String requestPath = "/perapi/admin/content.json";
        String contentPath = "/content/test/contentPage" + DATA_JSON_EXTENSION;
        PeregrineRequestMock mockRequest = PeregrineRequestMock.createInstance(requestPath, null, contentPath, false);

        ContentServlet servlet = new ContentServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ForwardResponse);
    }

    @Test
    public void testRequestNoExtension() throws Exception {
        String requestPath = "/perapi/admin/content.json";
        String contentPath = "/content/test/contentPage";
        PeregrineRequestMock mockRequest = PeregrineRequestMock.createInstance(requestPath, null, contentPath, false);

        ContentServlet servlet = new ContentServlet();
        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ForwardResponse);
    }
}
