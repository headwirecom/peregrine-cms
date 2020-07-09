package com.peregrine.slingjunit;


import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.sling.jcr.jackrabbit.accessmanager.PrivilegesInfo;
import javax.jcr.Node;
import javax.jcr.RepositoryException;;
import javax.jcr.security.Privilege;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import static org.junit.Assert.*;

@RunWith(SlingAnnotationsTestRunner.class)
public class RemoteReplAuthorJTest {

    @TestReference
    private SlingSettingsService settingsService;

    private ResourceResolver adminResourceResolver = null;

    @TestReference
    private ResourceResolverFactory resolverFactory;


    @TestReference
    private ConfigurationAdmin configAdmin;

    @Before
    public void setup(){
        try {
            adminResourceResolver = resolverFactory.getAdministrativeResourceResolver(null);

        } catch (LoginException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void cleanup(){
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
    public void authorDistributionServiceUserAccess() {
        List<String> readList = Arrays.asList("/var/sling/distribution", "/content");
        List<String> allList = Arrays.asList("/var/sling/distribution/packages");
        testListGranted(readList, "jcr:read", "distribution-agent-user");
        testListGranted(allList, "jcr:all", "distribution-agent-user");
    }

    private static String MAPPER_DISTRIBUTION_EVENT_PID = "org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended~distributionEventHandler";
    private static String[] DIST_EVENT_VALUES = {
        "com.peregrine-cms.admin.core:peregrine-distribution-sub-service=distribution-agent-user",
        "com.peregrine-cms.replication.core:peregrine-distribution-sub-service=distribution-agent-user"
    };

    private static String MAPPER_DISTRIBUTION_SERVICE_PID = "org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended~distributionAgentService";
    private static String[] DIST_SERVICE_VALUES = {
        "org.apache.sling.distribution.core:defaultAgentService=distribution-agent-user",
        "org.apache.sling.distribution.core=distribution-agent-user",
        "com.peregrine-cms.distribution:test-distribution=distribution-agent-user"
    };
    private static String USER_MAPPING = "user.mapping";

    @Test
    public void authorDistributionServiceUserMappings() {
        testOsgiStringArrayProps(MAPPER_DISTRIBUTION_EVENT_PID, USER_MAPPING, DIST_EVENT_VALUES);
        testOsgiStringArrayProps(MAPPER_DISTRIBUTION_SERVICE_PID, USER_MAPPING, DIST_SERVICE_VALUES);
    }

    private void testListGranted(List<String> list, String privilege, String usedId) {
        PrivilegesInfo privilegesInfo = new PrivilegesInfo();
        for (String path : list) {
            Node node = adminResourceResolver.getResource(path).adaptTo(Node.class);
            try {
                PrivilegesInfo.AccessRights accessRights = privilegesInfo.getEffectiveAccessRightsForPrincipal(node,usedId);
                Set<Privilege> grantedSet = accessRights.getGranted();
                assertTrue (grantedSet.stream().anyMatch( p ->
                    p.getName().equals(privilege)
                ));
            } catch (RepositoryException e) {
                fail(e.getMessage());
            }
        }
    }

    private void testOsgiStringArrayProps(String pid, String property, String[] expectedArr) {
        try {
            Configuration osgiConfig = configAdmin.getConfiguration(pid, null);
            Dictionary<String, Object> properties = osgiConfig.getProperties();
            String[] distMappers = PropertiesUtil.toStringArray(properties.get(property));

            for (String expected : expectedArr) {
                assertTrue(
                        Arrays.stream(distMappers).anyMatch( actual -> expected.equals(actual.trim()) )
                );
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}

