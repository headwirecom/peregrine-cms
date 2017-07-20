package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.it.basic.AbstractTest;
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
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Map;

import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkPages;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.checkResponse;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.basic.BasicTestHelpers.extractChildNodes;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_JUMBOTRON_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_OBJECT_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createObject;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.insertNodeAtAsComponent;
import static com.peregrine.it.util.TestHarness.updateResource;
import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_TITLE;
import static com.peregrine.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.util.PerUtil.TEMPLATE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by schaefa on 6/22/17.
 */
public class UpdateResourceServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/test-update-resource";

    private static final Logger logger = LoggerFactory.getLogger(UpdateResourceServletIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() {
        SlingClient client = slingInstanceRule.getAdminClient();
        try {
            deleteFolder(client, ROOT_PATH, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        } catch(IOException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        }
    }

    @Test
    public void testUpdatePage() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-up";
        createFolderStructure(client, folderPath);
        // Create a new source page
        Calendar before = createTimestampAndWait();
        String pageName = "test-page-1";
        response = createPage(client, folderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        logger.info("Response from creating test page: '{}'", response.getContent());

        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, pageName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, folderPath + "/" + pageName, 2, writer.toString(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        insertNodeAtAsComponent(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, "/apps/" + EXAMPLE_JUMBOTRON_TYPE_PATH, "into-after", 302);
        Map<String, Map> children = extractChildNodes(listResourceAsJson(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, 1));
        logger.info("List ");
        assertFalse("No Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.isEmpty());
        assertTrue("Too many Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.size() == 1);
        String jumboTronNodeName = children.keySet().iterator().next() + "";
        assertFalse("Jumbo Tron Name is not provided", jumboTronNodeName == null || jumboTronNodeName.isEmpty());

        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, pageName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeObjectFieldStart(jumboTronNodeName);
        json.writeStringField(JCR_PRIMARY_TYPE, NT_UNSTRUCTURED);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_JUMBOTRON_TYPE_PATH);
        json.writeEndObject();
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, folderPath + "/" + pageName, 3, writer.toString(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        before = createTimestampAndWait();
        // Not we are ready to update that component
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField("title", "Hello");
        json.writeStringField("text", "Peregrine");
        json.writeStringField(JCR_PRIMARY_TYPE, NT_UNSTRUCTURED);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_JUMBOTRON_TYPE_PATH);
        json.writeEndObject();
        json.close();
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + jumboTronNodeName, writer.toString(), 200);

        // Check page now
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, pageName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeObjectFieldStart(jumboTronNodeName);
        json.writeStringField("title", "Hello");
        json.writeStringField("text", "Peregrine");
        json.writeStringField(JCR_PRIMARY_TYPE, NT_UNSTRUCTURED);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_JUMBOTRON_TYPE_PATH);
        json.writeEndObject();
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, folderPath + "/" + pageName, 3, writer.toString(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);
    }


    @Test
    public void testUpdateObject() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-uo";
        createFolderStructure(client, folderPath);
        // Create a new source page
        String objectName = "test-object-1";
        response = createObject(client, folderPath, objectName, EXAMPLE_OBJECT_TYPE_PATH, 200);
        logger.info("Response from creating test object: '{}'", response.getContent());

        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, OBJECT_PRIMARY_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_OBJECT_TYPE_PATH);
        json.writeStringField(JCR_TITLE, objectName);
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, folderPath + "/" + objectName, 2, writer.toString(), true);

        // Not we are ready to update that component
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField("name", "Hello");
        json.writeStringField("value", "Peregrine");
        json.writeStringField(JCR_PRIMARY_TYPE, OBJECT_PRIMARY_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_OBJECT_TYPE_PATH);
        json.writeStringField(JCR_TITLE, objectName);
        json.writeEndObject();
        json.close();
        response = updateResource(client, folderPath + "/" + objectName, writer.toString(), 200);

        // Check page now
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, OBJECT_PRIMARY_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_OBJECT_TYPE_PATH);
        json.writeStringField(JCR_TITLE, objectName);
        json.writeStringField("name", "Hello");
        json.writeStringField("value", "Peregrine");
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, folderPath + "/" + objectName, 1, writer.toString(), true);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
