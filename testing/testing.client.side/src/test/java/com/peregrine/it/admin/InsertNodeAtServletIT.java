package com.peregrine.it.admin;

import com.peregrine.commons.test.AbstractTest;
import com.peregrine.it.basic.JsonTest.BasicObject;
import com.peregrine.it.basic.JsonTest.NoNameObject;
import com.peregrine.it.basic.JsonTest.Prop;
import com.peregrine.it.basic.JsonTest.TestPage;
import com.peregrine.it.basic.JsonTest.TestTemplate;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.test.TestUtil.compareJson;
import static com.peregrine.commons.test.TestUtil.convertJsonTextToMap;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.basic.BasicTestHelpers.extractChildNodes;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createTemplate;
import static com.peregrine.it.util.TestHarness.deleteLeafFolder;
import static com.peregrine.it.util.TestHarness.insertNodeAtAsComponent;
import static com.peregrine.it.util.TestHarness.insertNodeAtAsContent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Andreas Schaefer on 6/22/17.
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
    public void testCreatePageWithComponentDefaults() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();

        // Create Page
        String rootFolderPath = ROOT_PATH + "/test-cpwcd";
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
        // Default Nodes to the Test Page and check if the match
        // List the children and check if there is a folder
        Map children = extractChildNodes(listResourceAsJson(client, componentPath, 3));
        logger.info("Page Component Map: '{}'", children);
        NoNameObject componentObject = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType("it/components/test")
            .addProperties(new Prop("prop1", "value1"), new Prop("prop2", "value2"))
            .addChildren(
                new BasicObject("child1", NT_UNSTRUCTURED)
                    .addProperties(new Prop("child1Prop1", "child1Value1"), new Prop("child1Prop2", "child1Value2"))
                    .addChildren(
                        new BasicObject("grandchild1", NT_UNSTRUCTURED)
                            .addProperties(new Prop("grandchild1Prop1", "grandchild1Value1"), new Prop("grandchild1Prop2", "grandchild1Value2"))
                    ),
                new BasicObject("child2", NT_UNSTRUCTURED)
                    .addProperties(new Prop("child2Prop1", "child2Value1"), new Prop("child2Prop2", "child2Value2"))
                    .addChildren(
                        new BasicObject("grandchild2", NT_UNSTRUCTURED)
                            .addProperties(new Prop("grandchild2Prop1", "grandchild2Value1"), new Prop("grandchild2Prop2", "grandchild2Value2"))
                    )
            );
        Map expected = convertJsonTextToMap(componentObject.toJSon());
        Map actual = (Map) children.values().iterator().next();
        logger.info("Expected Map: '{}'", expected);
        logger.info("Actual Map: '{}'", actual);
        compareJson(expected, actual);
    }

    @Test
    public void testCreatePageWithComponentVariation() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();

        // Create Page
        String rootFolderPath = ROOT_PATH + "/test-cpwcv";
        String pageName = "testPage";
        // Create the folder structure
        createFolderStructure(client, rootFolderPath);
        createPage(client, rootFolderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, rootFolderPath + "/" + pageName, 2, testPage.toJSon(), true);

        // Create Component Node with the Test Component
        String componentPath = rootFolderPath + "/" + pageName + "/" + JCR_CONTENT;
        SlingHttpResponse response = insertNodeAtAsContent(client, componentPath,
            "{\"component\":\"it/components/testvariation\", \"test\": \"test-one\"}", "into", "v2", 302);
        // Default Nodes to the Test Page and check if the match
        // List the children and check if there is a folder
        Map children = extractChildNodes(listResourceAsJson(client, componentPath, 3));
        logger.info("Page Component Map: '{}'", children);
        NoNameObject componentObject = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType("it/components/testvariation")
            .addProperties(new Prop("v2-prop-1", "v2-value-1"), new Prop("v2-prop-2", "v2-value-2")
        );
        Map expected = convertJsonTextToMap(componentObject.toJSon());
        Map actual = (Map) children.values().iterator().next();
        logger.info("Expected Map: '{}'", expected);
        logger.info("Actual Map: '{}'", actual);
        compareJson(expected, actual);
    }

    @Test
    public void testCreatePageWithComponentDefaultVariation() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();

        // Create Page
        String rootFolderPath = ROOT_PATH + "/test-cpwcdv";
        String pageName = "testPage";
        // Create the folder structure
        createFolderStructure(client, rootFolderPath);
        createPage(client, rootFolderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, rootFolderPath + "/" + pageName, 2, testPage.toJSon(), true);

        // Create Component Node with the Test Component
        String componentPath = rootFolderPath + "/" + pageName + "/" + JCR_CONTENT;
        SlingHttpResponse response = insertNodeAtAsContent(client, componentPath,
            "{\"component\":\"it/components/testvariation\", \"test\": \"test-one\"}", "into", 302);
        // Default Nodes to the Test Page and check if the match
        // List the children and check if there is a folder
        Map children = extractChildNodes(listResourceAsJson(client, componentPath, 3));
        logger.info("Page Component Map: '{}'", children);
        NoNameObject componentObject = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType("it/components/testvariation")
            .addProperties(new Prop("v1-prop-1", "v1-value-1"), new Prop("v1-prop-2", "v1-value-2")
        );
        Map expected = convertJsonTextToMap(componentObject.toJSon());
        Map actual = (Map) children.values().iterator().next();
        logger.info("Expected Map: '{}'", expected);
        logger.info("Actual Map: '{}'", actual);
        compareJson(expected, actual);
    }

    @Test
    public void testCreateTemplateWithDefaults() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();

        // Create Page
        String rootFolderPath = ROOT_PATH + "/test-ctwd";
        String templateName = "testTemplate";
        // Create the folder structure
        createFolderStructure(client, rootFolderPath);
        // Create Component
        createTemplate(client, rootFolderPath, templateName, "it/components/test",200);
        TestTemplate testTemplate = (TestTemplate) new TestTemplate(templateName, "it/components/test")
            .addContentChildren(
                new BasicObject("child1", NT_UNSTRUCTURED)
                    .addProperties(new Prop("child1Prop1", "child1Value1"), new Prop("child1Prop2", "child1Value2"))
                    .addChildren(
                        new BasicObject("grandchild1", NT_UNSTRUCTURED)
                            .addProperties(new Prop("grandchild1Prop1", "grandchild1Value1"), new Prop("grandchild1Prop2", "grandchild1Value2"))
                    ),
                new BasicObject("child2", NT_UNSTRUCTURED)
                    .addProperties(new Prop("child2Prop1", "child2Value1"), new Prop("child2Prop2", "child2Value2"))
                    .addChildren(
                        new BasicObject("grandchild2", NT_UNSTRUCTURED)
                            .addProperties(new Prop("grandchild2Prop1", "grandchild2Value1"), new Prop("grandchild2Prop2", "grandchild2Value2"))
                    )
            )
            .addContentProperty(new Prop("prop1", "value1"))
            .addContentProperty(new Prop("prop2", "value2"))
            ;
        checkResourceByJson(client, rootFolderPath + "/" + templateName, 4, testTemplate.toJSon(), true);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
