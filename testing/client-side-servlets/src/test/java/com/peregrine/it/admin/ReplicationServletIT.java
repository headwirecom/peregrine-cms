package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.it.util.AbstractTest;
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
import java.util.Map;

import static com.peregrine.it.util.TestHarness.checkPages;
import static com.peregrine.it.util.TestHarness.checkResourceByJson;
import static com.peregrine.it.util.TestHarness.checkResponse;
import static com.peregrine.it.util.TestHarness.createFolderStructure;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createTemplate;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.executeReplication;
import static com.peregrine.it.util.TestHarness.listResource;
import static com.peregrine.it.util.TestHarness.moveResource;
import static com.peregrine.it.util.TestHarness.renameResource;
import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_TITLE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.util.PerUtil.TEMPLATE;

/**
 * Created by schaefa on 6/22/17.
 */
public class ReplicationServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/replication-source";
    public static final String LIVE_ROOT_PATH = "/live/tests/replication-source";
    public static final String REPLICATION_PATH = "/live";

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
        createTemplate(client, rootFolderPath, templateName, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, "example/components/page");
        json.writeStringField(JCR_TITLE, templateName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, rootFolderPath + "/" + templateName, 2, writer.toString(), true);

        String pageName = "replication-page";
        createPage(client, rootFolderPath, pageName, rootFolderPath + "/" + templateName, 200);
        jf = new JsonFactory();
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, "example/components/page");
        json.writeStringField(JCR_TITLE, pageName);
        json.writeStringField(TEMPLATE, rootFolderPath + "/" + templateName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, rootFolderPath + "/" + pageName, 2, writer.toString(), true);

        // Create Target Replication Folder
        createFolderStructure(client, REPLICATION_PATH);

        // Replicate the Page and check its new content
        executeReplication(client, rootFolderPath + "/" + pageName, "local", 200);

        // Check page and template
        jf = new JsonFactory();
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, "example/components/page");
        json.writeStringField(JCR_TITLE, pageName);
        json.writeStringField(TEMPLATE, liveRootFolderPath + "/" + templateName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, liveRootFolderPath + "/" + pageName, 2, writer.toString(), true);
        jf = new JsonFactory();
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, "example/components/page");
        json.writeStringField(JCR_TITLE, templateName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, liveRootFolderPath + "/" + templateName, 2, writer.toString(), true);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
