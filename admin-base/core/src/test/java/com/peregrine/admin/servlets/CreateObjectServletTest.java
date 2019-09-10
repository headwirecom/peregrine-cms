package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.TreeTest;
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
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TEMPLATE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CreateObjectServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String newResourcePath;
    private String newResourceName;

    private CreateObjectServlet init() throws RepositoryException {
        String resourcePath = "/perapi/admin/createObject.json";
        String parentPath = "/content/test";
        newResourceName = "newObject";
        newResourcePath = parentPath + "/" + newResourceName;

        setupCreation(resourcePath, PATH, parentPath, NAME, newResourceName, TEMPLATE_PATH, "peregrine/testTemplate");
        when(mockParentNode.addNode(eq(newResourceName), eq(OBJECT_PRIMARY_TYPE))).thenReturn(mockNewNode);

        // Create and Setup Servlet, call it and check the returned Resource
        CreateObjectServlet servlet = new CreateObjectServlet();
        setupServlet(servlet);
        return servlet;
    }

    @Test
    public void testDirectObjectCreation() throws ManagementException, RepositoryException {
        CreateObjectServlet servlet = init();
        Resource answer = servlet.doAction(mockRequest);
        assertNotNull("No Object created", answer);
        assertEquals("Wrong Object created (by path)", newResourcePath, answer.getPath());
    }

    @Test
    public void testRequestObjectCreation() throws RepositoryException, IOException {
        CreateObjectServlet servlet = init();
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Folder created", answer);
        assertTrue("Wrong Response Type", answer instanceof JsonResponse);
    }

    @Test
    public void testRequestObjectCreationFailure() throws RepositoryException, IOException {
        CreateObjectServlet servlet = init();
        when(mockParentNode.addNode(eq(newResourceName), eq(OBJECT_PRIMARY_TYPE))).thenThrow(
            new RepositoryException("Forced Test Failure")
        );
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Folder created", answer);
        assertTrue("Wrong Response Type", answer instanceof ErrorResponse);
    }
}
