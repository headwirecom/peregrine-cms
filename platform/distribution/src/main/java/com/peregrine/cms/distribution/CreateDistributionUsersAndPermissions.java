package com.peregrine.cms.distribution;

import com.peregrine.commons.util.AbstractUserAndPermissionsService;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;
import java.security.Principal;

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
    public static final String VAR_SLING_DISTRIBUTION_PACKAGES = "/var/sling/distribution/packages";
    public static final String LIBS_SLING_DISTRIBUTION = "/libs/sling/distribution";
    public static final String ETC_DISTRIBUTION = "/etc/distribution";
    public static final String CONTENT = "/content";
    public static final String LIBS_SLING_DISTRIBUTION1 = "/libs/sling/distribution";
    public static final String ETC_DISTRIBUTION1 = "/etc/distribution";
    public static final String CONTENT1 = "/content";
    public static final String JCR_ALL = "jcr:all";

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
        AccessControlUtils.addAccessControlEntry(session, VAR_SLING_DISTRIBUTION_PACKAGES, principal, new String[]{Privilege.JCR_ALL}, true);
        AccessControlUtils.addAccessControlEntry(session, LIBS_SLING_DISTRIBUTION, principal, new String[]{Privilege.JCR_ALL}, true);
        AccessControlUtils.addAccessControlEntry(session, ETC_DISTRIBUTION, principal, new String[]{Privilege.JCR_ALL}, true);
        AccessControlUtils.addAccessControlEntry(session, null, principal, new String[]{Privilege.JCR_ALL}, true);
        // We need the 'distribution-agent-user' user to have full access to the nodes we distribution from and to to set the replication properties
        AccessControlUtils.addAccessControlEntry(session, CONTENT, principal, new String[]{Privilege.JCR_ALL}, true);
    }

    @Override
    protected void testPermissions(Session session, String userName) {
        //AS NOTE: This cannot be tested and so we ignore it
        //                        checkUserPermission(session, "/var/sling/distribution/packages", userName, "jcr:all");
        checkUserPermission(session, LIBS_SLING_DISTRIBUTION1, userName, JCR_ALL);
        checkUserPermission(session, ETC_DISTRIBUTION1, userName, JCR_ALL);
        checkUserPermission(session, CONTENT1, userName, JCR_ALL);
    }
}
