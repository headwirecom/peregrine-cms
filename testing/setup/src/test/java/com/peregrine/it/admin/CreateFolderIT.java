package com.peregrine.it.admin;

import com.peregrine.it.util.AbstractTest;
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

import static com.peregrine.it.util.TestHarness.checkFolderExists;
import static com.peregrine.it.util.TestHarness.checkFolders;
import static com.peregrine.it.util.TestHarness.createFolder;
import static com.peregrine.it.util.TestHarness.createFolderStructure;
import static com.peregrine.it.util.TestHarness.deleteFolder;

/**
 * Created by schaefa on 6/30/17.
 */
public class CreateFolderIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/create-folder";

    private static final Logger logger = LoggerFactory.getLogger(CreateFolderIT.class.getName());

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
    public void testCreateFolder() throws Exception {
        String rootFolderPath = ROOT_PATH + "/folder-cf";
        String folderName = "created";
        SlingClient client = slingInstanceRule.getAdminClient();
        // This test depends on the Create Folder to work
        createFolderStructure(client, rootFolderPath + "/" + folderName);
        // Check that the folder exists
        checkFolderExists(client, rootFolderPath, folderName);
    }

    @Test
    public void testCreateFoldersInsideFolder() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();

        String rootFolderPath = ROOT_PATH + "/folder-cfif";
        createFolderStructure(client, rootFolderPath);

        String[] folderNames = new String[] {"folder-1", "folder-2", "folder-3", "folder-4"};
        // This test depends on the Create Folder to work
        for(String folderName: folderNames) {
            createFolder(client, rootFolderPath, folderName, 200);
            // Check that the folder exists
            checkFolderExists(client, rootFolderPath, folderName);
        }
        // Check order of the created folders
        checkFolders(client, rootFolderPath, folderNames);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
