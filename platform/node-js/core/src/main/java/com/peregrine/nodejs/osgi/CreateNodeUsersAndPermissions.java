package com.peregrine.nodejs.osgi;

import com.peregrine.commons.util.AbstractUserAndPermissionsService;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.security.Principal;

/**
 * Creates the System User for the Node JS
 * if it could log in as default admin otherwise it just tests the
 * user being in place
 */
@Component(immediate = true)
public class CreateNodeUsersAndPermissions
    extends AbstractUserAndPermissionsService
{

    public static final String NODE_JS_SERVICE_USER_NAME = "nodejs-service-user";
    public static final String NODE_JR_TEST_SUB_SERVICE = "nodejs-service";

    @Reference
    SlingRepository slingRepository;
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    public SlingRepository getSlingRepository() { return slingRepository; }

    @Override
    public ResourceResolverFactory getResourceResolverFactory() { return resourceResolverFactory; }

    @Activate
    public void activate() throws Exception {
        handleUserAndPermissions(NODE_JS_SERVICE_USER_NAME, NODE_JR_TEST_SUB_SERVICE);
    }

    @Override
    protected void createPermissions(Session session, Principal principal) throws RepositoryException {
    }

    @Override
    protected void testPermissions(Session session, String userName) {
    }
}
