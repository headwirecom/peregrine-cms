package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
import java.io.StringWriter;
import java.util.Calendar;

import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.basic.BasicTestHelpers.loadFile;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.uploadFile;
import static com.peregrine.commons.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerUtil.METADATA;

/**
 * Created by Andreas Schaefer on 7/6/17.
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

        byte[] imageContent = loadFile(IMAGE_RESOURCES_PATH, imageName, "Test - PNG");
        Calendar before = createTimestampAndWait();
        uploadFile(client, rootFolderPath, imageName, imageContent, 200);

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

//        json.writeObjectFieldStart("icc-profile");
//        json.writeStringField("class", "Display Device");
//        json.writeStringField("color_space", "RGB ");
//        // More Properties
//        json.writeEndObject();
//        json.writeObjectFieldStart("png-iccp");
//        json.writeStringField("icc_profile_name", "ICC Profile");
//        // More Properties
//        json.writeEndObject();

        json.writeObjectFieldStart("png-srgb");
        json.writeStringField("srgb_rendering_intent", "Perceptual");
        json.writeEndObject();

        json.writeObjectFieldStart("xmp");
        json.writeStringField("xmp_value_count", "11");
        json.writeEndObject();

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
