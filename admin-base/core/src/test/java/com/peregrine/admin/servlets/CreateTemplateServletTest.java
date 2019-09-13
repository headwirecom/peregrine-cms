package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.TreeTest;
import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import java.io.IOException;

import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CreateTemplateServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String newResourcePath;

    private CreateTemplateServlet init() throws RepositoryException, PersistenceException {
        String resourcePath = "/perapi/admin/createTemplate.json";
        String parentPath = SITES_ROOT;
        String templateName = "newTemplate";
        String componentName = "myComponent";
        newResourcePath = parentPath + "/" + templateName;

        setupCreation(resourcePath, PATH, parentPath, NAME, templateName, COMPONENT, componentName);
        setupSite(templateName);

        // Create and Setup Servlet, call it and check the returned Resource
        CreateTemplateServlet servlet = new CreateTemplateServlet();
        setupServlet(servlet);
        return servlet;
    }

    @Test
    public void testDirectTemplateCreation() throws ManagementException, RepositoryException, PersistenceException {
        CreateTemplateServlet servlet = init();
        Resource answer = servlet.doAction(mockRequest);
        assertNotNull("No Template created", answer);
        assertEquals("Wrong Template created (by path)", newResourcePath, answer.getPath());
    }

    @Test
    public void testRequestTemplateCreation() throws RepositoryException, IOException {
        CreateTemplateServlet servlet = init();
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Site created", answer);
        assertTrue("Wrong Response Type", answer instanceof JsonResponse);
    }

    @Test
    public void testRequestTemplateCreationFailure() throws RepositoryException, IOException {
        CreateTemplateServlet servlet = init();
        // Force an Management Exception by not returning the parent resource
        when(mockResourceResolver.getResource(eq(SITES_ROOT))).thenReturn(null);
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Folder created", answer);
        assertTrue("Wrong Response Type", answer instanceof ErrorResponse);
    }
}
