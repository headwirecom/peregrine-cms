package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.DeletionResponse;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.TreeTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeletePageServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testSimplePageDeletion() throws ManagementException, RepositoryException {
        String resourcePath = "/perapi/admin/deletePage.json";
        String parentPath = "/content/test";
        String newResourceName = "deletePage";
        String newResourcePath = parentPath + "/" + newResourceName;

        setupDeletion(resourcePath, PATH, newResourcePath, NAME, newResourceName);
        setupDeletion(PAGE_PRIMARY_TYPE);

        // Create and Setup Servlet, call it and check the returned Resource
        DeletePageServlet servlet = new DeletePageServlet();
        setupServlet(servlet);
        DeletionResponse answer = servlet.doAction(mockRequest);
        assertNotNull("No Resource Created", answer);
        assertEquals("Wrong Delete Path", newResourcePath, answer.getPath());
    }
}
