package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.DeletionResponse;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.TreeTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeleteNodeServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testSimpleNodeDeletion() throws ManagementException, RepositoryException {
        String resourcePath = "/perapi/admin/deleteNode.json";
        String parentPath = "/content/test";
        String newResourceName = "deleteNode";
        String newResourcePath = parentPath + "/" + newResourceName;
        String typeName = "myType";

        setupDeletion(resourcePath, PATH, newResourcePath, NAME, newResourceName, TYPE, typeName);
        setupDeletion(typeName);

        // Create and Setup Servlet, call it and check the returned Resource
        DeleteNodeServlet servlet = new DeleteNodeServlet();
        setupServlet(servlet);
        DeletionResponse answer = servlet.doAction(mockRequest);
        assertNotNull("No Resource Created", answer);
        assertEquals("Wrong Delete Path", newResourcePath, answer.getPath());
    }
}
