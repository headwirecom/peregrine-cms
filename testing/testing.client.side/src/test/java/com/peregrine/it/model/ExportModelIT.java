package com.peregrine.it.model;

import com.peregrine.commons.test.AbstractTest;
import com.peregrine.it.base.RenditionServletIT;
import com.peregrine.it.basic.JsonTest.NoNameObject;
import com.peregrine.it.basic.JsonTest.Prop;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.it.basic.BasicTestHelpers.compareJson;
import static com.peregrine.it.basic.BasicTestHelpers.convertToMap;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.extractChildNodes;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.basic.BasicTestHelpers.loadFile;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_JUMBOTRON_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createObject;
import static com.peregrine.it.util.TestHarness.insertNodeAtAsComponent;
import static com.peregrine.it.util.TestHarness.updateResource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExportModelIT
    extends AbstractTest
{
    private static final Logger logger = LoggerFactory.getLogger(RenditionServletIT.class.getName());

    public static final String ROOT_PATH = "/content/it/objects";

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @Test
    public void testSimpleExport() throws Exception {
        // Create a Page and export it using export.json
    }

    @Test
    public void testSimpleObjectData() throws Exception {
        // Create an Object and export it using data.json
        String rootFolderPath = ROOT_PATH;
        String objectName = "fieldsWithChildrenObject";
        SlingClient client = slingInstanceRule.getAdminClient();
        // Load the object with data.json
        SlingHttpResponse response = client.doGet(ROOT_PATH + SLASH + objectName + ".data.json", 200);
        String responseContent = response.getContent();
        logger.info("Data JSon Response: '{}'", responseContent);
        Map actual = convertToMap(responseContent);
        byte[] resultFileBytes = loadFile("src/test/resources/results/exportModel", "expected.fields.with.children.data.json", "Failed to read Expected Fields With Children file");
        Map expected = convertToMap(new String(resultFileBytes));
        compareJson(expected, actual);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
