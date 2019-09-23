package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.commons.test.AbstractTest;
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
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED_BY;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.it.basic.BasicTestHelpers.checkFolderExists;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.convertResponseToMap;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.basic.BasicTestHelpers.getDateDifferenceInMillis;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.createTemplate;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.executeReplication;
import static com.peregrine.it.util.TestHarness.getNodes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Andreas Schaefer on 6/30/17.
 */
public class NodesIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/get-nodes";
    public static final String LIVE_ROOT_PATH = "/live/tests/get-nodes";
    public static final String REPLICATION_PATH = "/live";

    private static final Logger logger = LoggerFactory.getLogger(NodesIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() throws IOException, ClientException {
        SlingClient client = slingInstanceRule.getAdminClient();
        try {
            deleteFolder(client, ROOT_PATH, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        } catch(IOException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        }
        try {
            deleteFolder(client, LIVE_ROOT_PATH, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete live root path: '{}' -> ignore", ROOT_PATH, e);
        } catch(IOException e) {
            logger.warn("Could not delete life root path: '{}' -> ignore", ROOT_PATH, e);
        }
        // Upload the Test Assets to have something to rendition
        createFolderStructure(client, ROOT_PATH);
        createFolderStructure(client, REPLICATION_PATH);
    }


    @Test
    public void testListNodes() throws Exception {
        String rootFolderPath = ROOT_PATH + "/folder-ln";
        SlingClient client = slingInstanceRule.getAdminClient();
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
        Map data = convertResponseToMap(response);
        logger.info("Nodes.json response: '{}'", data);
        // Look for the node and then check the page data
        Map<String, Object> page = traverse((Map<String, Object>) data, pageFolder + "/" + pageName);
        assertNotNull("Page was not returned by nodes", page);
        assertEquals("Wrong Page Name", pageName, page.get("name"));
        // Created
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


    @Test
    public void testNodesForReplication() throws Exception {
        // START: The following code was taken from Replication Servlet IT testSimplePageReplication() with minor path adjustments
        SlingClient client = slingInstanceRule.getAdminClient();
        String rootFolderPath = ROOT_PATH + "/test-nfr";
        String liveRootFolderPath = LIVE_ROOT_PATH + "/test-nfr";
        createFolderStructure(client, rootFolderPath);
        // First Create a Template, then a Page using it
        String templateName = "replication-template";
        createTemplate(client, rootFolderPath, templateName, "example/components/page", 200);
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

        String pageName = "replication-page";
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

        // Create Target Replication Folder
        createFolderStructure(client, REPLICATION_PATH);

        Calendar before = createTimestampAndWait();
        // Replicate the Page and check its new content
        SlingHttpResponse checkResponse = executeReplication(client, rootFolderPath + "/" + pageName, "local", 200);
        logger.info("Execute Replication Response: '{}'", convertResponseToMap(checkResponse));

        // Check page and template
        jf = new JsonFactory();
        writer = new StringWriter();
        json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
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
        json.writeStringField(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        json.writeStringField(JCR_TITLE, templateName);
        json.writeEndObject();
        json.writeEndObject();
        json.close();
        checkResourceByJson(client, liveRootFolderPath + "/" + templateName, 2, writer.toString(), true);
        //END Now we start testing Nodes

        SlingHttpResponse response = getNodes(client, rootFolderPath + "/" + pageName, 200);
        Map data = convertResponseToMap(response);
        logger.info("Nodes.json response: '{}'", data);
        // Look for the node and then check the page data
        Map<String, Object> page = traverse((Map<String, Object>) data, rootFolderPath + "/" + pageName);
        assertNotNull("Page was not returned by nodes", page);
        assertEquals("Wrong Page Name", pageName, page.get("name"));
        // Created
        assertEquals("Wrong Created By", "admin", page.get("createdBy"));
        String dateString = getValue(page, "created");
        assertNotNull("Created date missing", dateString);
        long dateDifference = getDateDifferenceInMillis(dateString, before, true);
        logger.info("Created Date Difference: '{}|'", dateDifference);
        assertTrue("Wrong Created Date: " + dateDifference, dateDifference >= 0);
        // Last Modified
        assertEquals("Wrong Last Modified By", "admin", page.get("createdBy"));
        dateString = getValue(page, "lastModified");
        assertNotNull("Last Modified date missing", dateString);
        dateDifference = getDateDifferenceInMillis(dateString, before, true);
        logger.info("Last Modified Date Difference: '{}|'", dateDifference);
        assertTrue("Wrong Last Modified Date: " + dateDifference, dateDifference >= 0);
        // Now we check the Replicated Date, User and Location as well as the replication status
        String replicated = getValue(page, "Replicated");
        assertNotNull("Replicated date missing", replicated);
        dateDifference = getDateDifferenceInMillis(replicated, before, true);
        logger.info("Replicated Date Difference: '{}|'", dateDifference);
        assertTrue("Wrong Replicated Date: "+ dateDifference, dateDifference < 0);
        String replicatedBy = getValue(page, "ReplicatedBy");
        assertEquals("Wrong Replicated By", "admin", replicatedBy);
        String replicationRef = getValue(page, "ReplicationRef");
        assertEquals("Wrong Replication Location By", liveRootFolderPath + "/" + pageName, replicationRef);
        String replicationStatus = getValue(page, "ReplicationStatus");
        assertEquals("Wrong Replication Status", "activated", replicationStatus);
    }

    private String getValue(Map<String, Object> map, String key) {
        String answer = null;
        if(map.containsKey(key)) {
            Object value = map.get(key);
            if(value != null) {
                answer = value.toString();
            }
        }
        return answer;
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
