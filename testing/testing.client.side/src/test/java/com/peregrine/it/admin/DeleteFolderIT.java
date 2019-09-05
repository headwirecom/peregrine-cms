package com.peregrine.it.admin;

import com.peregrine.commons.test.AbstractTest;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static com.peregrine.it.basic.BasicTestHelpers.checkFolderExists;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createFolders;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Andreas Schaefer on 6/30/17.
 */
public class DeleteFolderIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/delete-folder";

    private static final Logger logger = LoggerFactory.getLogger(DeleteFolderIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    @BeforeClass
    public static void setUpAll() {
        SlingClient client = slingInstanceRule.getAdminClient();
        try {
            deleteFolder(client, ROOT_PATH, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        } catch(IOException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        }
    }

    @Test
    public void testDeleteExistingFolder() throws Exception {
        String rootFolderPath = ROOT_PATH + "/parent-def";
        String folderName = "to-be-removed";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath + "/" + folderName);
        // Check that the folder exists
        checkFolderExists(client, rootFolderPath, folderName);
        // Delete that folder and make sure the folder is gone
        deleteFolder(client, rootFolderPath + "/" + folderName, 200);
        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Folder is still there", rootFolder.containsKey(folderName));
    }

    @Test
    public void testDeleteExistingFolderInFolders() throws Exception {
        String rootFolderPath = ROOT_PATH + "/parent-defif";
        String folderName = "to-be-removed";
        String[] folderNamesBefore = new String[] {"folder-1", "folder-2", folderName, "folder-3"};
        String[] folderNamesAfter = new String[] {"folder-1", "folder-2", "folder-3"};
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);
        createFolders(client, rootFolderPath, folderNamesBefore);
        // Delete that folder and make sure the folder is gone
        deleteFolder(client, rootFolderPath + "/" + folderName, 200);
        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertFalse("Removed Folder is still there", rootFolder.containsKey(folderName));
    }

    @Test
    public void testDeleteNonExistingFolder() throws Exception {
        String rootFolderPath = ROOT_PATH + "/parent-dnef";
        String folderName = "to-be-removed";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath + "/" + folderName);
        // Delete that folder and make sure the folder is gone
        deleteFolder(client, rootFolderPath + "/" + folderName + "-dnef", 400);
        Map rootFolder = listResourceAsJson(client, rootFolderPath, 1);
        assertTrue("Not Remove Folder is gone", rootFolder.containsKey(folderName));
    }

    @Test
    public void testDeleteNonExistingParentFolder() throws Exception {
        String rootFolderPath = ROOT_PATH + "/parent-dnepf";
        String folderName = "to-be-removed";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath);
        // Delete that folder and make sure the folder is gone
        deleteFolder(client, rootFolderPath + "/" + folderName + "-dnef", 400);
        Map rootFolder = listResourceAsJson(client, ROOT_PATH, 1);
        assertFalse("Not created Folder is found", rootFolder.containsKey(rootFolderPath));
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
