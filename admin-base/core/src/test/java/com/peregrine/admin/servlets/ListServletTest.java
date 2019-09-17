package com.peregrine.admin.servlets;

import com.peregrine.admin.servlets.ListServlet.PlainJsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.ResourceMock;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.peregrine.admin.servlets.ListServlet.CONTENT_ADMIN_TOOLS;
import static com.peregrine.admin.servlets.ListServlet.CONTENT_ADMIN_TOOLS_CONFIG;
import static com.peregrine.admin.servlets.ListServlet.TOOLS;
import static com.peregrine.admin.servlets.ListServlet.TOOLS_CONFIG;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.PATH;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class ListServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(ListServletTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testRequestForTools() throws Exception {
        String requestPath = "/perapi/admin/list.json";
        String contentPath = TOOLS;
        setupCreation(requestPath, PATH, contentPath);

        ResourceMock toolsResourceMock = new ResourceMock()
            .setPath(contentPath)
            .setResourceResolver(mockRequest.getResourceResolver());
        ResourceMock contentAdminToolsResourceMock = new ResourceMock()
            .setPath(CONTENT_ADMIN_TOOLS)
            .setResourceResolver(mockRequest.getResourceResolver());

        ListServlet servlet = new ListServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof PlainJsonResponse);
    }

    @Test
    public void testRequestForToolsConfig() throws Exception {
        String requestPath = "/perapi/admin/list.json";
        String contentPath = TOOLS_CONFIG;
        setupCreation(requestPath, PATH, contentPath);

        ResourceMock toolsResourceMock = new ResourceMock()
            .setPath(contentPath)
            .setResourceResolver(mockRequest.getResourceResolver());
        ResourceMock contentAdminToolsResourceMock = new ResourceMock()
            .setPath(CONTENT_ADMIN_TOOLS_CONFIG)
            .setResourceResolver(mockRequest.getResourceResolver());

        ListServlet servlet = new ListServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof PlainJsonResponse);
    }

    @Test
    public void testRequestBadPath() throws Exception {
        String requestPath = "/perapi/admin/list.json";
        String contentPath = "/laksjdfl;s";
        setupCreation(requestPath, PATH, contentPath);

        ListServlet servlet = new ListServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ErrorResponse);
    }

    @Test
    public void testRequestEmptyPath() throws Exception {
        String requestPath = "/perapi/admin/list.json";
        String contentPath = "";
        setupCreation(requestPath, PATH, contentPath);

        ListServlet servlet = new ListServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ErrorResponse);
    }

    @Test
    public void testRequestExporterFailure() throws Exception {
        String requestPath = "/perapi/admin/getObject.json";
        String contentPath = TOOLS_CONFIG;
        setupCreation(requestPath, PATH, contentPath);

        ResourceMock toolsResourceMock = new ResourceMock()
            .setPath(contentPath)
            .setResourceResolver(mockRequest.getResourceResolver());
        ResourceMock contentAdminToolsResourceMock = new ResourceMock()
            .setPath(CONTENT_ADMIN_TOOLS_CONFIG)
            .setResourceResolver(mockRequest.getResourceResolver());

        ListServlet servlet = new ListServlet();
        setupServlet(servlet);
        when(
            mockModelFactory.exportModelForResource(
                eq(contentAdminToolsResourceMock), eq(JACKSON), eq(String.class), any(Map.class)
            )
        ).thenThrow(new ExportException("Fail Model Export"));

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ErrorResponse);
    }

    @Test
    public void testRequestMissingExporterFailure() throws Exception {
        String requestPath = "/perapi/admin/getObject.json";
        String contentPath = TOOLS;
        setupCreation(requestPath, PATH, contentPath);

        ResourceMock toolsResourceMock = new ResourceMock()
            .setPath(contentPath)
            .setResourceResolver(mockRequest.getResourceResolver());
        ResourceMock contentAdminToolsResourceMock = new ResourceMock()
            .setPath(CONTENT_ADMIN_TOOLS)
            .setResourceResolver(mockRequest.getResourceResolver());

        ListServlet servlet = new ListServlet();
        setupServlet(servlet);
        when(
            mockModelFactory.exportModelForResource(
                eq(contentAdminToolsResourceMock), eq(JACKSON), eq(String.class), any(Map.class)
            )
        ).thenThrow(new MissingExporterException("MissingModelExporter", Object.class));

        Response response = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", response);
        assertTrue("Response Type is not of type Forward Response", response instanceof ErrorResponse);
    }
}
