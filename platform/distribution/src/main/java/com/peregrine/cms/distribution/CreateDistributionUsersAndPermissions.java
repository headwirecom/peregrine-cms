package com.peregrine.cms.distribution;

import com.peregrine.commons.util.AbstractUserAndPermissionsService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;

import javax.jcr.Session;

import java.security.Principal;

import static javax.jcr.security.Privilege.JCR_ALL;

/**
 * Creates the System User for the Sling Distribution
 * and sets the necessary permissions if it could
 * log in as default admin otherwise it just tests the
 * permissions
 */
@Component(immediate = true)
public class CreateDistributionUsersAndPermissions
    extends AbstractUserAndPermissionsService
{

    public static final String DISTRIBUTION_AGENT_USER = "distribution-agent-user";
    public static final String DISTRIBUTION_TEST_SUB_SERVICE = "test-distribution";

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
        handleUserAndPermissions(DISTRIBUTION_AGENT_USER, DISTRIBUTION_TEST_SUB_SERVICE);
    }

    @Override
    protected void createPermissions(Session session, Principal principal) throws RepositoryException {
        AccessControlUtils.addAccessControlEntry(session, "/var/sling/distribution/packages", principal, new String[]{JCR_ALL}, true);
        AccessControlUtils.addAccessControlEntry(session, "/libs/sling/distribution", principal, new String[]{JCR_ALL}, true);
        AccessControlUtils.addAccessControlEntry(session, "/etc/distribution", principal, new String[]{JCR_ALL}, true);
        AccessControlUtils.addAccessControlEntry(session, null, principal, new String[]{JCR_ALL}, true);
        // We need the 'distribution-agent-user' user to have full access to the nodes we distribution from and to to set the replication properties
        AccessControlUtils.addAccessControlEntry(session, "/content", principal, new String[]{JCR_ALL}, true);
    }

    @Override
    protected void testPermissions(Session session, String userName) {
        //AS NOTE: This cannot be tested and so we ignore it
        //                        checkUserPermission(session, "/var/sling/distribution/packages", userName, "jcr:all");
        checkUserPermission(session, "/libs/sling/distribution", userName, "jcr:all");
        checkUserPermission(session, "/etc/distribution", userName, "jcr:all");
        checkUserPermission(session, "/content", userName, "jcr:all");
    }
}
