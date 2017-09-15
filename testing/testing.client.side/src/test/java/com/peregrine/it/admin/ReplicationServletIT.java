package com.peregrine.it.admin;

import com.peregrine.admin.replication.impl.LocalFileSystemReplicationService;
import com.peregrine.admin.replication.impl.RemoteS3SystemReplicationService;
import com.peregrine.it.basic.AbstractTest;
import com.peregrine.it.basic.JsonTest.TestAsset;
import com.peregrine.it.basic.JsonTest.TestPage;
import com.peregrine.transform.operation.ThumbnailImageTransformation;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.osgi.OsgiConsoleClient;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.it.basic.BasicTestHelpers.IMAGE_PNG_MIME_TYPE;
import static com.peregrine.it.basic.BasicTestHelpers.checkFile;
import static com.peregrine.it.basic.BasicTestHelpers.checkFolderAndCreate;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createOSGiServiceConfiguration;
import static com.peregrine.it.basic.BasicTestHelpers.findFolderByPath;
import static com.peregrine.it.basic.BasicTestHelpers.getStringOrNull;
import static com.peregrine.it.basic.BasicTestHelpers.listComponentsAsJson;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.basic.BasicTestHelpers.listServicesAsJson;
import static com.peregrine.it.basic.BasicTestHelpers.loadFile;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createTemplate;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.executeReplication;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.it.util.TestHarness.uploadFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by schaefa on 6/22/17.
 */
