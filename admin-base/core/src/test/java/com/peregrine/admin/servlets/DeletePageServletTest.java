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
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class DeletePageServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String newResourcePath;

    private DeletePageServlet init() throws RepositoryException {
        String resourcePath = "/perapi/admin/deletePage.json";
        String parentPath = "/content/test";
        String newResourceName = "deletePage";
        newResourcePath = parentPath + "/" + newResourceName;

        setupDeletion(resourcePath, PATH, newResourcePath, NAME, newResourceName);
        setupDeletion(PAGE_PRIMARY_TYPE);

        // Create and Setup Servlet, call it and check the returned Resource
        DeletePageServlet servlet = new DeletePageServlet();
        setupServlet(servlet);
        return servlet;
    }

    @Test
    public void testSimplePageDeletion() throws ManagementException, RepositoryException {
        DeletePageServlet servlet = init();
        DeletionResponse answer = servlet.doAction(mockRequest);
        assertNotNull("No Page deleted", answer);
        assertEquals("No Page deleted (wrong path)", newResourcePath, answer.getPath());
    }

    @Test
    public void testRequestPageDeletion() throws RepositoryException, IOException {
        DeletePageServlet servlet = init();
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Page deleted", answer);
        assertTrue("Wrong Response Type", answer instanceof JsonResponse);
    }

    @Test
    public void testRequestPageDeletionFailure() throws RepositoryException, IOException {
        DeletePageServlet servlet = init();
        // Force an Management Exception by not returning the parent resource
        when(mockResourceResolver.getResource(eq(newResourcePath))).thenReturn(null);
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Response from Servlet", answer);
        assertTrue("Wrong Response Type", answer instanceof ErrorResponse);
    }
}
