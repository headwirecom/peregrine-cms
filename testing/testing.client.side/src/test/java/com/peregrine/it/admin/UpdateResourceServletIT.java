package com.peregrine.it.admin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.peregrine.admin.resource.AdminResourceHandlerService.DELETION_PROPERTY_NAME;
import static com.peregrine.it.basic.BasicTestHelpers.checkLastModified;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.BasicTestHelpers.createTimestampAndWait;
import static com.peregrine.it.basic.BasicTestHelpers.extractChildNodes;
import static com.peregrine.it.basic.BasicTestHelpers.listResourceAsJson;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_CAROUSEL_ITEM_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_CAROUSEL_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_JUMBOTRON_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_OBJECT_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.it.util.TestHarness.createObject;
import static com.peregrine.it.util.TestHarness.createPage;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.insertNodeAtAsComponent;
import static com.peregrine.it.util.TestHarness.updateResource;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by schaefa on 6/22/17.
 */
public class UpdateResourceServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/test-update-resource";

    private static final Logger logger = LoggerFactory.getLogger(UpdateResourceServletIT.class.getName());

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
    public void testUpdatePage() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-up";
        createFolderStructure(client, folderPath);
        // Create a new source page
        Calendar before = createTimestampAndWait();
        String pageName = "test-page-1";
        response = createPage(client, folderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        logger.info("Response from creating test page: '{}'", response.getContent());

        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 2, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        insertNodeAtAsComponent(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, "/apps/" + EXAMPLE_JUMBOTRON_TYPE_PATH, "into-after", 302);
        Map<String, Map> children = extractChildNodes(listResourceAsJson(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, 1));
        logger.info("List ");
        assertFalse("No Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.isEmpty());
        assertTrue("Too many Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.size() == 1);
        String jumboTronNodeName = children.keySet().iterator().next() + "";
        assertFalse("Jumbo Tron Name is not provided", jumboTronNodeName == null || jumboTronNodeName.isEmpty());

        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        testPage.content.addChild(jumboTronNodeName, EXAMPLE_JUMBOTRON_TYPE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        before = createTimestampAndWait();
        SimpleObject simpleObject = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_JUMBOTRON_TYPE_PATH, null)
            .addProps(new Prop("title", "Hello"), new Prop("text", "Peregrine"));
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + jumboTronNodeName, simpleObject.toJSon(), 200);

        // Check page now
        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        testPage.content.addChild(jumboTronNodeName, EXAMPLE_JUMBOTRON_TYPE_PATH, new Prop("title", "Hello"), new Prop("text", "Peregrine"));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);
    }


    @Test
    public void testUpdateObject() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-uo";
        createFolderStructure(client, folderPath);
        // Create a new source page
        String objectName = "test-object-1";
        response = createObject(client, folderPath, objectName, EXAMPLE_OBJECT_TYPE_PATH, 200);
        logger.info("Response from creating test object: '{}'", response.getContent());

        SimpleObject simpleObject = new SimpleObject(OBJECT_PRIMARY_TYPE, EXAMPLE_OBJECT_TYPE_PATH, objectName);
        checkResourceByJson(client, folderPath + "/" + objectName, 2, simpleObject.toJSon(), true);

        // Now we are ready to update that component
        simpleObject = new SimpleObject(OBJECT_PRIMARY_TYPE, EXAMPLE_OBJECT_TYPE_PATH, objectName)
            .addProps(new Prop("name", "Hello"), new Prop("value", "Peregrine"));
        response = updateResource(client, folderPath + "/" + objectName, simpleObject.toJSon(), 200);

        // Check page now
        simpleObject = new SimpleObject(OBJECT_PRIMARY_TYPE, EXAMPLE_OBJECT_TYPE_PATH, objectName)
            .addProps(new Prop("name", "Hello"), new Prop("value", "Peregrine"));
        checkResourceByJson(client, folderPath + "/" + objectName, 1, simpleObject.toJSon(), true);
    }

    @Test
    public void testUpdatePageWithNewChildNodes() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-upwncn";
        createFolderStructure(client, folderPath);
        // Create a new source page
        Calendar before = createTimestampAndWait();
        String pageName = "test-page-1";
        response = createPage(client, folderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        logger.info("Response from creating test page: '{}'", response.getContent());

        JsonFactory jf = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator json = jf.createGenerator(writer);
        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 2, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        insertNodeAtAsComponent(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, "/apps/" + EXAMPLE_CAROUSEL_TYPE_PATH, "into-after", 302);
        Map<String, Map> children = extractChildNodes(listResourceAsJson(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, 1));
        logger.info("List ");
        assertFalse("No Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.isEmpty());
        assertTrue("Too many Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.size() == 1);
        String carouselNodeName = children.keySet().iterator().next() + "";
        assertFalse("Carousel Name is not provided", carouselNodeName == null || carouselNodeName.isEmpty());

        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        testPage.content.addChild(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        String slide1Name = "slide-1";
        String image1Path = "/content/asset/slide-1-image";
        SimpleObject simpleObject = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_CAROUSEL_TYPE_PATH, null);
        simpleObject.addChild(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH, new Prop("imagePath", image1Path));
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, simpleObject.toJSon(), 200);

        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        SimpleObject carousel = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_CAROUSEL_TYPE_PATH, null);
        testPage.content.addChild(carouselNodeName, carousel);
        carousel.addChild(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH, new Prop("imagePath", image1Path));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);
    }

    @Test
    public void testUpdatePageWithDeletedChildNodeByFlag() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-upwdcnbf";
        createFolderStructure(client, folderPath);
        // Create a new source page
        Calendar before = createTimestampAndWait();
        String pageName = "test-page-1";
        response = createPage(client, folderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        logger.info("Response from creating test page: '{}'", response.getContent());

        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 2, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        insertNodeAtAsComponent(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, "/apps/" + EXAMPLE_CAROUSEL_TYPE_PATH, "into-after", 302);
        Map<String, Map> children = extractChildNodes(listResourceAsJson(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, 1));
        logger.info("List ");
        assertFalse("No Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.isEmpty());
        assertTrue("Too many Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.size() == 1);
        String carouselNodeName = children.keySet().iterator().next() + "";
        assertFalse("Carousel Name is not provided", carouselNodeName == null || carouselNodeName.isEmpty());

        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        testPage.content.addChild(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        String slide1Name = "slide-1";
        String image1Path = "/content/asset/slide-1-image";
        before = createTimestampAndWait();
        SimpleObject simpleObject = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_CAROUSEL_TYPE_PATH, null);
        simpleObject.addChild(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH, new Prop("name", slide1Name), new Prop("imagePath", image1Path));
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, simpleObject.toJSon(), 200);

        // Check page now
        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        SimpleObject carousel = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_CAROUSEL_TYPE_PATH, null);
        testPage.content.addChild(carouselNodeName, carousel);
        carousel.addChild(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH, new Prop("imagePath", image1Path));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        // Now remove that child by indicating that the node has to be deleted
        before = createTimestampAndWait();
        // Not we are ready to update that component by adding a slide
        simpleObject = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_CAROUSEL_TYPE_PATH, null);
        simpleObject.addChild(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH, new Prop(DELETION_PROPERTY_NAME, "true"));
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, simpleObject.toJSon(), 200);

        // Check page now
        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        testPage.content.addChild(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);        checkLastModified(client, folderPath + "/" + pageName, before);
    }

    @Test
    public void testUpdatePageWithDeletedChildNodeByPath() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-upwdcnbp";
        createFolderStructure(client, folderPath);
        // Create a new source page
        Calendar before = createTimestampAndWait();
        String pageName = "test-page-1";
        response = createPage(client, folderPath, pageName, EXAMPLE_TEMPLATE_PATH, 200);
        logger.info("Response from creating test page: '{}'", response.getContent());

        TestPage testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 2, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        insertNodeAtAsComponent(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, "/apps/" + EXAMPLE_CAROUSEL_TYPE_PATH, "into-after", 302);
        Map<String, Map> children = extractChildNodes(listResourceAsJson(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, 1));
        logger.info("List ");
        assertFalse("No Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.isEmpty());
        assertTrue("Too many Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.size() == 1);
        String carouselNodeName = children.keySet().iterator().next() + "";
        assertFalse("Carousel Name is not provided", carouselNodeName == null || carouselNodeName.isEmpty());

        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        testPage.content.addChild(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        String slide1Name = "slide-1";
        String image1Path = "/content/asset/slide-1-image";
        before = createTimestampAndWait();
        // Not we are ready to update that component by adding a slide
        SimpleObject simpleObject = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_CAROUSEL_TYPE_PATH, null);
        simpleObject.addChild(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH, new Prop("name", slide1Name), new Prop("imagePath", image1Path));
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, simpleObject.toJSon(), 200);

        // Check page now
        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        SimpleObject carousel = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_CAROUSEL_TYPE_PATH, null);
        testPage.content.addChild(carouselNodeName, carousel);
        carousel.addChild(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH, new Prop("imagePath", image1Path));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        // Now remove that child by indicating that the node has to be deleted
        before = createTimestampAndWait();
        // Not we are ready to update that component by adding a slide
        simpleObject = new SimpleObject(NT_UNSTRUCTURED, EXAMPLE_CAROUSEL_TYPE_PATH, null).addProps(new Prop(DELETION_PROPERTY_NAME, slide1Name));
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, simpleObject.toJSon(), 200);

        // Check page now
        testPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        testPage.content.addChild(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);        checkLastModified(client, folderPath + "/" + pageName, before);
        checkLastModified(client, folderPath + "/" + pageName, before);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static interface TestObject {
        String toJSon();
        Map getMap();
    }

    private static class BaseTestObject implements TestObject {
        Map map = new LinkedHashMap();
        @Override
        public String toJSon() {
            StringWriter writer = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writeValue(writer, map);
                writer.close();
            } catch (IOException e) {
                logger.error("not able to create string writer", e);
            }
            return writer.toString();
        }

        @Override
        public Map getMap() {
            return map;
        }
    }

    private static class TestPage extends BaseTestObject {
        ContentObject content;
        public TestPage(String name, String slingResourceType, String templatePath) {
            map.put(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
            content = new ContentObject(slingResourceType);
            content.map.put(JCR_TITLE, name);
            content.map.put(TEMPLATE, templatePath);
            map.put(JCR_CONTENT, content.map);
        }
    }

    private static class SimpleObject extends BaseTestObject {
        public SimpleObject(String primaryType, String slingResourceType, String title) {
            map.put(JCR_PRIMARY_TYPE, primaryType);
            map.put(SLING_RESOURCE_TYPE, slingResourceType);
            if(title != null && !title.isEmpty()) {
                map.put(JCR_TITLE, title);
            }
        }

        public SimpleObject addProps(Prop...props) {
            if(props != null) {
                for(Prop prop : props) {
                    map.put(prop.name, prop.value);
                }
            }
            return this;
        }

        public SimpleObject addChild(SimpleObject child) {
            return addChild(null, child);
        }

        public SimpleObject addChild(String name, SimpleObject child) {
            if(name == null || name.isEmpty()) {
                Object temp = child.map.get("name");
                if(temp == null) {
                    throw new IllegalArgumentException("No Name provided");
                }
                name = temp.toString();
            }
            map.put(name, child.getMap());
            return this;
        }

        public SimpleObject addChild(String name, String slingResourceType) {
            return addChild(name, slingResourceType, null);
        }

        public SimpleObject addChild(String name, String slingResourceType, Prop...props) {
            ChildObject childObject = new ChildObject(slingResourceType);
            if(props != null) {
                for(Prop prop : props) {
                    childObject.map.put(prop.name, prop.value);
                }
            }
            map.put(name, childObject.getMap());
            return this;
        }

        public SimpleObject addChild(String name, String primaryType, String slingResourceType, Prop...props) {
            ChildObject childObject = new ChildObject(primaryType, slingResourceType);
            if(props != null) {
                for(Prop prop : props) {
                    childObject.map.put(prop.name, prop.value);
                }
            }
            map.put(name, childObject.getMap());
            return this;
        }
    }

    private static class ContentObject extends SimpleObject {
        public ContentObject(String slingResourceType) {
            super(PAGE_CONTENT_TYPE, slingResourceType, null);
        }
    }

    private static class ChildObject extends BaseTestObject {
        public ChildObject(String slingResourceType) {
            this(NT_UNSTRUCTURED, slingResourceType);
        }

        public ChildObject(String primaryType, String slingResourceType) {
            map.put(JCR_PRIMARY_TYPE, primaryType);
            map.put(SLING_RESOURCE_TYPE, slingResourceType);
        }
    }

    private static class Prop {
        String name, value;
        public Prop(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
