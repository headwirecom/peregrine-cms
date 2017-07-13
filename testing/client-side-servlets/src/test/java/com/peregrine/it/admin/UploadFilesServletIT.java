package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.it.basic.AbstractTest;
import org.apache.commons.io.IOUtils;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.junit.rules.SlingInstanceRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;

import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.uploadFile;
import static com.peregrine.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerUtil.METADATA;
import static org.junit.Assert.assertTrue;

/**
 * Created by schaefa on 7/6/17.
 */
public class UploadFilesServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/upload-assets";
    public static final String IMAGE_RESOURCES_PATH = "src/test/resources/images";
    public static final String SINGLE_IMAGE_RESOURCE_PATH = "src/test/resources/images/test.png";

    private static final Logger logger = LoggerFactory.getLogger(ReplicationServletIT.class.getName());

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
    public void testSimpleAssetUpload() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        String rootFolderPath = ROOT_PATH + "/test-sau";
        String imageName = "test.png";
        createFolderStructure(client, rootFolderPath);

        File localFolder = new File(".");
        logger.info("Local Folder: '{}'", localFolder.getAbsolutePath());
        assertTrue("Local Folder does not exist", localFolder.exists());
        assertTrue("Local Folder is not a folder", localFolder.isDirectory());
        File imagesFolder = new File(localFolder, IMAGE_RESOURCES_PATH);
        logger.info("Images Folder: '{}'", imagesFolder.getAbsolutePath());
        assertTrue("Local Folder does not exist", imagesFolder.exists());
        assertTrue("Local Folder is not a folder", imagesFolder .isDirectory());
        File image = new File(imagesFolder, imageName);
        logger.info("Image File: '{}'", image.getAbsolutePath());
        assertTrue("Image File does not exist", image.exists());
        assertTrue("Image File is not a file", image.isFile());
        assertTrue("Image File cannot be read", image.canRead());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new FileInputStream(image), baos);
        byte[] imageContent = baos.toByteArray();
        Calendar before = createTimestampAndWait();
        uploadFile(client, rootFolderPath, image.getName(), imageContent, 200);

        StringWriter writer = new StringWriter();
        JsonFactory jf = new JsonFactory();
        JsonGenerator json = jf.createGenerator(writer);
        json.writeStartObject();
        json.writeStringField(JCR_PRIMARY_TYPE, ASSET_PRIMARY_TYPE);
        json.writeObjectFieldStart(JCR_CONTENT);
        json.writeStringField(JCR_PRIMARY_TYPE, ASSET_CONTENT_TYPE);
        json.writeStringField(JCR_MIME_TYPE, "application/octet-stream");
        // This is a very limited check on the Image Meta Data
        json.writeObjectFieldStart(METADATA);
        json.writeObjectFieldStart("icc-profile");
        json.writeStringField("class", "Display Device");
        json.writeStringField("color_space", "RGB ");
        // More Properties
        json.writeEndObject();
        json.writeObjectFieldStart("png-iccp");
        json.writeStringField("icc_profile_name", "ICC Profile");
        // More Properties
        json.writeEndObject();
        // More Metadata Folders
        json.writeEndObject();
        json.writeEndObject();
        json.writeEndObject();
        json.close();

        checkResourceByJson(client, rootFolderPath + "/" + imageName, 4, writer.toString(), true);
        checkLastModified(client, rootFolderPath + "/" + imageName, before);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
