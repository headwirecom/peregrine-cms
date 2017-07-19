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

import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.it.basic.BasicTestHelpers.checkFolderExists;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.convertToMap;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.basic.BasicTestHelpers.getDateDifferenceInMillis;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.getNodes;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED_BY;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by schaefa on 6/30/17.
 */
public class NodesIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/get-nodes";

    private static final Logger logger = LoggerFactory.getLogger(NodesIT.class.getName());

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
    public void testListNodes() throws Exception {
        String rootFolderPath = ROOT_PATH + "/folder-ln";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
//        createFolderStructure(client, rootFolderPath);
        // Create a Page, an Object and an Asset and then list it
        String pageFolder = rootFolderPath + "/page";
        createFolderStructure(client, pageFolder);
        // Check that the folder exists
        checkFolderExists(client, rootFolderPath, "page");
        String pageName = "page-1";
        Calendar before = createTimestampAndWait();
        createPage(client, pageFolder, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        StringWriter writer = new StringWriter();
        JsonFactory jf = new JsonFactory();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeStringField(JCR_LAST_MODIFIED_BY, "admin");
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(JCR_LAST_MODIFIED_BY, "admin");
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, pageFolder + "/" + pageName, 2, writer.toString(), true);
        SlingHttpResponse response = getNodes(client, pageFolder + "/" + pageName, 200);
        Map data = convertToMap(response);
        logger.info("Nodes.json response: '{}'", data);
        // Look for the node and then check the page data
        Map<String, Object> page = traverse((Map<String, Object>) data, pageFolder + "/" + pageName);
        assertNotNull("Page was not returned by nodes", page);
        assertEquals("Wrong Page Name", pageName, page.get("name"));
        // Created
        assertEquals("Wrong Resource Type", "per:Page", page.get("resourceType"));
        assertEquals("Wrong Created By", "admin", page.get("createdBy"));
        String dateString = page.get("created") + "";
        assertNotNull("Created date missing", dateString);
        long dateDifference = getDateDifferenceInMillis(dateString, before, true);
        logger.info("Created Date Difference: '{}|'", dateDifference);
        assertTrue("Wrong Created Date", dateDifference < 0);
        // Last Modified
        assertEquals("Wrong Last Modified By", "admin", page.get("createdBy"));
        dateString = page.get("lastModified") + "";
        assertNotNull("Last Modified date missing", dateString);
        dateDifference = getDateDifferenceInMillis(dateString, before, true);
        logger.info("Last Modified Date Difference: '{}|'", dateDifference);
        assertTrue("Wrong Last Modified Date", dateDifference < 0);
    }

    private Map<String, Object> traverse(Map<String, Object> map, String path) {
        for(Entry<String, Object> entry: map.entrySet()) {
            String key = entry.getKey() + "";
            if("path".equals(key)) {
                String value = entry.getValue() + "";
                if(path.equals(value)) {
                    return map;
                }
            } else if("children".equals(key)) {
                List children = (List) entry.getValue();
                for(Object child: children) {
                    if(child instanceof Map) {
                        Map<String, Object> answer = traverse((Map<String, Object>) child, path);
                        if(answer != null) {
                            return answer;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
