package com.peregrine.slingjunit;


import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.sling.jcr.jackrabbit.accessmanager.PrivilegesInfo;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SlingAnnotationsTestRunner.class)
public class RemoteReplAuthorJTest {

    @TestReference
    private SlingSettingsService settingsService;

    @TestReference
    private ResourceResolverFactory resourceResolverFactory;
    private AccessControlManager accessControlManager;
    private ResourceResolver adminResourceResolver = null;
    private Session adminSession = null;

    @TestReference
    private ResourceResolverFactory resolverFactory;

    @Before
    public void setup(){
        try {
            adminResourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            adminSession = adminResourceResolver.adaptTo(Session.class);
            accessControlManager =  adminSession.getAccessControlManager();

//            PrivilegesInfo privilegesInfo = adminResourceResolver.adaptTo(PrivilegesInfo.class);

//            JackrabbitAccessControlManager jackrabbithAccessControlManager = (JackrabbitAccessControlManager) accessControlManager;

        } catch (LoginException | RepositoryException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void cleanup(){
        accessControlManager = null;
        adminResourceResolver = null;
    }

    @Test
    public void authorRunmodes() {
        Set<String> runmmodes = settingsService.getRunModes();
        assertTrue(runmmodes.contains("author"));
        assertTrue(runmmodes.contains("notshared"));
        assertTrue(runmmodes.contains("oak_tar"));
    }

    @Test
    public void authorDistributionServiceUser() {
        PrivilegesInfo privilegesInfo = new PrivilegesInfo();
        Node contentNode = adminResourceResolver.getResource("/content").adaptTo(Node.class);

        try {
            PrivilegesInfo.AccessRights accessRights = privilegesInfo.getEffectiveAccessRightsForPrincipal(contentNode,"distribution-agent-user");
            Set<Privilege> grantedSet = accessRights.getGranted();
            assertTrue (grantedSet.stream().anyMatch( p ->
                p.getName().equals("jcr:read")
            ));

        } catch (RepositoryException e) {
            fail(e.getMessage());
        }
    }
}
