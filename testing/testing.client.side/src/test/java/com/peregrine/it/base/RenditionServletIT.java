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
import static com.peregrine.commons.util.PerConstants.JPEG_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.NT_RESOURCE;
import static com.peregrine.commons.util.PerConstants.PNG_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerConstants.WEBP_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.RENDITIONS;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.renderAsset;
import static com.peregrine.it.util.TestHarness.uploadFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

// TODO: We do not test the size of the rendition images
public class RenditionServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/renditions-test";
    public static final String IMAGES_SOURCE_FOLDER_PATH = "src/test/resources/images";

    // Test Source Images
    public static final String TEST_RESOURCE_PNG_IMAGE_NAME = "test.png";
    public static final String TEST_RESOURCE_JPG_IMAGE_NAME = "test.jpg";

    // Asset Names of the Images
    public static final String TEST_PNG_STRUCTURE_IMAGE_NAME = "test-structure.png";
    public static final String TEST_PNG_SIMPLE_IMAGE_NAME = "test-simple.png";
    public static final String TEST_PNG_SCALING_IMAGE_NAME = "test-scaling.png";
    public static final String TEST_WEBP_CONVERSION_IMAGE_NAME = "test-web-p.png";
    public static final String TEST_CUSTOMER_IMAGE_NAME = "test-customer.png";

    public static final String TEST_JPG_IMAGE_PLAIN_NAME = "test-plain.jpg";
    public static final String TEST_JPG_CONVERSION_IMAGE_NAME = "test-conversion.jpg";

    // Rendition Names of Assets (matches the Image Transformation Setup Name)
    public static final String THUMBNAIL_RENDITION_NAME = "thumbnail.png";
