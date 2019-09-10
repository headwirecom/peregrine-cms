package com.peregrine.admin.servlets;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.TreeTest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import static com.peregrine.commons.util.PerConstants.FROM_SITE_NAME;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static com.peregrine.commons.util.PerConstants.TO_SITE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;

public class CreateSiteServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testSimpleSiteCreation() throws ManagementException, RepositoryException, PersistenceException {
        String resourcePath = "/perapi/admin/createSite.json";
        String parentPath = SITES_ROOT;
        String oldSiteName = "oldSite";
        String newSiteName = "newSite";
        String newResourcePath = parentPath + "/" + newSiteName;

        setupCreation(resourcePath, PATH, parentPath, NAME, oldSiteName, FROM_SITE_NAME, oldSiteName, TO_SITE_NAME, newSiteName);
        setupSite(newSiteName);

        // Create and Setup Servlet, call it and check the returned Resource
        CreateSiteServlet servlet = new CreateSiteServlet();
        setupServlet(servlet);
        Resource answer = servlet.doAction(mockRequest);
        assertNotNull("No Resource Created", answer);
        assertEquals("Wrong Folder Create (by path)", newResourcePath, answer.getPath());
    }
}