public class ReplicationServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/replication-source";
    public static final String LIVE_ROOT_PATH = "/live/tests/replication-source";
    public static final String REPLICATION_PATH = "/live";
    public static final File LOCAL_FOLDER = new File("target/tests/local-fs-replication");
    public static final String IMAGE_RESOURCES_PATH = "src/test/resources/images";

    private static final Logger logger = LoggerFactory.getLogger(ReplicationServletIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() {
        SlingClient client = slingInstanceRule.getAdminClient();
        try {
            deleteFolder(client, ROOT_PATH, 200);
            deleteFolder(client, LIVE_ROOT_PATH, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        } catch(IOException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        }
    }

    @Test
    public void testInSlingPageReplication() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String rootFolderPath = ROOT_PATH + "/test-spr";
        String liveRootFolderPath = LIVE_ROOT_PATH + "/test-spr";
        createFolderStructure(client, rootFolderPath);
        // First Create a Template, then a Page using it
        String templateName = "replication-template";
        createTemplate(client, rootFolderPath, templateName, EXAMPLE_PAGE_TYPE_PATH, 200);
        TestPage testTemplate = new TestPage(templateName, EXAMPLE_PAGE_TYPE_PATH, null);
        checkResourceByJson(client, rootFolderPath + "/" + templateName, 2, testTemplate.toJSon(), true);

        String pageName = "replication-page";
        createPage(client, rootFolderPath, pageName, rootFolderPath + "/" + templateName, 200);
        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, rootFolderPath + "/" + templateName);
        checkResourceByJson(client, rootFolderPath + "/" + pageName, 2, testPage.toJSon(), true);

        // Create Target Replication Folder
        createFolderStructure(client, REPLICATION_PATH);

        // Replicate the Page and check its new content
        executeReplication(client, rootFolderPath + "/" + pageName, "local", 200);

        // Check page and template
        TestPage testLivePage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, liveRootFolderPath + "/" + templateName);
        checkResourceByJson(client, liveRootFolderPath + "/" + pageName, 2, testLivePage.toJSon(), true);
        TestPage testLiveTemplate = new TestPage(templateName, EXAMPLE_PAGE_TYPE_PATH, null);
        checkResourceByJson(client, liveRootFolderPath + "/" + templateName, 2, testLiveTemplate.toJSon(), true);
    }

    @Test
    public void testInSlingAssetReplication() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String rootFolderPath = ROOT_PATH + "/test-sar";
        String liveRootFolderPath = LIVE_ROOT_PATH + "/test-sar";
        String imageName = "test.png";
        createFolderStructure(client, rootFolderPath);

        // Create Target Replication Folder
        createFolderStructure(client, REPLICATION_PATH);

        // Get Image Content and upload it. Then check if Asset exists
        byte[] imageContent = loadFile(IMAGE_RESOURCES_PATH, imageName, "Test Image - PNG");
        uploadFile(client, rootFolderPath, imageName, imageContent, IMAGE_PNG_MIME_TYPE,200);

        // Replicate the Page and check its new content
        executeReplication(client, rootFolderPath + "/" + imageName, "local", 200);

        // Check page and template
        TestAsset testAsset = new TestAsset(imageName, IMAGE_PNG_MIME_TYPE);
        checkResourceByJson(client, liveRootFolderPath + "/" + imageName, 2, testAsset.toJSon(), true);
    }

    @Test
    public void testLocalFSPageReplication() throws Exception {
        checkFolderAndCreate(LOCAL_FOLDER, true);
        SlingClient client = slingInstanceRule.getAdminClient();
        OsgiConsoleClient osgiConsoleClient = new OsgiConsoleClient(client.getUrl(), client.getUser(), client.getPassword());
        String replicationName = "localFSPageCreationTest";

        createLocalReplicationConfiguration(client, replicationName, LOCAL_FOLDER);

//AS TODO: The search for components by name or id in ComponentsInfo is not working (bug) so we do it manually for now
//        ComponentsInfo componentsInfo = osgiConsoleClient.getComponentsInfo(200);
//        logger.info("Components Info: '{}", componentsInfo);
//        ComponentInfo localFSComponent = componentsInfo.forName(LocalFileSystemReplicationService.class.getName());
//        logger.info("Local FS Component Info, Id: '{}', Name: '{}', PID: '{}', Status: '{}'", localFSComponent.getId(), localFSComponent.getName(), localFSComponent.getPid(), localFSComponent.getStatus());

        String rootFolderPath = ROOT_PATH + "/test-lfspr";
        createFolderStructure(client, rootFolderPath);
        // First Create a Template, then a Page using it
        String templateName = "replication-template";
        createTemplate(client, rootFolderPath, templateName, EXAMPLE_PAGE_TYPE_PATH, 200);
        TestPage testTemplate = new TestPage(templateName, EXAMPLE_PAGE_TYPE_PATH, null);
        checkResourceByJson(client, rootFolderPath + "/" + templateName, 2, testTemplate.toJSon(), true);

        String pageName = "replication-page";
        createPage(client, rootFolderPath, pageName, rootFolderPath + "/" + templateName, 200);
        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, rootFolderPath + "/" + templateName);
        checkResourceByJson(client, rootFolderPath + "/" + pageName, 2, testPage.toJSon(), true);

        // Replicate the Page and check its new content
        executeReplication(client, rootFolderPath + "/" + pageName, replicationName, 200);

        // Check page and template on the File System
        File folder = findFolderByPath(LOCAL_FOLDER, rootFolderPath);
        // Check the Page File
        checkFile(new File(folder, pageName + ".data.json"), "Page - JSon", false);
        checkFile(new File(folder, pageName + ".html"), "Page - HTML", false);
        checkFile(new File(folder, templateName + ".data.json"), "Template - JSon", false);
        checkFile(new File(folder, templateName + ".html"), "Template - HTML", false);
    }

    @Test
    public void testLocalFSAssetReplication() throws Exception {
        checkFolderAndCreate(LOCAL_FOLDER, true);
        SlingClient client = slingInstanceRule.getAdminClient();
        OsgiConsoleClient osgiConsoleClient = new OsgiConsoleClient(client.getUrl(), client.getUser(), client.getPassword());
        String imageName = "test.png";
        String replicationName = "localFSAssetCreationTest";

        createLocalReplicationConfiguration(client, replicationName, LOCAL_FOLDER);

        Map<String, Object> tnProperties = new HashMap<>();
        tnProperties.put("enabled", "true");
        String tnPid = ThumbnailImageTransformation.class.getName();
        osgiConsoleClient.editConfiguration(tnPid, null, tnProperties,302);
        Map<String, Object> updatedServiceConfiguration = osgiConsoleClient.getConfiguration(tnPid, 200);
        boolean checkRenditions = true;
        if(!"true".equalsIgnoreCase(updatedServiceConfiguration.get("enabled") + "")) {
            logger.warn("Thumbnail Image Transformation is not enabled and hence it will not be tested");
            checkRenditions = false;
        }

        String rootFolderPath = ROOT_PATH + "/test-lfsar";
        createFolderStructure(client, rootFolderPath);

        // Get Image Content and upload it. Then check if Asset exists
        byte[] imageContent = loadFile(IMAGE_RESOURCES_PATH, imageName, "Test Image - PNG");
        uploadFile(client, rootFolderPath, imageName, imageContent, 200);
        Map asset = listResourceAsJson(client, rootFolderPath + "/" + imageName, 2);
        logger.info("Local FS Asset (Path: '{}') Replication in Sling: '{}'", rootFolderPath, asset);
        assertFalse("No Image Asset found after upload", asset.isEmpty());
        // Look for the JCR Primary Type and make sure it is an Asset
        assertTrue("No primary type found", asset.containsKey(JCR_PRIMARY_TYPE));
        assertEquals("Wrong Asset found after upload", ASSET_PRIMARY_TYPE, asset.get(JCR_PRIMARY_TYPE));

        // Replicate the Page and check its new content
        executeReplication(client, rootFolderPath + "/" + imageName, replicationName, 200);

        // Check page and template on the File System
        File folder = findFolderByPath(LOCAL_FOLDER, rootFolderPath);
        // Check the Page File
        checkFile(new File(folder, imageName), "Asset - PNG", false);
        if(checkRenditions) {
            checkFile(new File(folder, imageName + ".thumbnail.png"), "Asset - Thumbnail PNG", false);
        }
    }


    @Test
    public void testRemoteS3PageReplication() throws Exception {
        checkFolderAndCreate(LOCAL_FOLDER, true);
        SlingClient client = slingInstanceRule.getAdminClient();
        List<Map> remoteS3Components = getComponentByName(client, RemoteS3SystemReplicationService.class.getName());
        logger.info("List of Remote S3 Components: '{}'", remoteS3Components);
        String replicationName = "remoteS3IT";
        boolean isFound = false;
        for(Map componentMap: remoteS3Components) {
            if(componentMap.containsKey("stat")) {
                String state = getStringOrNull(componentMap, "state");
                String checkId = getStringOrNull(componentMap, "id");
                if("active".equals(state) && replicationName.equals(checkId)) {
                    isFound = true;
                    break;
                }
            }
        }
        if(isFound) {
            // Remote S3 Replication found and active -> start testing
            // First obtain the replication name to make sure
            String imageName = "test.png";
            String rootFolderPath = ROOT_PATH + "/test-lfsar";
            createFolderStructure(client, rootFolderPath);

            // Get Image Content and upload it. Then check if Asset exists
            byte[] imageContent = loadFile(IMAGE_RESOURCES_PATH, imageName, "Test Image - PNG");
            uploadFile(client, rootFolderPath, imageName, imageContent, 200);
            Map asset = listResourceAsJson(client, rootFolderPath + "/" + imageName, 2);
            logger.info("Local FS Asset (Path: '{}') Replication in Sling: '{}'", rootFolderPath, asset);
            assertFalse("No Image Asset found after upload", asset.isEmpty());
            // Look for the JCR Primary Type and make sure it is an Asset
            assertTrue("No primary type found", asset.containsKey(JCR_PRIMARY_TYPE));
            assertEquals("Wrong Asset found after upload", ASSET_PRIMARY_TYPE, asset.get(JCR_PRIMARY_TYPE));

            // Replicate the Page
            executeReplication(client, rootFolderPath + "/" + imageName, replicationName, 200);
            //AS TODO: Open an S3 Connection and test if the content made it there
        }
    }

        //AS TODO: Keep this method around we might night them later to investigate OSGi bundles, configurations and services
    private List<Map> getComponentByName(SlingClient client, String name) throws IOException, ClientException {
        List<Map> answer = new ArrayList<>();
        Map componentsMap = listComponentsAsJson(client);
        List data = (List) componentsMap.get("data");
        for(Object temp: data) {
            if(temp instanceof Map) {
                Map componentMap = (Map) temp;
                String pid = componentMap.get("pid") + "";
                if(pid.equals(name)) {
                    logger.info("Found OSGi Component: '{}'", componentMap);
                    answer.add(componentMap);
                }
            }
        }
        return answer;
    }

    private List getServicesByID(SlingClient client, String id) throws IOException, ClientException {
        List answer = new ArrayList();
        Map componentsMap = listServicesAsJson(client);
        List data = (List) componentsMap.get("data");
        for(Object temp: data) {
            if(temp instanceof Map) {
                Map serviceMap = (Map) temp;
                String pid = serviceMap.get("pid") + "";
                if(pid.startsWith(id)) {
                    logger.info("Found Local FS Replication Service: '{}'", serviceMap);
                    answer.add(serviceMap);
                    break;
                }
            }
        }
        return answer;
    }

    private void createLocalReplicationConfiguration(SlingClient client, String name, File folder) throws IOException, ClientException {
        // Create Local FS Replication Configuration
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", name);
        properties.put("description", name + " - creation test");
        properties.put("targetFolder", folder.getAbsolutePath());
        properties.put("exportExtensions", new String[] {"data.json=per:Page|per:Template", "infinity.json=per:Object", "html=per:Page|per:Template", "*~raw=nt:file"});
        properties.put("mandatoryRenditions", "thumbnail.png");
        String fPid = LocalFileSystemReplicationService.class.getName();
        String pid = createOSGiServiceConfiguration(client, fPid, properties);
        logger.info("Newly Create Configuration, PID: '{}'", pid);
        assertNotNull("No PID for the Local Replication Service: '" + name + "' was returned",pid);
        assertTrue("Given PID: '" + pid + "' does not start with: '" + fPid, pid.startsWith(fPid));
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
