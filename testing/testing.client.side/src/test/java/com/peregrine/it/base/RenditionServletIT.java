package com.peregrine.it.base;

import com.peregrine.commons.test.AbstractTest;
import com.peregrine.it.basic.JsonTest.BasicObject;
import com.peregrine.it.basic.JsonTest.BasicWithContent;
import com.peregrine.it.basic.JsonTest.Prop;
import com.peregrine.it.basic.JsonTest.TestAsset;
import org.apache.commons.io.IOUtils;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
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
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.NT_RESOURCE;
import static com.peregrine.commons.util.PerConstants.PNG_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerUtil.RENDITIONS;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.renderAsset;
import static com.peregrine.it.util.TestHarness.uploadFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RenditionServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/renditions-test";
    public static final String IMAGES_SOURCE_FOLDER_PATH = "src/test/resources/images";
    public static final String TEST_PNG_IMAGE_NAME = "test.png";
    public static final String TEST_JPG_IMAGE_NAME = "test.jpg";
    public static final String THUMBNAIL_RENDITION = "thumbnail.png";
    public static final String TEST_PNG_IMAGE_RESOURCE_PATH = ROOT_PATH + "/" + TEST_PNG_IMAGE_NAME;
    public static final String TEST_JPG_IMAGE_RESOURCE_PATH = ROOT_PATH + "/" + TEST_JPG_IMAGE_NAME;

    private static final Logger logger = LoggerFactory.getLogger(RenditionServletIT.class.getName());

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
        // Upload the Test Assets to have something to rendition
        createFolderStructure(client, ROOT_PATH);

        File localFolder = new File(".");
        logger.info("Local Folder: '{}'", localFolder.getAbsolutePath());
        File imagesFolder = new File(localFolder, IMAGES_SOURCE_FOLDER_PATH);
        logger.info("Images Folder: '{}'", imagesFolder.getAbsolutePath());
        File image = new File(imagesFolder, TEST_PNG_IMAGE_NAME);
        logger.info("Image File: '{}'", image.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new FileInputStream(image), baos);
        byte[] imageContent = baos.toByteArray();
        uploadFile(client, ROOT_PATH, image.getName(), imageContent, PNG_MIME_TYPE, 200);
        baos = new ByteArrayOutputStream();
        image = new File(imagesFolder, TEST_JPG_IMAGE_NAME);
        IOUtils.copy(new FileInputStream(image), baos);
        imageContent = baos.toByteArray();
        uploadFile(client, ROOT_PATH, image.getName(), imageContent, "image/jpeg", 200);
    }

    @Test
    public void testImageStructureRequest() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        // First we check if the image is there and there are no renditions
        Map imageMap = listResourceAsJson(client, TEST_PNG_IMAGE_RESOURCE_PATH, 4);
        logger.info("Original Image Map: '{}'", imageMap);
        assertFalse("Image Structure Data (JSon) is not found", imageMap.isEmpty());
    }

    @Test
    public void testSimpleThumbnailRendition() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        // First we check if the image is there and there are no renditions
        Map imageMap = listResourceAsJson(client, TEST_PNG_IMAGE_RESOURCE_PATH, 4);
        logger.info("Original Image Map: '{}'", imageMap);
        assertFalse("No Image data provided (upload failed?)", imageMap.isEmpty());
        assertEquals("Image Name Primary Type is incorrect", ASSET_PRIMARY_TYPE, imageMap.get(JCR_PRIMARY_TYPE));
        assertNull("Renditions Folder is there but should not", imageMap.get(RENDITIONS));

        // Call Rendition Servlet
        SlingHttpResponse response = renderAsset(client, TEST_PNG_IMAGE_RESOURCE_PATH, false, THUMBNAIL_RENDITION, 200);
        assertEquals("Wrong Content Type from the Response", "Content-Type: image/png",response.getLastHeader("Content-Type") + "");

        imageMap = listResourceAsJson(client, TEST_PNG_IMAGE_RESOURCE_PATH, 4);
        logger.info("Rendered Image Map: '{}'", imageMap);
        TestAsset imageWithRendition = (TestAsset) new TestAsset(PNG_MIME_TYPE)
            .addChild(
                new BasicObject(RENDITIONS, SLING_FOLDER)
                    .addChild(
                        new BasicWithContent(THUMBNAIL_RENDITION, NT_FILE, NT_RESOURCE, null)
                            .addContentProperty(new Prop(JCR_MIME_TYPE, PNG_MIME_TYPE))
                    )
            );
        logger.info("Expected PNG Image With Rendition: '{}'", imageWithRendition.toJSon());
        checkResourceByJson(client, TEST_PNG_IMAGE_RESOURCE_PATH, 4, imageWithRendition.toJSon(), true);
    }

    @Test
    public void testConversionThumbnailRendition() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        // First we check if the image is there and there are no renditions
        Map imageMap = listResourceAsJson(client, TEST_JPG_IMAGE_RESOURCE_PATH, 4);
        logger.info("Original Image Map: '{}'", imageMap);
        assertFalse("No Image data provided (upload failed?)", imageMap.isEmpty());
        assertEquals("Image Name Primary Type is incorrect", ASSET_PRIMARY_TYPE, imageMap.get(JCR_PRIMARY_TYPE));
        assertNull("Renditions Folder is there but should not", imageMap.get(RENDITIONS));

        // Call Rendition Servlet
        SlingHttpResponse response = renderAsset(client, TEST_JPG_IMAGE_RESOURCE_PATH, false, THUMBNAIL_RENDITION, 200);
        assertEquals("Wrong Content Type from the Response", "Content-Type: image/png",response.getLastHeader("Content-Type") + "");

        imageMap = listResourceAsJson(client, TEST_PNG_IMAGE_RESOURCE_PATH, 4);
        logger.info("Rendered Image Map: '{}'", imageMap);
        TestAsset imageWithRendition = (TestAsset) new TestAsset("image/jpeg")
            .addChild(
                new BasicObject(RENDITIONS, SLING_FOLDER)
                    .addChild(
                        new BasicWithContent(THUMBNAIL_RENDITION, NT_FILE, NT_RESOURCE, null)
                            .addContentProperty(new Prop(JCR_MIME_TYPE, PNG_MIME_TYPE))
                    )
            );
        logger.info("Expected JPEG Image With Rendition: '{}'", imageWithRendition.toJSon());
        checkResourceByJson(client, TEST_JPG_IMAGE_RESOURCE_PATH, 4, imageWithRendition.toJSon(), true);
    }

    @Test
    public void testPlainImageRetrieval() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = renderAsset(client, TEST_JPG_IMAGE_RESOURCE_PATH, false, null, 200);
        assertEquals("Wrong Content Type from the Response", "Content-Type: image/jpeg",response.getLastHeader("Content-Type") + "");
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
