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
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createObject;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.deletePage;
import static org.junit.Assert.assertFalse;

/**
 * Created by Andreas Schaefer on 6/30/17.
 */
public class DeletePageIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/delete-page";

    private static final Logger logger = LoggerFactory.getLogger(DeletePageIT.class.getName());

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
    public void testSinglePageDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/parent-spd";
        String pageName = "to-be-removed";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createPage(client, rootFolderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
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

        checkResourceByJson(client, rootFolderPath + "/" + pageName, 2, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deletePage(client, rootFolderPath + "/" + pageName, 200);
        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Page is still there", rootFolder.containsKey(pageName));
    }

    @Test
    public void testNestedChildPageDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/parent-ncpd";
        String parentPageName = "to-be-removed-parent";
        String childPageName = "to-be-removed-child";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createPage(client, rootFolderPath, parentPageName, EXAMPLE_TEMPLATE_PATH, 200);
        createPage(client, rootFolderPath + "/" + parentPageName, childPageName, EXAMPLE_TEMPLATE_PATH, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, parentPageName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeEndObject();
        json.writeObjectFieldStart(childPageName);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, childPageName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + parentPageName, 3, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deletePage(client, rootFolderPath + "/" + parentPageName + "/" + childPageName, 200);

        jf = new JsonFactory();
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, parentPageName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + parentPageName, 2, writer.toString(), true);
    }

    @Test
    public void testNestedParentPageDeletion() throws Exception {
        String rootFolderPath = ROOT_PATH + "/parent-nppd";
        String parentPageName = "to-be-removed-parent";
        String childPageName = "to-be-removed-child";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createPage(client, rootFolderPath, parentPageName, EXAMPLE_TEMPLATE_PATH, 200);
        createPage(client, rootFolderPath + "/" + parentPageName, childPageName, EXAMPLE_TEMPLATE_PATH, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, parentPageName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeEndObject();
        json.writeObjectFieldStart(childPageName);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, childPageName);
        json.writeStringField(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + parentPageName, 3, writer.toString(), true);

        // Delete that folder and make sure the page is gone
        deletePage(client, rootFolderPath + "/" + parentPageName, 200);

        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Page is still there", rootFolder.containsKey(parentPageName));
    }

    @Test
    public void testNonPageDeletionFailure() throws Exception {
        String rootFolderPath = ROOT_PATH + "/parent-npdf";
        String objectName = "to-be-removed";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Page and check that it is created correctly
        createObject(client, rootFolderPath, objectName, EXAMPLE_TEMPLATE_PATH, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, OBJECT_PRIMARY_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_TEMPLATE_PATH);
        json.writeStringField(JCR_TITLE, objectName);
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + objectName, 1, writer.toString(), true);

        // Delete that folder and make sure that it fails because it is not a page
        deletePage(client, rootFolderPath + "/" + objectName, 400);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
