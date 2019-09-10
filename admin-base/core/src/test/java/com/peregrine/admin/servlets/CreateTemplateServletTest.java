package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.TreeTest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CreateTemplateServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testSimpleTemplateCreation() throws ManagementException, RepositoryException, PersistenceException {
        String resourcePath = "/perapi/admin/createTemplate.json";
        String parentPath = SITES_ROOT;
        String templateName = "newTemplate";
        String componentName = "myComponent";
        String newResourcePath = parentPath + "/" + templateName;

        setupCreation(resourcePath, PATH, parentPath, NAME, templateName, COMPONENT, componentName);
        setupSite(templateName);

        // Create and Setup Servlet, call it and check the returned Resource
        CreateTemplateServlet servlet = new CreateTemplateServlet();
        setupServlet(servlet);
        Resource answer = servlet.doAction(mockRequest);
        assertNotNull("No Resource Created", answer);
        assertEquals("Wrong Folder Create (by path)", newResourcePath, answer.getPath());
    }
}