//    public static final String THUMBNAIL_PATH_RENDITION_NAME = "thumbnailPath.png";
    public static final String THUMBNAIL_PATH_RENDITION_NAME = "thumbnail.png";
    public static final String SCALING_100_RENDITION_NAME = "scaling100.png";
    public static final String WEBP_RENDITION_NAME = "convertTo.webp";

    // Customer Folders
    public static final String CUSTOMER_A_PATH = ROOT_PATH + "/" + "customerA";
    public static final String CUSTOMER_B_PATH = ROOT_PATH + "/" + "customerB";

    private static final Logger logger = LoggerFactory.getLogger(RenditionServletIT.class.getName());

    @ClassRule
    public static SlingInstanceRule slingInstanceRule = new SlingInstanceRule();

    private static SlingClient client;

    /**
     * Drop the Image Rendition Tests Folder to reset all tests. With this tests can be
     * reviewed after they ran
     **/
    @BeforeClass
    public static void setUpAll() throws IOException, ClientException {
        client = slingInstanceRule.getAdminClient();
        try {
            deleteFolder(client, ROOT_PATH, 200);
        } catch(ClientException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        } catch(IOException e) {
            logger.warn("Could not delete root path: '{}' -> ignore", ROOT_PATH, e);
        }
        // Upload the Test Assets to have something to rendition
        createFolderStructure(client, ROOT_PATH);
    }

    /**
     * Uploads a resource image name to Sling
     * @param imageName Name of the Image inside the test resources/images folder
     * @param imageMimeType Mime Type of the Image (set in the created Asset Node)
     * @param imageTargetName Name of the Asset (Image) Node
     */
    private void uploadImageFile(String imageName, String imageMimeType, String imageTargetName) throws IOException, ClientException {
        uploadImageFile(imageName, imageMimeType, ROOT_PATH, imageTargetName);
    }

    private void uploadImageFile(String imageName, String imageMimeType, String targetFolderPath, String imageTargetName) throws IOException, ClientException {
        File localFolder = new File(".");
        logger.info("Local Folder: '{}'", localFolder.getAbsolutePath());
        File imagesFolder = new File(localFolder, IMAGES_SOURCE_FOLDER_PATH);
        logger.info("Images Folder: '{}'", imagesFolder.getAbsolutePath());
        File image = new File(imagesFolder, imageName);
        logger.info("Image File: '{}'", image.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new FileInputStream(image), baos);
        byte[] imageContent = baos.toByteArray();
        uploadFile(client, targetFolderPath, imageTargetName, imageContent, imageMimeType, 200);
    }

    /** Tests the Upload of an Image and its created Node Structure **/
    @Test
    public void testImageStructureRequest() throws Exception {
        String resourcePath = ROOT_PATH + "/" + TEST_PNG_STRUCTURE_IMAGE_NAME;
        uploadImageFile(TEST_RESOURCE_PNG_IMAGE_NAME, PNG_MIME_TYPE, TEST_PNG_STRUCTURE_IMAGE_NAME);
        // First we check if the image is there and there are no renditions
        Map imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Original Image Map: '{}'", imageMap);
        assertFalse("Image Structure Data (JSon) is not found", imageMap.isEmpty());
    }

    /** Test the Upload and Asset Rendering of an Image **/
    @Test
    public void testPlainImageRetrieval() throws Exception {
        String resourcePath = ROOT_PATH + "/" + TEST_JPG_IMAGE_PLAIN_NAME;
        uploadImageFile(TEST_RESOURCE_JPG_IMAGE_NAME, JPEG_MIME_TYPE, TEST_JPG_IMAGE_PLAIN_NAME);
        SlingHttpResponse response = renderAsset(client, resourcePath, false, null, 200);
        assertEquals("Wrong Content Type from the Response", "Content-Type: " + JPEG_MIME_TYPE,response.getLastHeader("Content-Type") + "");
    }

    /** Tests the Creation of a Image Rendition w/o conversion **/
    @Test
    public void testSimpleThumbnailRendition() throws Exception {
        String resourcePath = ROOT_PATH + "/" + TEST_PNG_SIMPLE_IMAGE_NAME;
        uploadImageFile(TEST_RESOURCE_PNG_IMAGE_NAME, PNG_MIME_TYPE, TEST_PNG_SIMPLE_IMAGE_NAME);
        // First we check if the image is there and there are no renditions
        Map imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Original Image Map: '{}'", imageMap);
        assertFalse("No Image data provided (upload failed?)", imageMap.isEmpty());
        assertEquals("Image Name Primary Type is incorrect", ASSET_PRIMARY_TYPE, imageMap.get(JCR_PRIMARY_TYPE));
        assertNull("Renditions Folder is there but should not", imageMap.get(RENDITIONS));

        // Call Rendition Servlet
        SlingHttpResponse response = renderAsset(client, resourcePath, false, THUMBNAIL_RENDITION_NAME, 200);
        assertEquals("Wrong Content Type from the Response", "Content-Type: " + PNG_MIME_TYPE,response.getLastHeader("Content-Type") + "");

        imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Rendered Image Map: '{}'", imageMap);
        TestAsset imageWithRendition = (TestAsset) new TestAsset(PNG_MIME_TYPE)
            .addChild(
                new BasicObject(RENDITIONS, SLING_FOLDER)
                    .addChild(
                        new BasicWithContent(THUMBNAIL_RENDITION_NAME, NT_FILE, NT_RESOURCE, null)
                            .addContentProperty(new Prop(JCR_MIME_TYPE, PNG_MIME_TYPE))
                    )
            );
        logger.info("Expected PNG Image With Rendition: '{}'", imageWithRendition.toJSon());
        checkResourceByJson(client, resourcePath, 4, imageWithRendition.toJSon(), true);
    }

    /** Tests the Scaling of an Image w/o any conversion **/
    @Test
    public void testSimpleScalingRendition() throws Exception {
        String resourcePath = ROOT_PATH + "/" + TEST_PNG_SCALING_IMAGE_NAME;
        uploadImageFile(TEST_RESOURCE_PNG_IMAGE_NAME, PNG_MIME_TYPE, TEST_PNG_SCALING_IMAGE_NAME);
        // First we check if the image is there and there are no renditions
        Map imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Original Image Map: '{}'", imageMap);
        assertFalse("No Image data provided (upload failed?)", imageMap.isEmpty());
        assertEquals("Image Name Primary Type is incorrect", ASSET_PRIMARY_TYPE, imageMap.get(JCR_PRIMARY_TYPE));
        assertNull("Renditions Folder is there but should not", imageMap.get(RENDITIONS));

        // Call Rendition Servlet
        SlingHttpResponse response = renderAsset(client, resourcePath, false, SCALING_100_RENDITION_NAME, 200);
        assertEquals("Wrong Content Type from the Response", "Content-Type: " + PNG_MIME_TYPE,response.getLastHeader("Content-Type") + "");

        imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Rendered Image Map: '{}'", imageMap);
        TestAsset imageWithRendition = (TestAsset) new TestAsset(PNG_MIME_TYPE)
            .addChild(
                new BasicObject(RENDITIONS, SLING_FOLDER)
                    .addChild(
                        new BasicWithContent(SCALING_100_RENDITION_NAME, NT_FILE, NT_RESOURCE, null)
                            .addContentProperty(new Prop(JCR_MIME_TYPE, PNG_MIME_TYPE))
                    )
            );
        logger.info("Expected PNG Image With Rendition: '{}'", imageWithRendition.toJSon());
        checkResourceByJson(client, resourcePath, 4, imageWithRendition.toJSon(), true);
    }

    /** Tests the Conversion of an Image to Webp **/
    @Test
    public void testWebpConversionRendition() throws Exception {
        String resourcePath = ROOT_PATH + "/" + TEST_WEBP_CONVERSION_IMAGE_NAME;
        uploadImageFile(TEST_RESOURCE_PNG_IMAGE_NAME, PNG_MIME_TYPE, TEST_WEBP_CONVERSION_IMAGE_NAME);
        // First we check if the image is there and there are no renditions
        Map imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Original Image Map: '{}'", imageMap);
        assertFalse("No Image data provided (upload failed?)", imageMap.isEmpty());
        assertEquals("Image Name Primary Type is incorrect", ASSET_PRIMARY_TYPE, imageMap.get(JCR_PRIMARY_TYPE));
        assertNull("Renditions Folder is there but should not", imageMap.get(RENDITIONS));

        // Call Rendition Servlet
        SlingHttpResponse response = renderAsset(client, resourcePath, false, WEBP_RENDITION_NAME, 200);
        assertEquals("Wrong Content Type from the Response", "Content-Type: " + WEBP_MIME_TYPE,response.getLastHeader("Content-Type") + "");

        imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Rendered Image Map: '{}'", imageMap);
        TestAsset imageWithRendition = (TestAsset) new TestAsset(PNG_MIME_TYPE)
            .addChild(
                new BasicObject(RENDITIONS, SLING_FOLDER)
                    .addChild(
                        new BasicWithContent(WEBP_RENDITION_NAME, NT_FILE, NT_RESOURCE, null)
                            .addContentProperty(new Prop(JCR_MIME_TYPE, WEBP_MIME_TYPE))
                    )
            );
        logger.info("Expected PNG Image With Rendition: '{}'", imageWithRendition.toJSon());
        checkResourceByJson(client, resourcePath, 4, imageWithRendition.toJSon(), true);
    }

    /** Tests the Conversion and Thumbnailing of an Image **/
    @Test
    public void testConversionThumbnailRendition() throws Exception {
        String resourcePath = ROOT_PATH + "/" + TEST_JPG_CONVERSION_IMAGE_NAME;
        uploadImageFile(TEST_RESOURCE_JPG_IMAGE_NAME, JPEG_MIME_TYPE, TEST_JPG_CONVERSION_IMAGE_NAME);
        // First we check if the image is there and there are no renditions
        Map imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Original Image Map: '{}'", imageMap);
        assertFalse("No Image data provided (upload failed?)", imageMap.isEmpty());
        assertEquals("Image Name Primary Type is incorrect", ASSET_PRIMARY_TYPE, imageMap.get(JCR_PRIMARY_TYPE));
        assertNull("Renditions Folder is there but should not", imageMap.get(RENDITIONS));

        // Call Rendition Servlet
        SlingHttpResponse response = renderAsset(client, resourcePath, false, THUMBNAIL_RENDITION_NAME, 200);
        assertEquals("Wrong Content Type from the Response", "Content-Type: " + PNG_MIME_TYPE,response.getLastHeader("Content-Type") + "");

        imageMap = listResourceAsJson(client, resourcePath, 4);
        logger.info("Rendered Image Map: '{}'", imageMap);
        TestAsset imageWithRendition = (TestAsset) new TestAsset(JPEG_MIME_TYPE)
            .addChild(
                new BasicObject(RENDITIONS, SLING_FOLDER)
                    .addChild(
                        new BasicWithContent(THUMBNAIL_RENDITION_NAME, NT_FILE, NT_RESOURCE, null)
                            .addContentProperty(new Prop(JCR_MIME_TYPE, PNG_MIME_TYPE))
                    )
            );
        logger.info("Expected JPEG Image With Rendition: '{}'", imageWithRendition.toJSon());
        checkResourceByJson(client, resourcePath, 4, imageWithRendition.toJSon(), true);
    }

    /** Tests the Thumbnailing on Path Restriction **/
    @Test
    public void testThumbnailOnPathRendition() throws Exception {
        createFolderStructure(client, CUSTOMER_A_PATH);
        createFolderStructure(client, CUSTOMER_B_PATH);
        String resourceAPath = CUSTOMER_A_PATH + "/" + TEST_CUSTOMER_IMAGE_NAME;
        String resourceBPath = CUSTOMER_B_PATH + "/" + TEST_CUSTOMER_IMAGE_NAME;
        uploadImageFile(TEST_RESOURCE_PNG_IMAGE_NAME, PNG_MIME_TYPE, CUSTOMER_A_PATH, TEST_CUSTOMER_IMAGE_NAME);
        uploadImageFile(TEST_RESOURCE_PNG_IMAGE_NAME, PNG_MIME_TYPE, CUSTOMER_B_PATH, TEST_CUSTOMER_IMAGE_NAME);

        // First we check if the image is there and there are no renditions
        checkUploadedImage(resourceAPath, "Customer A");
        checkUploadedImage(resourceBPath, "Customer B");

        // Call Rendition Servlet
        SlingHttpResponse response = renderAsset(client, resourceAPath, false, THUMBNAIL_PATH_RENDITION_NAME, 200);
        assertEquals("Wrong Content Type (Customer A) from the Response", "Content-Type: " + PNG_MIME_TYPE,response.getLastHeader("Content-Type") + "");
        response = renderAsset(client, resourceBPath, false, THUMBNAIL_PATH_RENDITION_NAME, 200);
        assertEquals("Wrong Content Type (Customer B) from the Response", "Content-Type: " + PNG_MIME_TYPE,response.getLastHeader("Content-Type") + "");

        Map imageMap = listResourceAsJson(client, resourceAPath, 4);
        logger.info("Rendered Image Map (Customer A): '{}'", imageMap);
        imageMap = listResourceAsJson(client, resourceBPath, 4);
        logger.info("Rendered Image Map (Customer B): '{}'", imageMap);
        TestAsset imageWithRendition = (TestAsset) new TestAsset(PNG_MIME_TYPE)
            .addChild(
                new BasicObject(RENDITIONS, SLING_FOLDER)
                    .addChild(
                        new BasicWithContent(THUMBNAIL_PATH_RENDITION_NAME, NT_FILE, NT_RESOURCE, null)
                            .addContentProperty(new Prop(JCR_MIME_TYPE, PNG_MIME_TYPE))
                    )
            );
        logger.info("Expected JPEG Image With Rendition: '{}'", imageWithRendition.toJSon());
        checkResourceByJson(client, resourceAPath, 4, imageWithRendition.toJSon(), true);
        checkResourceByJson(client, resourceBPath, 4, imageWithRendition.toJSon(), true);
    }

    private void checkUploadedImage(String assetPath, String hint) throws IOException, ClientException {
        String hintFix = hint == null || hint.isEmpty() ?
            "" : " (" + hint + ")";
        Map imageMap = listResourceAsJson(client, assetPath, 4);
        logger.info("Original Image Map{}: '{}'", hintFix, imageMap);
        assertFalse("No Image" + hintFix + " data provided (upload failed?)", imageMap.isEmpty());
        assertEquals("Image Name" + hintFix + " Primary Type is incorrect", ASSET_PRIMARY_TYPE, imageMap.get(JCR_PRIMARY_TYPE));
        assertNull("Renditions Folder" + hintFix + " is there but should not", imageMap.get(RENDITIONS));
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
