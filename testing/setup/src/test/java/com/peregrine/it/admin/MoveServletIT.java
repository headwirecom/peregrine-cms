package com.peregrine.it.admin;

import org.apache.http.HttpEntity;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by schaefa on 6/22/17.
 */
public class MoveServletIT {

    public static final String ADMIN_PREFIX_URL = "/api/admin/";
    private enum type {before, after, child};

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @Test
    public void testMoveToAnotherFolder() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
//        SlingHttpResponse response = client.doGet(path + ".mimetype.txt", 200);
//        response.checkContentContains(expected);
        SlingHttpResponse response = null;
        // Create a new test folder node
        response = createFolder(client, "/content", "tests");
        logger.info("Response from creating tests folder: '{}'", response.getContent());
        response = createFolder(client, "/content/tests", "test-move-simple");
        logger.info("Response from creating test-move-simple folder: '{}'", response.getContent());
        // Create a new test source folder node
        response = createFolder(client, "/content/tests/test-move-simple", "source");
        logger.info("Response from creating test move source folder: '{}'", response.getContent());
        // Create a new test target folder node
        response = createFolder(client, "/content/tests/test-move-simple", "target");
        logger.info("Response from creating test move target folder: '{}'", response.getContent());
        // Create a new source page
        response = createPage(client, "/content/tests/test-move-simple/source", "test-page-1", "/content/templates/example");
        logger.info("Response from creating test page folder: '{}'", response.getContent());
        // Move the resource
        response = moveResource(client, "/content/tests/test-move-simple/source/test-page-1", "/content/tests/test-move-simple/target", type.child);
        logger.info("Response from creating move the resoure: '{}'", response.getContent());
        // Check the servlet response
        // Check the node json listing
        response = listResource(client, "/content/tests/test-move-simple/target/test-page-1");
        logger.info("Response from listing the moved page: '{}'", response.getContent());
    }

    private SlingHttpResponse listResource(SlingClient client, String path) throws ClientException {
        return client.doGet(path + ".3.json", 200);
    }

    private SlingHttpResponse createFolder(SlingClient client, String path, String name) throws ClientException {
        String url = ADMIN_PREFIX_URL + "createFolder.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).build();
        return client.doPost(url, formEntry, 200);
    }

    private SlingHttpResponse createPage(SlingClient client, String path, String name, String templatePath) throws ClientException {
        String url = ADMIN_PREFIX_URL + "createPage.json" + path;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).addParameter("templatePath", templatePath).build();
        return client.doPost(url, formEntry, 200);
    }

    private SlingHttpResponse moveResource(SlingClient client, String from, String to, type type) throws ClientException {
        String url = ADMIN_PREFIX_URL + "move.json" + from;
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("to", to).addParameter("type", type.toString()).build();
        return client.doPost(url, formEntry, 200);
    }
}
