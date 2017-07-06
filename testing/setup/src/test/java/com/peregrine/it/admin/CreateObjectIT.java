package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.it.util.AbstractTest;
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

import static com.peregrine.it.util.TestConstants.TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.checkResourceByJson;
import static com.peregrine.it.util.TestHarness.createFolderStructure;
import static com.peregrine.it.util.TestHarness.createObject;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.deletePage;
import static com.peregrine.it.util.TestHarness.listResourceAsJson;
import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_TITLE;
import static com.peregrine.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.util.PerUtil.TEMPLATE;
import static org.junit.Assert.assertFalse;

/**
 * Created by schaefa on 6/30/17.
 */
public class CreateObjectIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/create-object";

    private static final Logger logger = LoggerFactory.getLogger(CreateObjectIT.class.getName());

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
    public void testObjectCreation() throws Exception {
        String rootFolderPath = ROOT_PATH + "/test-oc";
        String objectName = "new-object";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);

        // Create the Object and check that it is created correctly
        createObject(client, rootFolderPath, objectName, TEMPLATE_PATH, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, OBJECT_PRIMARY_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, TEMPLATE_PATH);
        json.writeStringField(JCR_TITLE, objectName);
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + objectName, 1, writer.toString(), true);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
