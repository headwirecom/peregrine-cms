package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.commons.test.AbstractTest;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolder;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createObject;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createTemplate;
import static com.peregrine.it.util.TestHarness.deleteNode;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Andreas Schaefer on 6/30/17.
 */
public class DeleteNodeIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/delete-page";

    private static final Logger logger = LoggerFactory.getLogger(DeleteNodeIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() {
        SlingClient client = slingInstanceRule.getAdminClient();
        try {
            client.doDelete(ROOT_PATH, null, null, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        }
    }

    @Test
    public void testPageDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-pd";
        String nodeName = "new-page";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createPage(client, rootFolderPath, nodeName, EXAMPLE_TEMPLATE_PATH, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, nodeName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + nodeName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deleteNode(client, rootFolderPath + "/" + nodeName, 200);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Node (Page) is still there", rootFolder.containsKey(nodeName));
    }

    @Test
    public void testObjectDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-od";
        String nodeName = "new-object";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createObject(client, rootFolderPath, nodeName, EXAMPLE_TEMPLATE_PATH, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, OBJECT_PRIMARY_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_TEMPLATE_PATH);
        json.writeStringField(JCR_TITLE, nodeName);
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + nodeName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deleteNode(client, rootFolderPath + "/" + nodeName, 200);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Node (Object) is still there", rootFolder.containsKey(nodeName));
    }

    @Test
    public void testObjectWithTypeDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-owtd";
        String nodeName = "new-object";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createObject(client, rootFolderPath, nodeName, EXAMPLE_TEMPLATE_PATH, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, OBJECT_PRIMARY_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_TEMPLATE_PATH);
        json.writeStringField(JCR_TITLE, nodeName);
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + nodeName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deleteNode(client, rootFolderPath + "/" + nodeName, OBJECT_PRIMARY_TYPE, 200);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Node (Object) is still there", rootFolder.containsKey(nodeName));
    }

    @Test
    public void testTemplateDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-td";
        String nodeName = "new-template";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createTemplate(client, rootFolderPath, nodeName, "example/components/page", 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, nodeName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + nodeName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deleteNode(client, rootFolderPath + "/" + nodeName, 200);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Node (Template) is still there", rootFolder.containsKey(nodeName));
    }

    @Test
    public void testTemplateWithTypeDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-twtd";
        String nodeName = "new-template";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createTemplate(client, rootFolderPath, nodeName, "example/components/page", 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, nodeName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + nodeName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deleteNode(client, rootFolderPath + "/" + nodeName, PAGE_PRIMARY_TYPE, 200);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Node (Template) is still there", rootFolder.containsKey(nodeName));
    }

    @Test
    public void testFolderDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-fd";
        String nodeName = "new-folder";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createFolder(client, rootFolderPath, nodeName, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, SLING_ORDERED_FOLDER);
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + nodeName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deleteNode(client, rootFolderPath + "/" + nodeName, 200);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Node (Folder) is still there", rootFolder.containsKey(nodeName));
    }

    @Test
    public void testFolderWithTypeDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-fwtd";
        String nodeName = "new-folder";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createFolder(client, rootFolderPath, nodeName, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, SLING_ORDERED_FOLDER);
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + nodeName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deleteNode(client, rootFolderPath + "/" + nodeName, SLING_ORDERED_FOLDER, 200);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Node (Folder) is still there", rootFolder.containsKey(nodeName));
    }

    @Test
    public void testFolderWithBadTypeDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-fwbtd";
        String nodeName = "new-folder";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createFolder(client, rootFolderPath, nodeName, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, SLING_ORDERED_FOLDER);
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + nodeName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deleteNode(client, rootFolderPath + "/" + nodeName, PAGE_PRIMARY_TYPE, 400);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertTrue("Failed Removed Node (Folder) is not there", rootFolder.containsKey(nodeName));
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
