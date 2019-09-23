package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.commons.test.AbstractTest;
import com.peregrine.it.basic.JsonTest.NoNameObject;
import com.peregrine.it.basic.JsonTest.Prop;
import com.peregrine.it.basic.JsonTest.TestPage;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Map;

import static com.peregrine.commons.test.TestUtil.compareJson;
import static com.peregrine.commons.test.TestUtil.convertJsonTextToMap;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.convertResponseToMap;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createTemplate;
import static com.peregrine.it.util.TestHarness.deleteLeafFolder;
import static com.peregrine.it.util.TestHarness.getReferenceList;
import static com.peregrine.it.util.TestHarness.updateResource;
import static org.junit.Assert.fail;

/**
 * Created by Andreas Schaefer on 7/6/17.
 */
public class ReferenceListerServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/reference-lister";
    public static final String PAGES_NAME = "pages";
    public static final String TEMPLATES_NAME = "templates";

    private static final Logger logger = LoggerFactory.getLogger(ReplicationServletIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() {
        SlingClient client = slingInstanceRule.getAdminClient();
        if(!deleteLeafFolder(client, ROOT_PATH)) {
            fail("Could not delete Reference Lister Root Folder: " + ROOT_PATH);
        }
    }

    @Test
    public void testSimpleShallowPageReference() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String folderName = "test-sspr";
        // Create a Template and a Page
        String templatesPath = ROOT_PATH + "/" + folderName + "/" + TEMPLATES_NAME;
        String pagesPath = ROOT_PATH + "/" + folderName + "/" + PAGES_NAME;
        createFolderStructure(client, templatesPath);
        createFolderStructure(client, pagesPath);
        String templateName = "reference-template";
        createTemplate(client, templatesPath, templateName, EXAMPLE_PAGE_TYPE_PATH, 200);
        TestPage testTemplate = new TestPage(templateName, EXAMPLE_PAGE_TYPE_PATH, null);
        checkResourceByJson(client, templatesPath + "/" + templateName, 2, testTemplate.toJSon(), true);
        String pageName = "reference-page";
        createPage(client, pagesPath, pageName, templatesPath + "/" + templateName, 200);
        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, templatesPath + "/" + templateName);
        checkResourceByJson(client, pagesPath + "/" + pageName, 2, testPage.toJSon(), true);

        // Get References and check if it returns the template
        StringWriter writer = new StringWriter();
        JsonFactory jf = new JsonFactory();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField("sourceName", pageName);
        json.writeStringField("sourcePath", pagesPath + "/" + pageName);
        json.writeArrayFieldStart("references");
        json.writeStartObject();
        json.writeStringField("name", templateName);
        json.writeStringField("path", templatesPath + "/" + templateName);
        json.writeEndObject();
        json.writeEndArray();
        json.writeEndObject();
        json.close();
        SlingHttpResponse response = getReferenceList(client, pagesPath + "/" + pageName, 200);
        Map expected = convertJsonTextToMap(writer.toString());
        Map actual = convertResponseToMap(response);
        compareJson(expected, actual);
    }

    @Test
    public void testCyclicReference() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String folderName = "test-cr";
        // Create a Template and a Page
        String templatesPath = ROOT_PATH + "/" + folderName + "/" + TEMPLATES_NAME;
        String pagesPath = ROOT_PATH + "/" + folderName + "/" + PAGES_NAME;
        createFolderStructure(client, templatesPath);
        createFolderStructure(client, pagesPath);
        String templateName = "reference-template";
        createTemplate(client, templatesPath, templateName, EXAMPLE_PAGE_TYPE_PATH, 200);
        TestPage testTemplate = new TestPage(templateName, EXAMPLE_PAGE_TYPE_PATH, null);
        checkResourceByJson(client, templatesPath + "/" + templateName, 2, testTemplate.toJSon(), true);
        String pageName = "reference-page";
        createPage(client, pagesPath, pageName, templatesPath + "/" + templateName, 200);
        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, templatesPath + "/" + templateName);
        checkResourceByJson(client, pagesPath + "/" + pageName, 2, testPage.toJSon(), true);

        NoNameObject simpleObject = (NoNameObject) new NoNameObject(PAGE_CONTENT_TYPE).addSlingResourceType(EXAMPLE_PAGE_TYPE_PATH)
            .addProperties(new Prop("myRef", pagesPath + "/" + pageName));
        updateResource(client, templatesPath + "/" + templateName + "/" + JCR_CONTENT, simpleObject.toJSon(), 200);
        testTemplate.getContent().addProperty(new Prop("myRef", pagesPath + "/" + pageName));
        checkResourceByJson(client, templatesPath + "/" + templateName, 2, testTemplate.toJSon(), true);


        // Get References and check if it returns the template
        StringWriter writer = new StringWriter();
        JsonFactory jf = new JsonFactory();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField("sourceName", pageName);
        json.writeStringField("sourcePath", pagesPath + "/" + pageName);
        json.writeArrayFieldStart("references");
        json.writeStartObject();
        json.writeStringField("name", templateName);
        json.writeStringField("path", templatesPath + "/" + templateName);
        json.writeEndObject();
        json.writeStartObject();
        json.writeStringField("name", pageName);
        json.writeStringField("path", pagesPath + "/" + pageName);
        json.writeEndObject();
        json.writeEndArray();
        json.writeEndObject();
        json.close();
        SlingHttpResponse response = getReferenceList(client, pagesPath + "/" + pageName, 200);
        Map expected = convertJsonTextToMap(writer.toString());
        Map actual = convertResponseToMap(response);
        logger.info("Reference List, Actual: '{}', Expoected: '{}'", actual, expected);
        compareJson(expected, actual);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
