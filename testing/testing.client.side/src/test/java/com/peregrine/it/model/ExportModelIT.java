package com.peregrine.it.model;

import com.peregrine.commons.test.AbstractTest;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.test.TestUtil.compareJson;
import static com.peregrine.commons.test.TestUtil.convertJsonTextToList;
import static com.peregrine.commons.test.TestUtil.convertJsonTextToMap;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.it.basic.BasicTestHelpers.loadFile;

public class ExportModelIT
    extends AbstractTest
{
    private static final Logger logger = LoggerFactory.getLogger(ExportModelIT.class.getName());

    public static final String ROOT_PATH = "/content/it/objects";

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @Test
    public void testSimpleExport() throws Exception {
        // Create an Object and export it using data.json
        String rootFolderPath = ROOT_PATH;
        String objectName = "exportOne";
        SlingClient client = slingInstanceRule.getAdminClient();
        // Load the object with data.json
        SlingHttpResponse response = client.doGet(ROOT_PATH + SLASH + objectName + ".export.json", 200);
        String responseContent = response.getContent();
        logger.info("Data JSon Response: '{}'", responseContent);
        List actual = convertJsonTextToList(responseContent);
        Map actualMap = new HashMap();
        actualMap.put("list", actual);
        byte[] resultFileBytes = loadFile("src/test/resources/results/exportModel", "export.one.export.json", "Failed to read Expected Export One Export");
        List expected = convertJsonTextToList(new String(resultFileBytes));
        Map expectedMap = new HashMap();
        expectedMap.put("list", expected);
        compareJson(expectedMap, actualMap, true);
    }

    @Test
    public void testSimpleModel() throws Exception {
        // Create an Object and export it using data.json
        String rootFolderPath = ROOT_PATH + SLASH + "exportOne";
        String objectName = "exportOneSub";
        SlingClient client = slingInstanceRule.getAdminClient();
        // Load the object with data.json
        SlingHttpResponse response = client.doGet(rootFolderPath + SLASH + objectName + ".model.json", 200);
        String responseContent = response.getContent();
        logger.info("Data JSon Response: '{}'", responseContent);
        Map actual = convertJsonTextToMap(responseContent);
        byte[] resultFileBytes = loadFile("src/test/resources/results/exportModel", "export.one.sub.model.json", "Failed to read Expected Export One Export");
        Map expected = convertJsonTextToMap(new String(resultFileBytes));
        compareJson(expected, actual, true);
    }

    /**
     * This test is here to ensure that we can list the content of an Object
     * using the .data.json extension.
     *
     * The extension definition is listed underneath /apps/it/objects/fieldsAndChildren
     * as data.json.html file. It references to the FieldsWithChildrenObject class that provides
     * the content. The model com.peregrine.it.client.model.FieldsWithChildrenModel is
     * defining the output of the resource of Sling Resource Type: 'it/objects/fieldsAndChildren'
     *
     * @throws Exception
     */
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
        Map actual = convertJsonTextToMap(responseContent);
        byte[] resultFileBytes = loadFile("src/test/resources/results/exportModel", "expected.fields.with.children.data.json", "Failed to read Expected Fields With Children file");
        Map expected = convertJsonTextToMap(new String(resultFileBytes));
        compareJson(expected, actual);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
