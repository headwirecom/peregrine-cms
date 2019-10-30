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
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.checkTwoResources;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.basic.TestConstants.THEMECLEAN_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.THEMECLWAN_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createSite;

/**
 * Test the Create Site Servlet
 *
 * Created by Andreas Schaefer on 9/17/19.
 */
public class CreateSiteIT
    extends AbstractTest
{
//    public static final String ROOT_PATH = "/content/sites/it-test/Test-Sites";
    public static final String FROM_SITE_1_NAME = "itTest";
    public static final String TO_SITE_1_NAME = "itTestCopy";

    private static final Logger logger = LoggerFactory.getLogger(CreateSiteIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() {
        SlingClient client = slingInstanceRule.getAdminClient();
        try {
            client.doDelete(SITES_ROOT + SLASH + TO_SITE_1_NAME, null, null, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", SITES_ROOT + SLASH + TO_SITE_1_NAME, e);
        }
    }

    @Test
    public void testCreateSite() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();

        // Create the folder structure
        createSite(client, FROM_SITE_1_NAME, TO_SITE_1_NAME, 200);
        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, THEMECLEAN_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, TO_SITE_1_NAME);
        json.writeStringField(TEMPLATE, THEMECLWAN_TEMPLATE_PATH);
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, SITES_ROOT + "/" + TO_SITE_1_NAME, 2, writer.toString(), true);
        checkTwoResources(client, SITES_ROOT + "/" + FROM_SITE_1_NAME, SITES_ROOT + "/" + TO_SITE_1_NAME, 5,
            "jcr:content/jcr:title"
        );
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
