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

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.it.basic.BasicTestHelpers.*;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;

public class ListTenantsIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/sites";

    private static final Logger logger = LoggerFactory.getLogger(CreatePageIT.class.getName());
    public static final String TEST_SITE_NAME = "testsite";
    public static final String TENANTS = "tenants";
    public static final String ROOTS = "roots";
    public static final String APPS = "apps";
    public static final String ASSETS = "assets";
    public static final String FELIBS = "felibs";
    public static final String OBJECTS = "objects";
    public static final String SITES = "sites";
    public static final String TEMPLATES = "templates";
    public static final String LIST_TENANTS_SERVLET_PATH = "/perapi/admin/listTenants.json";

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
    public void testListTenants() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        // Create the folder structure
        createFolderStructure(client, ROOT_PATH);
        createPage(client, ROOT_PATH, TEST_SITE_NAME, EXAMPLE_TEMPLATE_PATH, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeArrayFieldStart(TENANTS);
        json.writeStartObject();
        json.writeStringField(NAME, TEST_SITE_NAME);
        json.writeStringField(TITLE, TEST_SITE_NAME);
        json.writeObjectFieldStart(ROOTS);
        json.writeStringField(APPS, APPS_ROOT + SLASH + TEST_SITE_NAME);
        json.writeStringField(ASSETS, ASSETS_ROOT + SLASH + TEST_SITE_NAME);
        json.writeStringField(FELIBS, FELIBS_ROOT + SLASH + TEST_SITE_NAME);
        json.writeStringField(OBJECTS, OBJECTS_ROOT + SLASH + TEST_SITE_NAME);
        json.writeStringField(SITES, SITES_ROOT + SLASH + TEST_SITE_NAME);
        json.writeStringField(TEMPLATES, TEMPLATES_ROOT + SLASH + TEST_SITE_NAME);
        json.writeEndObject();
        json.writeEndObject();
        json.writeEndArray();
        json.close();

        checkServletByJson(client, LIST_TENANTS_SERVLET_PATH, writer.toString());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
