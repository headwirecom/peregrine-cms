package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.admin.replication.impl.LocalFileSystemReplicationService;
import com.peregrine.it.basic.AbstractTest;
import com.peregrine.it.basic.JsonTest.TestPage;
import org.apache.http.Header;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.osgi.ComponentInfo;
import org.apache.sling.testing.clients.osgi.ComponentsInfo;
import org.apache.sling.testing.clients.osgi.OsgiConsoleClient;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.it.basic.BasicTestHelpers.checkFile;
import static com.peregrine.it.basic.BasicTestHelpers.checkFolderAndCreate;
import static com.peregrine.it.basic.BasicTestHelpers.checkPages;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.checkResponse;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createOSGiServiceConfiguration;
import static com.peregrine.it.basic.BasicTestHelpers.listComponentsAsJson;
import static com.peregrine.it.basic.BasicTestHelpers.listServicesAsJson;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createTemplate;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.executeReplication;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    public void testSimplePageReplication() throws Exception {
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
    public void testLocalFSPageReplication() throws Exception {
        checkFolderAndCreate(LOCAL_FOLDER, true);
        SlingClient client = slingInstanceRule.getAdminClient();
        OsgiConsoleClient osgiConsoleClient = new OsgiConsoleClient(client.getUrl(), client.getUser(), client.getPassword());

        // Create Local FS Replication Configuration
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "localFSCreationTest");
        properties.put("description", "localFSCreationTest - creation test");
        properties.put("targetFolder", LOCAL_FOLDER.getAbsolutePath());
        properties.put("exportExtensions", new String[] {"data.json=per:Page|per:Template", "infinity.json=per:Object", "html=per:Page|per:Template", "*~raw=nt:file"});
        properties.put("mandatoryRenditions", "thumbnail.png");
        String fPid = LocalFileSystemReplicationService.class.getName();
        String pid = createOSGiServiceConfiguration(client, fPid, properties);
        logger.info("Newly Create Configuration, PID: '{}'", pid);
        assertNotNull("No PID for the Local FS Replication Service was returned",pid);
        assertTrue("Given PID: '" + pid + "' does not start with: '" + fPid, pid.startsWith(fPid));

        Map<String, Object> updatedServiceConfiguration = osgiConsoleClient.getConfiguration(pid, 200);
        // Migrate any arrays to lists to report the values here
        for(Entry<String, Object> entry: updatedServiceConfiguration.entrySet()) {
            String key = entry.getKey();
            Object temp = entry.getValue();
            if(temp instanceof Object[]) {
                temp = Arrays.asList((Object[]) temp);
                logger.info("Update Entry: '{}' to: '{}'", key, temp);
                entry.setValue(temp);
            }
        }
        logger.info("Updated Local FS Replication Component: '{}'", updatedServiceConfiguration);

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
        executeReplication(client, rootFolderPath + "/" + pageName, "localFSCreationTest", 200);

        // Check page and template on the File System
        String[] folderNames = rootFolderPath.split("/");
        logger.info("Folder Paths: '{}'", Arrays.asList(folderNames));
        File folder = LOCAL_FOLDER;
        logger.info("Root Folder: '{}'", folder.getAbsolutePath());
        for(String folderName: folderNames) {
            if(isNotEmpty(folderName)) {
                File[] children = folder.listFiles();
                logger.info("Children Files: '{}'", Arrays.asList(children));
                boolean found = false;
                for(File child : children) {
                    logger.info("Check Child Folder: '{}'", folder.getAbsolutePath());
                    if(child.getName().equals(folderName)) {
                        folder = child.getAbsoluteFile();
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    fail("Child Folder: '" + folderName + "' of Parent: '" + folder.getAbsolutePath() + "' not found");
                }
            }
        }
        // Check the Page File
        checkFile(new File(folder, pageName + ".data.json"), "Page - JSon", false);
        checkFile(new File(folder, pageName + ".html"), "Page - HTML", false);
        checkFile(new File(folder, templateName + ".data.json"), "Template - JSon", false);
    }

    private Map getComponentByName(SlingClient client, String name) throws IOException, ClientException {
        Map componentsMap = listComponentsAsJson(client);
        List data = (List) componentsMap.get("data");
        Map localFSReplicationMap = null;
        for(Object temp: data) {
            if(temp instanceof Map) {
                Map componentMap = (Map) temp;
                String pid = componentMap.get("pid") + "";
                if(LocalFileSystemReplicationService.class.getName().equals(pid)) {
                    logger.info("Found Local FS Replication Component: '{}'", componentMap);
                    localFSReplicationMap = componentMap;
                    break;
                }
            }
        }
        return localFSReplicationMap;
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

    @Override
    public Logger getLogger() {
        return logger;
    }
}
