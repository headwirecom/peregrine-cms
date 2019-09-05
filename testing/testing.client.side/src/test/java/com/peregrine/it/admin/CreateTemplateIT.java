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

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createTemplate;

/**
 * Created by Andreas Schaefer on 6/30/17.
 */
public class CreateTemplateIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/create-template";

    private static final Logger logger = LoggerFactory.getLogger(CreateTemplateIT.class.getName());

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
    public void testTemplateCreation() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-tc";
        String templateName = "new-template";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Object and check that it is created correctly
        createTemplate(client, rootFolderPath, templateName, "example/components/page",200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, templateName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + templateName, 2, writer.toString(), true);
        checkLastModified(client, rootFolderPath + "/" + templateName);
    }

    @Test
    public void testTemplateCreationAndUsage() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-tcau";
        String templateName = "new-template";
        String pageName = "new-page";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Object and check that it is created correctly
        createTemplate(client, rootFolderPath, templateName,"example/components/page", 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, templateName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + templateName, 2, writer.toString(), true);
        checkLastModified(client, rootFolderPath + "/" + templateName);

        createPage(client, rootFolderPath, pageName, rootFolderPath + "/" + templateName, 200);
        jf = new JsonFactory();
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, pageName);
        json.writeStringField(TEMPLATE, rootFolderPath + "/" + templateName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + pageName, 2, writer.toString(), true);
        checkLastModified(client, rootFolderPath + "/" + pageName);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
