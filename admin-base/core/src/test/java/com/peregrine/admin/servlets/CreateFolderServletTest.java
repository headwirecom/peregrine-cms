package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.TreeTest;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    @Test
    public void testSimpleFolderCreation() throws ManagementException, RepositoryException {
        String resourcePath = "/perapi/admin/createFolder.json";
        String parentPath = "/content/test";
        String newResourceName = "newFolder";
        String newResourcePath = parentPath + "/" + newResourceName;

        setupCreation(resourcePath, PATH, parentPath, NAME, newResourceName);
        when(mockParentNode.addNode(eq(newResourceName), eq(SLING_ORDERED_FOLDER))).thenReturn(mockNewNode);

        // Create and Setup Servlet, call it and check the returned Resource
        CreateFolderServlet servlet = new CreateFolderServlet();
        setupServlet(servlet);
        Resource answer = servlet.doAction(mockRequest);
        assertNotNull("No Resource Created", answer);
        assertEquals("Wrong Folder Create (by path)", newResourcePath, answer.getPath());
    }
}
