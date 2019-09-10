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
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.FROM_SITE_NAME;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static com.peregrine.commons.util.PerConstants.TO_SITE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CreateSiteServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(TreeTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    private String newResourcePath;
    private String newSiteName;

    private CreateSiteServlet init() throws RepositoryException, PersistenceException {
        String resourcePath = "/perapi/admin/createSite.json";
        String parentPath = SITES_ROOT;
        String oldSiteName = "oldSite";
        newSiteName = "newSite";
        newResourcePath = parentPath + "/" + newSiteName;

        setupCreation(resourcePath, PATH, parentPath, NAME, oldSiteName, FROM_SITE_NAME, oldSiteName, TO_SITE_NAME, newSiteName);
        setupSite(newSiteName);

        // Create and Setup Servlet, call it and check the returned Resource
        CreateSiteServlet servlet = new CreateSiteServlet();
        setupServlet(servlet);
        return servlet;
    }

    @Test
    public void testSimpleSiteCreation() throws ManagementException, RepositoryException, PersistenceException {
        CreateSiteServlet servlet = init();
        Resource answer = servlet.doAction(mockRequest);
        assertNotNull("No Site created", answer);
        assertEquals("Wrong Site created (by path)", newResourcePath, answer.getPath());
    }

    @Test
    public void testRequestSiteCreation() throws RepositoryException, IOException {
        CreateSiteServlet servlet = init();
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Site created", answer);
        assertTrue("Wrong Response Type", answer instanceof JsonResponse);
    }

    @Test
    public void testRequestSiteCreationFailure() throws RepositoryException, IOException {
        CreateSiteServlet servlet = init();
        // Force an Management Exception by not returning the parent resource
        when(mockResourceResolver.getResource(eq(SITES_ROOT))).thenReturn(null);
        Response answer = servlet.handleRequest(mockRequest);
        assertNotNull("No Folder created", answer);
        assertTrue("Wrong Response Type", answer instanceof ErrorResponse);
    }
}
