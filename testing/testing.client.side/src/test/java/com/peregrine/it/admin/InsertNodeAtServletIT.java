package com.peregrine.it.admin;

import com.peregrine.it.basic.AbstractTest;
import com.peregrine.it.basic.JsonTest.TestPage;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.it.basic.BasicTestHelpers.checkFolder;
import static com.peregrine.it.basic.BasicTestHelpers.checkFolders;
import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.basic.BasicTestHelpers.extractChildNodes;
import static com.peregrine.it.util.TestHarness.deleteLeafFolder;
import static com.peregrine.it.util.TestHarness.insertNodeAtAsComponent;
import static com.peregrine.it.util.TestHarness.insertNodeAtAsContent;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by schaefa on 6/22/17.
 */
public class InsertNodeAtServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/test-insert-node-at";


    private static final Logger logger = LoggerFactory.getLogger(InsertNodeAtServletIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() {
        SlingClient client = slingInstanceRule.getAdminClient();
        if(!deleteLeafFolder(client, ROOT_PATH)) {
            fail("Could not delete Leaf Folder: " + ROOT_PATH);
        }
    }

    @Test
    public void testInsertNodeIntoEmptyFolderAsComponent() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String sourcePath = ROOT_PATH + "/source";
        String targetPath = ROOT_PATH + "/target/target-iniefaCom";
        createFolderStructure(client, sourcePath);
        createFolderStructure(client, targetPath);
        // Insert a new Page to that folder
        Calendar before = createTimestampAndWait();
        SlingHttpResponse response = insertNodeAtAsComponent(client, targetPath, "admin/components/col", "into", 302);
        // List the children and check if there is a folder
        Map children = extractChildNodes(listResourceAsJson(client, targetPath, 1));
        assertFalse("No nodes were created", children.isEmpty());
        assertEquals("There are more than one node", 1, children.size());
        checkLastModified(client, targetPath + "/" + children.keySet().iterator().next().toString(), before);
    }

    @Test
    public void testInsertNodeIntoEmptyFolderAsContent() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String sourcePath = ROOT_PATH + "/source";
        String targetPath = ROOT_PATH + "/target/target-iniefaCon";
        createFolderStructure(client, sourcePath);
        createFolderStructure(client, targetPath);
        // Insert a new Page to that folder
        Calendar before = createTimestampAndWait();;
        SlingHttpResponse response = insertNodeAtAsContent(client, targetPath, "{\"component\":\"admin/components/col\", \"test\": \"test-one\"}", "into", 302);
        // List the children and check if there is a folder
        Map children = extractChildNodes(listResourceAsJson(client, targetPath, 1));
        assertFalse("No nodes were created", children.isEmpty());
        assertEquals("There are more than one node", 1, children.size());
        checkLastModified(client, targetPath + "/" + children.keySet().iterator().next().toString(), before);
        Map child = (Map) children.values().iterator().next();
        assertFalse("Node does not contain properties", child.isEmpty());
        assertTrue("Node does not contain test properties", child.containsKey("test"));
        assertEquals("Node test property does not contain the correct value", "test-one", child.get("test"));
    }

    @Test
    public void testInsertNodeIntoFolderFirst() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String sourcePath = ROOT_PATH + "/source";
        String targetPath = ROOT_PATH + "/target/target-iniff";
        createFolderStructure(client, sourcePath);
        createFolderStructure(client, targetPath);
        // Insert a new Page to that folder
        Calendar before = createTimestampAndWait();;
        SlingHttpResponse response = insertNodeAtAsContent(client, targetPath, "{\"component\":\"admin/components/col\", \"test\": \"test-one\"}", "into", 302);
        // List the children and check if there is a folder
        Map children = extractChildNodes(listResourceAsJson(client, targetPath, 1));
        assertFalse("No initial node were created", children.isEmpty());
        assertEquals("There are more than one node", 1, children.size());
        String firstNodeName = children.keySet().iterator().next() + "";
        response = insertNodeAtAsContent(client, targetPath, "{\"component\":\"admin/components/col\", \"test\": \"test-two\"}", "into-before", 302);
        children = extractChildNodes(listResourceAsJson(client, targetPath, 1));
        assertFalse("No nodes found", children.isEmpty());
        assertEquals("Unexpected number of nodes found", 2, children.size());
        Iterator<Entry> i = children.entrySet().iterator();
        Entry first = i.next();
        Entry second = i.next();
        logger.info("Original First: '{}', new first: '{}'", firstNodeName, first.getKey());
        assertNotEquals("Initial must be second", firstNodeName, first.getKey());
        assertEquals("Second node must be the initially created one", firstNodeName, second.getKey());
        checkLastModified(client, targetPath + "/" + first.getKey().toString(), before);
        Map firstChild = (Map) first.getValue();
        Map secondChild = (Map) second.getValue();
        assertFalse("1st Node does not contain properties", firstChild.isEmpty());
        assertTrue("1st Node does not contain test properties", firstChild.containsKey("test"));
        assertEquals("1st Node test property does not contain the correct value", "test-two", firstChild.get("test"));
        assertFalse("2nd Node does not contain properties", secondChild.isEmpty());
        assertTrue("2nd Node does not contain test properties", secondChild.containsKey("test"));
        assertEquals("2nd Node test property does not contain the correct value", "test-one", secondChild.get("test"));
    }

    @Test
    public void testInsertNodeIntoFolderLast() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String sourcePath = ROOT_PATH + "/source";
        String targetPath = ROOT_PATH + "/target/target-inifl";
        createFolderStructure(client, sourcePath);
        createFolderStructure(client, targetPath);
        // Insert a new Page to that folder
        Calendar before = createTimestampAndWait();
        SlingHttpResponse response = insertNodeAtAsContent(client, targetPath, "{\"component\":\"admin/components/col\", \"test\": \"test-one\"}", "into", 302);
        // List the children and check if there is a folder
        Map children = extractChildNodes(listResourceAsJson(client, targetPath, 1));
        logger.info("Insert first node into folder: '{}', Map: '{}'", targetPath, children);
        assertFalse("No initial node were created", children.isEmpty());
        assertEquals("There are more than one node", 1, children.size());
        String firstNodeName = children.keySet().iterator().next() + "";
        checkLastModified(client, targetPath + "/" + firstNodeName, before);

        before = createTimestampAndWait();
        response = insertNodeAtAsContent(client, targetPath, "{\"component\":\"admin/components/col\", \"test\": \"test-two\"}", "into-after", 302);
        children = extractChildNodes(listResourceAsJson(client, targetPath, 1));
        logger.info("Insert second node into folder: '{}', Map: '{}'", targetPath, children);
        assertFalse("No nodes found", children.isEmpty());
        assertEquals("Unexpected number of nodes found", 2, children.size());
        Iterator<Entry> i = children.entrySet().iterator();
        Entry first = i.next();
        Entry second = i.next();
        logger.info("Original First: '{}', Current first: '{}'", firstNodeName, first.getKey());
        assertEquals("Initial must be first", firstNodeName, first.getKey());
        logger.info("Current second: '{}'", second.getKey());
        assertNotEquals("Second node must be the later created one", firstNodeName, second.getKey());
        checkLastModified(client, targetPath + "/" + second.getKey().toString(), before);
        Map firstChild = (Map) first.getValue();
        Map secondChild = (Map) second.getValue();
        assertFalse("1st Node does not contain properties", firstChild.isEmpty());
        assertTrue("1st Node does not contain test properties", firstChild.containsKey("test"));
        assertEquals("1st Node test property does not contain the correct value", "test-one", firstChild.get("test"));
        assertFalse("2nd Node does not contain properties", secondChild.isEmpty());
        assertTrue("2nd Node does not contain test properties", secondChild.containsKey("test"));
        assertEquals("2nd Node test property does not contain the correct value", "test-two", secondChild.get("test"));
    }

    @Test
    public void testCreateComponentWithDefaults() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();

        // Create Page
        String rootFolderPath = ROOT_PATH + "/test-ccwd";
        String pageName = "testPage";
        // Create the folder structure
        createFolderStructure(client, rootFolderPath);
        createPage(client, rootFolderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, rootFolderPath + "/" + pageName, 2, testPage.toJSon(), true);

        // Create Component Node with the Test Component
        String componentPath = rootFolderPath + "/" + pageName + "/" + JCR_CONTENT;
        SlingHttpResponse response = insertNodeAtAsContent(client, componentPath,
            "{\"component\":\"it/components/test\", \"test\": \"test-one\"}", "into", 302);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
