package com.peregrine.it.admin;

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
import java.text.ParseException;
import java.util.Calendar;

import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkPages;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.basic.BasicTestHelpers.listResource;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.moveNodeToResource;

/**
 * Created by Andreas Schaefer on 6/22/17.
 */
public class MoveNodeToServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/test-move-node-to";


    private static final Logger logger = LoggerFactory.getLogger(MoveNodeToServletIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        try {
            deleteFolder(client, ROOT_PATH, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        } catch(IOException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        }
    }


    @Test
    public void testMoveToAnotherEmptyFolder() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source";
        String targetPath = ROOT_PATH + "/target";
        createFolderStructure(client, sourcePath);
        createFolderStructure(client, targetPath);
        // Create a new source page
        Calendar before = createTimestampAndWait();
        response = createPage(client, sourcePath, "test-page-1", EXAMPLE_TEMPLATE_PATH, 200);
        checkLastModified(client, sourcePath + "/" + "test-page-1", before);

        logger.info("Response from creating test page folder: '{}'", response.getContent());
        // Move the resource
        before = createTimestampAndWait();
        response = moveNodeToResource(client, sourcePath+ "/test-page-1", targetPath, "child", 302);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
        // Check the node json listing
        response = listResource(client, targetPath + "/test-page-1", 1);
        logger.info("Response from listing the moved page: '{}'", response.getContent());
        checkPages(response, "test-page-1");
        checkLastModified(client, targetPath + "/test-page-1", before);
    }

    @Test
    public void testMoveToAnotherFolderAsChild() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2afac";
        String targetPath = ROOT_PATH + "/target/target-m2afac";
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource
        Calendar before = createTimestampAndWait();
        response = moveNodeToResource(client, sourcePath + "/source-page-q", targetPath, "child", 302);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
        // Check the node json listing
        response = listResource(client, targetPath, 2);
        logger.info("Response from listing the moved page: '{}'", response.getContent());
        checkPages(response, "target-page-a", "target-page-z", "target-page-b", "target-page-y", "source-page-q");
        checkLastModified(client, targetPath + "/source-page-q", before);
    }

    @Test
    public void testMoveToAnotherFolderAsChildBefore() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2afacb";
        String targetPath = ROOT_PATH + "/target/target-m2afacb";
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource
        Calendar before = createTimestampAndWait();
        response = moveNodeToResource(client, sourcePath + "/source-page-q", targetPath, "into-before", 302);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
        // Check the node json listing
        response = listResource(client, targetPath, 2);
        logger.info("Response from listing the moved page: '{}'", response.getContent());
        checkPages(response, "source-page-q", "target-page-a", "target-page-z", "target-page-b", "target-page-y");
        checkLastModified(client, targetPath + "/source-page-q", before);
    }

    @Test
    public void testMoveToSameFolderAsChildBefore() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2sfacb";
        String targetPath = ROOT_PATH + "/target/target-m2sfacb";
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource
        Calendar before = createTimestampAndWait();
        response = moveNodeToResource(client, targetPath + "/target-page-z", targetPath, "into-before", 302);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
        // Check the node json listing
        response = listResource(client, targetPath, 2);
        logger.info("Response from listing the moved page: '{}'", response.getContent());
        checkPages(response,  "target-page-z", "target-page-a", "target-page-b", "target-page-y");
        checkLastModified(client, targetPath + "/target-page-z", before);
    }

    @Test
    public void testMoveToAnotherFolderAsChildAfter() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2afaca";
        String targetPath = ROOT_PATH + "/target/target-m2afaca";
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource
        Calendar before = createTimestampAndWait();
        response = moveNodeToResource(client, sourcePath + "/source-page-q", targetPath, "into-after", 302);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
        // Check the node json listing
        response = listResource(client, targetPath, 2);
        logger.info("Response from listing the moved page: '{}'", response.getContent());
        checkPages(response, "target-page-a", "target-page-z", "target-page-b", "target-page-y", "source-page-q");
        checkLastModified(client, targetPath + "/source-page-q", before);
    }

    @Test
    public void testMoveToAnotherFolderAsBefore() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2afab";
        String targetPath = ROOT_PATH + "/target/target-m2afab";
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource before 'target-page-z'
        Calendar before = createTimestampAndWait();
        response = moveNodeToResource(client, sourcePath + "/source-page-q", targetPath + "/target-page-z", "before", 302);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
        // Check the node json listing
        response = listResource(client, targetPath, 2);
        logger.info("Response from listing the moved page: '{}'", response.getContent());
        checkPages(response, "target-page-a", "source-page-q", "target-page-z", "target-page-b", "target-page-y");
        checkLastModified(client, targetPath + "/source-page-q", before);
    }

    @Test
    public void testMoveToAnotherFolderAsAfter() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2afaa";
        String targetPath = ROOT_PATH + "/target/target-m2afaa";
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource before 'target-page-z'
        Calendar before = createTimestampAndWait();
        response = moveNodeToResource(client, sourcePath + "/source-page-q", targetPath + "/target-page-z", "after", 302);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
        // Check the node json listing
        response = listResource(client, targetPath, 2);
        logger.info("Response from listing the moved page: '{}'", response.getContent());
        checkPages(response, "target-page-a", "target-page-z", "source-page-q", "target-page-b", "target-page-y");
        checkLastModified(client, targetPath + "/source-page-q", before);
    }

    @Test
    public void testMoveToNonExistingFolder() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2nef";
        String targetPath = ROOT_PATH + "/target/target-m2nef";
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource before 'target-page-z'
        response = moveNodeToResource(client, sourcePath + "/source-page-q", targetPath + "-not-there", "child", 400);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
    }

    @Test
    public void testMoveOfNonExistingSourceFolder() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2nesf";
        String targetPath = ROOT_PATH + "/target/target-m2nesf";
        Calendar before = createTimestampAndWait();
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource before 'target-page-z'
        response = moveNodeToResource(client, sourcePath + "-not-there" + "/source-page-q", targetPath, "child", 400);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
    }

    @Test
    public void testMoveOfNonExistingSourcePage() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2nesp";
        String targetPath = ROOT_PATH + "/target/target-m2nesp";
        Calendar before = createTimestampAndWait();
        createPageSetup(client, sourcePath, targetPath);

        // Move the resource before 'target-page-z'
        response = moveNodeToResource(client, sourcePath + "/source-page-q-not-there", targetPath, "child", 400);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
    }

    @Test
    public void testMoveToParentWithPageAlreadyThere() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String sourcePath = ROOT_PATH + "/source/source-m2pwpat";
        String targetPath = ROOT_PATH + "/target/target-m2pwpat";
        createPageSetup(client, sourcePath, targetPath);
        Calendar before = createTimestampAndWait();
        createPage(client, targetPath, "source-page-q", EXAMPLE_TEMPLATE_PATH, 200);

        // Move the resource before 'target-page-z'
        response = moveNodeToResource(client, sourcePath + "/source-page-q", targetPath, "child", 400);
        logger.info("Response from creating move the resource: '{}'", response.getContent());
    }

    private void createPageSetup(SlingClient client, String sourcePath, String targetPath) throws ClientException, IOException, ParseException {
        createFolderStructure(client, sourcePath);
        createFolderStructure(client, targetPath);
        // Create a new source page and target pages
        Calendar before = createTimestampAndWait();
        createPage(client, sourcePath, "source-page-q", EXAMPLE_TEMPLATE_PATH, 200);
        checkLastModified(client, sourcePath + "/" + "source-page-q", before);
        createPage(client, targetPath, "target-page-a", EXAMPLE_TEMPLATE_PATH, 200);
        checkLastModified(client, targetPath + "/" + "target-page-a", before);
        createPage(client, targetPath, "target-page-z", EXAMPLE_TEMPLATE_PATH, 200);
        checkLastModified(client, targetPath + "/" + "target-page-z", before);
        createPage(client, targetPath, "target-page-b", EXAMPLE_TEMPLATE_PATH, 200);
        checkLastModified(client, targetPath + "/" + "target-page-b", before);
        createPage(client, targetPath, "target-page-y", EXAMPLE_TEMPLATE_PATH, 200);
        checkLastModified(client, targetPath + "/" + "target-page-y", before);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
