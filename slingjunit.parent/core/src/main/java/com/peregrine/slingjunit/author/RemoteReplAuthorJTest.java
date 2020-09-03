package com.peregrine.slingjunit.author;


import com.peregrine.adaption.PerReplicable;
import com.peregrine.replication.Replication;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
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
import java.util.*;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import static com.peregrine.commons.util.PerConstants.PER_REPLICATION;
import static org.apache.jackrabbit.JcrConstants.JCR_LASTMODIFIED;
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

    @TestReference(name="remote")
    private Replication remoteReplication;

    private Calendar beforeTime;

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
    private static String STELLA_PNG = "/content/example/assets/images/Stella.png";
    private static String CONTACT_PATH = "/content/example/pages/contact";
    private static String COMPONENT_PATH= "/content/example/pages/contact/jcr:content/n3736dc36-9cc3-49d7-a7d4-bf4d94e0ea2f";
    private Resource stellaImgRes;
    private static String PUBLISH_DOMAIN = "http://localhost:8180";

    @Before
    public void setup(){
        try {
            beforeTime = Calendar.getInstance();
            adminResourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            assertNotNull(remoteReplication);
            stellaImgRes = adminResourceResolver.getResource(STELLA_PNG);

        } catch (LoginException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void cleanup(){
        adminResourceResolver.close();
        adminResourceResolver = null;
    }

    @Test
    public void authorRunmodes() {
        Set<String> runmmodes = settingsService.getRunModes();
        assertTrue(runmmodes.contains("author"));
        assertTrue(runmmodes.contains("notshared"));
    }

    @Test
    public void replicationMixin(){
        assertReplicationMixin(CONTACT_PATH);
        assertReplicationMixin(CONTACT_PATH+"/jcr:content");
        assertReplicationMixin(STELLA_PNG);
        assertReplicationMixin(STELLA_PNG+"/jcr:content");
        assertReplicationMixin(COMPONENT_PATH);
    }

    private void assertReplicationMixin(String resourcePath){
        Resource res = adminResourceResolver.getResource(resourcePath);
        Node node = res.adaptTo(Node.class);
        try {
            assertTrue(node.canAddMixin(PER_REPLICATION));
        } catch (RepositoryException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void authorDistributionServiceUserAccess() {
        List<String> readList = Arrays.asList("/var/sling/distribution", "/content");
        List<String> allList = Arrays.asList("/var/sling/distribution/packages");
        testListGranted(readList, "jcr:read", "distribution-agent-user");
        testListGranted(allList, "jcr:all", "distribution-agent-user");
    }



    @Test
    public void authorDistributionServiceUserMappings() {
        testOsgiStringArrayProps(MAPPER_DISTRIBUTION_EVENT_PID, USER_MAPPING, DIST_EVENT_VALUES);
        testOsgiStringArrayProps(MAPPER_DISTRIBUTION_SERVICE_PID, USER_MAPPING, DIST_SERVICE_VALUES);
    }

    @Test
    public void replicateOneAsset() {
        deactivateResource(STELLA_PNG);
        assertStatusOnPublish(STELLA_PNG, 404);
        PerReplicable stellaImgRepl = stellaImgRes.adaptTo(PerReplicable.class);
        assertFalse(stellaImgRepl.isReplicated());
        try {
            remoteReplication.replicate(stellaImgRes, true);
            assertStatusOnPublish(STELLA_PNG, 200);
            assertTrue(stellaImgRepl.isReplicated());

            stellaImgRepl.getModifiableProperties().put(JCR_LASTMODIFIED, Calendar.getInstance());
            assertNotNull(stellaImgRepl.getReplicated());
            assertTrue(stellaImgRepl.isStale());

        } catch (Replication.ReplicationException e) {
            fail(e.getMessage());
        }
        deactivateResource(STELLA_PNG);
        assertStatusOnPublish(STELLA_PNG, 404);

        stellaImgRepl = adminResourceResolver.getResource(STELLA_PNG).adaptTo(PerReplicable.class);
        assertFalse(stellaImgRepl.isReplicated());
        assertFalse(stellaImgRepl.isStale());
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


    private void assertStatusOnPublish(String path, int status) {
        HttpUriRequest request = new HttpGet( PUBLISH_DOMAIN+path);

        int retries = 10;
        int attemptStatus = 0;
        try {
            while (attemptStatus != status && retries-- > 0) {
                Thread.sleep(1000);
                HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );
                attemptStatus = httpResponse.getStatusLine().getStatusCode();
            }
            assertEquals(status, attemptStatus );
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private void deactivateResource(String path) {
        try {
            remoteReplication.deactivate(stellaImgRes);
        } catch (Replication.ReplicationException e) {
            fail(e.getMessage());
        }
        assertStatusOnPublish(path, 404);
    }

}

