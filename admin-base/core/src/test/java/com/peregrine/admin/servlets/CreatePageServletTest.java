package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import java.io.IOException;

import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TEMPLATE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CreatePageServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(CreatePageServletTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String newResourcePath;
    private String newResourceName;

    private CreatePageServlet init() throws RepositoryException {
        String resourcePath = "/perapi/admin/createPage.json";
        String parentPath = "/content/test";
        newResourceName = "newPage";
        newResourcePath = parentPath + "/" + newResourceName;

        setupCreation(resourcePath, PATH, parentPath, NAME, newResourceName, TEMPLATE_PATH, "peregrine/testTemplate");
        setupPage();
        when(mockParentNode.addNode(eq(newResourceName), eq(PAGE_PRIMARY_TYPE))).thenReturn(mockNewNode);

        // Create and Setup Servlet, call it and check the returned Resource
        CreatePageServlet servlet = new CreatePageServlet();
        setupServlet(servlet);
        return servlet;
    }

    @Test
    public void testSimplePageCreation() throws ManagementException, RepositoryException {
        CreatePageServlet servlet = init();
        Resource answer = servlet.doAction(mockRequest);
        assertNotNull("No Page created", answer);
        assertEquals("Wrong Page created (by path)", newResourcePath, answer.getPath());
    }

    @Test
    public void testRequestPageCreation() throws RepositoryException, IOException {
        CreatePageServlet servlet = init();
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Page created", answer);
        assertTrue("Wrong Response Type", answer instanceof JsonResponse);
    }

    @Test
    public void testRequestPageCreationFailure() throws RepositoryException, IOException {
        CreatePageServlet servlet = init();
        when(mockParentNode.addNode(eq(newResourceName), eq(PAGE_PRIMARY_TYPE))).thenThrow(
            new RepositoryException("Forced Test Failure")
        );
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Folder created", answer);
        assertTrue("Wrong Response Type", answer instanceof ErrorResponse);
    }
}
