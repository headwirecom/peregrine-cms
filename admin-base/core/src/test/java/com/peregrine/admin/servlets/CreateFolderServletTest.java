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
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CreateFolderServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String newResourcePath;
    private String newResourceName;

    private CreateFolderServlet init() throws RepositoryException {
        String resourcePath = "/perapi/admin/createFolder.json";
        String parentPath = "/content/test";
        newResourceName = "newFolder";
        newResourcePath = parentPath + "/" + newResourceName;

        setupCreation(resourcePath, PATH, parentPath, NAME, newResourceName);
        when(mockParentNode.addNode(eq(newResourceName), eq(SLING_ORDERED_FOLDER))).thenReturn(mockNewNode);

        // Create and Setup Servlet, call it and check the returned Resource
        CreateFolderServlet servlet = new CreateFolderServlet();
        setupServlet(servlet);
        return servlet;
    }

    @Test
    public void testDirectFolderCreation() throws ManagementException, RepositoryException {
        CreateFolderServlet servlet = init();
        Resource answer = servlet.doAction(mockRequest);
        assertNotNull("No Folder created", answer);
        assertEquals("Wrong Folder create (by path)", newResourcePath, answer.getPath());
    }

    @Test
    public void testRequestFolderCreation() throws RepositoryException, IOException {
        CreateFolderServlet servlet = init();
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Folder created", answer);
        assertTrue("Wrong Response Type", answer instanceof JsonResponse);
    }

    @Test
    public void testRequestFolderCreationFailure() throws RepositoryException, IOException {
        CreateFolderServlet servlet = init();
        when(mockParentNode.addNode(eq(newResourceName), eq(SLING_ORDERED_FOLDER))).thenThrow(
            new RepositoryException("Forced Test Failure")
        );
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Folder created", answer);
        assertTrue("Wrong Response Type", answer instanceof ErrorResponse);
    }
}
