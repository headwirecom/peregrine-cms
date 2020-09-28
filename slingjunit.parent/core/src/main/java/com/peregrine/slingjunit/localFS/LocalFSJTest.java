package com.peregrine.slingjunit.localFS;

import com.peregrine.adaption.PerReplicable;
import com.peregrine.replication.Replication;
import com.peregrine.slingjunit.ReplicationTestBase;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.Calendar;
import static com.peregrine.slingjunit.author.RemoteReplAuthorJTest.STELLA_PNG;
import static org.apache.jackrabbit.JcrConstants.JCR_LASTMODIFIED;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

@RunWith(SlingAnnotationsTestRunner.class)
public class LocalFSJTest extends ReplicationTestBase {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Calendar beforeTime;
    @TestReference
    private ResourceResolverFactory resolverFactory;
    private ResourceResolver adminResourceResolver = null;

//    NOTE: There may be up to 3 types of replication services registered: localFS, remote, local
//    If possible, remove OSGI configs for unused replication service implementations; remote and local service, before running this test.
//    Otherwise, since the following line is non-deterministic, it could inject any of the replication services configured.
    @TestReference
    private Replication replication;

    private Resource stellaImgRes;
    private static String STATIC_HOME = "./sling/staticreplication";

    @Before
    public void setup(){
        try {
            beforeTime = Calendar.getInstance();
            adminResourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
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
    public void setupIsCorrect(){
        assertNotNull(replication);
        assertTrue("localFS".equals(replication.getName()));
        assertNotNull(stellaImgRes);
    }

    @Test
    public void staticreplicationExists() {
        assertFileExists(STATIC_HOME);
    }


    @Test
    public void replicateOneAsset() {
        // assert test starts with the file unpublished
        assertFileDoesNotExist(STATIC_HOME+STELLA_PNG);
        PerReplicable stellaImgRepl = stellaImgRes.adaptTo(PerReplicable.class);
        assertFalse(stellaImgRepl.isReplicated());
        try {
            // publish the file
            replication.replicate(stellaImgRes, true);
            assertFileExists(STATIC_HOME+STELLA_PNG);
            assertTrue(stellaImgRepl.isReplicated());
            // touch the asset modified date after publishing
            stellaImgRepl.getModifiableProperties().put(JCR_LASTMODIFIED, Calendar.getInstance());
            assertNotNull(stellaImgRepl.getReplicated());
            // verify the resource reports as stale
            assertTrue(stellaImgRepl.isStale());

        } catch (Replication.ReplicationException e) {
            fail(e.getMessage());
        }
        // unpublish the file
        deactivateResource(STELLA_PNG, stellaImgRes, replication);
        // assert the file is unpublished
        assertFileDoesNotExist(STATIC_HOME+STELLA_PNG);
        // verify the resource accurately reports publication status
        stellaImgRepl = adminResourceResolver.getResource(STELLA_PNG).adaptTo(PerReplicable.class);
        assertFalse(stellaImgRepl.isReplicated());
        assertFalse(stellaImgRepl.isStale());
    }

    private void assertFileExists(String filePath){
        File file = new File(filePath);
        assertTrue(file.exists());
    }

    private void assertFileDoesNotExist(String filePath){
        File file = new File(filePath);
        assertFalse(file.exists());
    }
}
