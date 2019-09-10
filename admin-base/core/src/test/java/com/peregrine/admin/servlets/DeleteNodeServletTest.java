package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.DeletionResponse;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.TreeTest;
import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import java.io.IOException;

import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class DeleteNodeServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String newResourcePath;

    private DeleteNodeServlet init() throws RepositoryException {
        String resourcePath = "/perapi/admin/deleteNode.json";
        String parentPath = "/content/test";
        String newResourceName = "deleteNode";
        newResourcePath = parentPath + "/" + newResourceName;
        String typeName = "myType";

        setupDeletion(resourcePath, PATH, newResourcePath, NAME, newResourceName, TYPE, typeName);
        setupDeletion(typeName);

        // Create and Setup Servlet, call it and check the returned Resource
        DeleteNodeServlet servlet = new DeleteNodeServlet();
        setupServlet(servlet);
        return servlet;
    }

    @Test
    public void testDirectNodeDeletion() throws ManagementException, RepositoryException {
        DeleteNodeServlet servlet = init();
        DeletionResponse answer = servlet.doAction(mockRequest);
        assertNotNull("No Node deleted", answer);
        assertEquals("No Node deleted (wrong path)", newResourcePath, answer.getPath());
    }

    @Test
    public void testRequestNodeDeletion() throws RepositoryException, IOException {
        DeleteNodeServlet servlet = init();
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Node deleted", answer);
        assertTrue("Wrong Response Type", answer instanceof JsonResponse);
    }

    @Test
    public void testRequestNodeDeletionFailure() throws RepositoryException, IOException {
        DeleteNodeServlet servlet = init();
        // Force an Management Exception by not returning the parent resource
        when(mockResourceResolver.getResource(eq(newResourcePath))).thenReturn(null);
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", answer);
        assertTrue("Wrong Response Type", answer instanceof ErrorResponse);
    }
}
