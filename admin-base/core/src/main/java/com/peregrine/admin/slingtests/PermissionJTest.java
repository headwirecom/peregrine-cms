package com.peregrine.admin.slingtests;

import com.peregrine.admin.resource.AdminResourceHandler;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.apache.jackrabbit.oak.spi.security.authorization.permission.Permissions;

import static com.peregrine.admin.slingtests.VersionsJTest.EXAMPLE_SITE_ROOT;

import static org.junit.Assert.*;

@RunWith(SlingAnnotationsTestRunner.class)
public class PermissionJTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @TestReference
    AdminResourceHandler resourceManagement;
    @TestReference
    private ResourceResolverFactory resolverFactory;
    private ResourceResolver resourceResolver;


    @Test
    public void runAsAdmin() {
        try {
            resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            assertTrue(resourceManagement.hasPermission(resourceResolver, "ALL", EXAMPLE_SITE_ROOT));
            assertTrue(resourceManagement.hasPermission(resourceResolver, "VERSION_MANAGEMENT", EXAMPLE_SITE_ROOT));
        } catch (LoginException e) {
            fail("No login");
        }
    }

    @Test
    public void runAsAnonymous() {
        try {
            resourceResolver = resolverFactory.getResourceResolver(null);
            assertEquals("anonymous", resourceResolver.getUserID());
            assertFalse(resourceManagement.hasPermission(resourceResolver, "ALL" , EXAMPLE_SITE_ROOT));
            assertTrue(resourceManagement.hasPermission(resourceResolver, "READ_NODE,READ_PROPERTY", EXAMPLE_SITE_ROOT));
        } catch (LoginException e) {
            fail("No login");
        }
    }
}
