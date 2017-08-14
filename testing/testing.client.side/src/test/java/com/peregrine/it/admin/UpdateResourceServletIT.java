package com.peregrine.it.admin;

import com.peregrine.it.basic.AbstractTest;
import com.peregrine.it.basic.JsonTest;
import com.peregrine.it.basic.JsonTest.BasicImpl;
import com.peregrine.it.basic.JsonTest.BasicListObject;
import com.peregrine.it.basic.JsonTest.BasicObject;
import com.peregrine.it.basic.JsonTest.ChildObject;
import com.peregrine.it.basic.JsonTest.NoNameObject;
import com.peregrine.it.basic.JsonTest.ObjectComponent;
import com.peregrine.it.basic.JsonTest.Prop;
import com.peregrine.it.basic.JsonTest.TestPage;
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
import java.util.Calendar;
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
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
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
    public void testUpdateSimplePage() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-usp";
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
        testPage.getContent().addChild(new ChildObject(jumboTronNodeName, EXAMPLE_JUMBOTRON_TYPE_PATH));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        before = createTimestampAndWait();
        NoNameObject simpleObject = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType(EXAMPLE_JUMBOTRON_TYPE_PATH)
            .addProperties(new Prop("title", "Hello"), new Prop("text", "Peregrine"));
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + jumboTronNodeName, simpleObject.toJSon(), 200);

        // Check page now
        testPage = new JsonTest.TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        testPage.getContent()
            .addChild(
                new JsonTest.ChildObject(jumboTronNodeName, EXAMPLE_JUMBOTRON_TYPE_PATH)
                    .addProperties(new Prop("title", "Hello"), new Prop("text", "Peregrine")
                )
            );
        checkResourceByJson(client, folderPath + "/" + pageName, 3, testPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);
    }

    @Test
    public void testUpdateSimpleObject() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-uso";
        createFolderStructure(client, folderPath);
        // Create a new source page
        String objectName = "test-object-1";
        response = createObject(client, folderPath, objectName, EXAMPLE_OBJECT_TYPE_PATH, 200);
        logger.info("Response from creating test object: '{}'", response.getContent());

        ObjectComponent simpleObject = new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH, null);
        checkResourceByJson(client, folderPath + "/" + objectName, 2, simpleObject.toJSon(), true);

        // Now we are ready to update that component
        BasicObject addPropertiesToObject = (BasicObject) new BasicObject(objectName, OBJECT_PRIMARY_TYPE).addSlingResourceType(EXAMPLE_OBJECT_TYPE_PATH)
            .addProperties(new Prop("name", "Hello"), new Prop("value", "Peregrine"));
        response = updateResource(client, folderPath + "/" + objectName, addPropertiesToObject.toJSon(), 200);

        // Check page now
        ObjectComponent simpleObjectWithProperties = new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH,
            new Prop("name", "Hello"), new Prop("value", "Peregrine"));
        checkResourceByJson(client, folderPath + "/" + objectName, 1, simpleObjectWithProperties.toJSon(), true);
    }

    @Test
    public void testUpdateObjectWithList() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-uowl";
        createFolderStructure(client, folderPath);
        // Create a new source page
        String objectName = "test-object-1";
        response = createObject(client, folderPath, objectName, EXAMPLE_OBJECT_TYPE_PATH, 200);
        logger.info("Response from creating test object: '{}'", response.getContent());

        ObjectComponent simpleObject = new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH, null);
        checkResourceByJson(client, folderPath + "/" + objectName, 2, simpleObject.toJSon(), true);

        // Now we are ready to add a list to the Object
        BasicObject addListToObject = (BasicObject) new BasicObject(objectName, OBJECT_PRIMARY_TYPE).addSlingResourceType(EXAMPLE_OBJECT_TYPE_PATH)
            .addChild(new BasicListObject("list",
                new BasicImpl("11").addProperties(
                    new Prop("name", "11"), new Prop("key", "one"), new Prop("value", "two")),
                new BasicImpl("12").addProperties(
                    new Prop("name", "12"), new Prop("key", "eins"), new Prop("value", "zwei")),
                new BasicImpl("13").addProperties(
                    new Prop("name", "13"), new Prop("key", "une"), new Prop("value", "deux"))
            ));
        response = updateResource(client, folderPath + "/" + objectName, addListToObject.toJSon(), 200);

        // Check page now
        // NOTE: Sling will not return this as a list but rather as a map with the name as object name
        //       and the rest as properties of that object something along the lines of:
        //       ..., "list": {"11"={"key":"one", "value":"two"}, "12"=...
        ObjectComponent simpleObjectWithList = (ObjectComponent) new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH)
            .addChild(new BasicImpl("list")
                .addChildren(
                    new BasicImpl("11").addProperties(
                        new Prop("key", "one"), new Prop("value", "two")),
                    new BasicImpl("12").addProperties(
                        new Prop("key", "eins"), new Prop("value", "zwei")),
                    new BasicImpl("13").addProperties(
                        new Prop("key", "une"), new Prop("value", "deux"))
                )
            );
        checkResourceByJson(client, folderPath + "/" + objectName, 3, simpleObjectWithList.toJSon(), true);

        BasicObject addAndRemoveListItems = (BasicObject) new BasicObject(objectName, OBJECT_PRIMARY_TYPE).addSlingResourceType(EXAMPLE_OBJECT_TYPE_PATH)
            .addChild(new BasicListObject("list",
                new BasicImpl("11").addProperties(
                    new Prop("name", "11"), new Prop("key", "one"), new Prop("value", "two")),
                new BasicImpl("12").addProperties(
                    new Prop("name", "12"), new Prop(DELETION_PROPERTY_NAME, "true")),
                new BasicImpl("13").addProperties(
                    new Prop("name", "13"), new Prop("key", "one"), new Prop("value", "two")),
                new BasicImpl("14").addProperties(
                    new Prop("name", "14"), new Prop("key", "uno"), new Prop("value", "due")),
                new BasicImpl("15").addProperties(
                    new Prop("name", "15"), new Prop("key", "1"), new Prop("value", "2"))
            ));
        response = updateResource(client, folderPath + "/" + objectName, addAndRemoveListItems.toJSon(), 200);

        ObjectComponent updatedSimpleObjectWithList = (ObjectComponent) new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH)
            .addChild(new BasicImpl("list")
                .addChildren(
                    new BasicImpl("11").addProperties(
                        new Prop("key", "one"), new Prop("value", "two")),
                    new BasicImpl("13").addProperties(
                        new Prop("key", "one"), new Prop("value", "two")),
                    new BasicImpl("14").addProperties(
                        new Prop("key", "uno"), new Prop("value", "due")),
                    new BasicImpl("15").addProperties(
                        new Prop("key", "1"), new Prop("value", "2"))
                )
            );
        checkResourceByJson(client, folderPath + "/" + objectName, 3, updatedSimpleObjectWithList.toJSon(), true);
    }

    @Test
    public void testUpdateObjectWithSingleList() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-uowsl";
        createFolderStructure(client, folderPath);
        // Create a new source page
        String objectName = "test-object-1";
        response = createObject(client, folderPath, objectName, EXAMPLE_OBJECT_TYPE_PATH, 200);
        logger.info("Response from creating test object: '{}'", response.getContent());

        ObjectComponent simpleObject = new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH, null);
        checkResourceByJson(client, folderPath + "/" + objectName, 2, simpleObject.toJSon(), true);

        // Now we are ready to add a list to the Object
        BasicObject addListToObject = (BasicObject) new BasicObject(objectName, OBJECT_PRIMARY_TYPE).addSlingResourceType(EXAMPLE_OBJECT_TYPE_PATH)
            .addChild(new BasicListObject("singleList",
                "one", "two", "three"
                )
            );
        response = updateResource(client, folderPath + "/" + objectName, addListToObject.toJSon(), 200);

        // Check page now
        // NOTE: Sling will not return this as a list but rather as a map with the name as object name
        //       and the rest as properties of that object something along the lines of:
        //       ..., "list": {"11"={"key":"one", "value":"two"}, "12"=...
        ObjectComponent simpleObjectWithList = (ObjectComponent) new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH)
            .addProperty(new Prop("singleList", "one", "two", "three"));
        checkResourceByJson(client, folderPath + "/" + objectName, 3, simpleObjectWithList.toJSon(), true);

        BasicObject addAndRemoveListItems = (BasicObject) new BasicObject(objectName, OBJECT_PRIMARY_TYPE).addSlingResourceType(EXAMPLE_OBJECT_TYPE_PATH)
            .addChild(new BasicListObject("singleList",
                    "one", "five", "three", "seven"
                )
            );
        response = updateResource(client, folderPath + "/" + objectName, addAndRemoveListItems.toJSon(), 200);

        ObjectComponent updatedSimpleObjectWithList = (ObjectComponent) new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH)
            .addProperty(new Prop("singleList", "one", "five", "three", "seven"));
        checkResourceByJson(client, folderPath + "/" + objectName, 3, updatedSimpleObjectWithList.toJSon(), true);
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

        TestPage emptyPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 2, emptyPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        insertNodeAtAsComponent(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, "/apps/" + EXAMPLE_CAROUSEL_TYPE_PATH, "into-after", 302);
        Map<String, Map> children = extractChildNodes(listResourceAsJson(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, 1));
        logger.info("List ");
        assertFalse("No Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.isEmpty());
        assertTrue("Too many Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.size() == 1);
        String carouselNodeName = children.keySet().iterator().next() + "";
        assertFalse("Carousel Name is not provided", carouselNodeName == null || carouselNodeName.isEmpty());

        TestPage pageWithCarousel = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        pageWithCarousel.getContent().addChild(new ChildObject(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, pageWithCarousel.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        String slide1Name = "slide-1";
        String image1Path = "/content/asset/slide-1-image";
        NoNameObject noNameObject = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType(EXAMPLE_CAROUSEL_TYPE_PATH)
            .addChild(new ChildObject(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH)
                .addProperties(new Prop("imagePath", image1Path))
            );
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, noNameObject.toJSon(), 200);

        TestPage pageWithCarouselAndImage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        pageWithCarouselAndImage.getContent()
            .addChild(new ChildObject(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH)
                .addChild(new ChildObject(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH)
                    .addProperties(new Prop("imagePath", image1Path))
                )
            );
        checkResourceByJson(client, folderPath + "/" + pageName, 3, pageWithCarouselAndImage.toJSon(), true);
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

        TestPage emptyPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 2, emptyPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        insertNodeAtAsComponent(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, "/apps/" + EXAMPLE_CAROUSEL_TYPE_PATH, "into-after", 302);
        Map<String, Map> children = extractChildNodes(listResourceAsJson(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, 1));
        logger.info("List ");
        assertFalse("No Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.isEmpty());
        assertTrue("Too many Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.size() == 1);
        String carouselNodeName = children.keySet().iterator().next() + "";
        assertFalse("Carousel Name is not provided", carouselNodeName == null || carouselNodeName.isEmpty());

        TestPage pageWithCarousel = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        pageWithCarousel.getContent()
            .addChild(new ChildObject(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, pageWithCarousel.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        String slide1Name = "slide-1";
        String image1Path = "/content/asset/slide-1-image";
        before = createTimestampAndWait();
        NoNameObject insertSlide = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType(EXAMPLE_CAROUSEL_TYPE_PATH)
            .addChild(new ChildObject(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH)
                .addProperties(new Prop("name", slide1Name), new Prop("imagePath", image1Path))
            );
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, insertSlide.toJSon(), 200);

        // Check page now
        TestPage pageWithCarouselAndSlide = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        pageWithCarouselAndSlide.getContent()
            .addChild(new ChildObject(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH)
                .addChild(new ChildObject(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH)
                    .addProperties(new Prop("imagePath", image1Path))
//AS TODO: Check if ignoring the 'name' property is done on purpose
//                    .addProperties(new Prop("name", slide1Name), new Prop("imagePath", image1Path))
                )
            );
        checkResourceByJson(client, folderPath + "/" + pageName, 3, pageWithCarouselAndSlide.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        // Now remove that child by indicating that the node has to be deleted
        before = createTimestampAndWait();
        // Not we are ready to update that component by adding a slide
        NoNameObject deleteNode = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType(EXAMPLE_CAROUSEL_TYPE_PATH)
            .addChild(new ChildObject(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH)
                .addProperties(new Prop(DELETION_PROPERTY_NAME, "true"))
            );
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, deleteNode.toJSon(), 200);

        // Check page now
        TestPage pageWithCarouselWithoutSlide = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        pageWithCarouselWithoutSlide.getContent()
            .addChild(new ChildObject(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH)
            );
        checkResourceByJson(client, folderPath + "/" + pageName, 3, pageWithCarouselWithoutSlide.toJSon(), true);        checkLastModified(client, folderPath + "/" + pageName, before);
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

        TestPage emptyPage = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        checkResourceByJson(client, folderPath + "/" + pageName, 2, emptyPage.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        insertNodeAtAsComponent(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, "/apps/" + EXAMPLE_CAROUSEL_TYPE_PATH, "into-after", 302);
        Map<String, Map> children = extractChildNodes(listResourceAsJson(client, folderPath + "/" + pageName + "/" + JCR_CONTENT, 1));
        logger.info("List ");
        assertFalse("No Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.isEmpty());
        assertTrue("Too many Children found of: " + folderPath + "/" + pageName + "/" + JCR_CONTENT, children.size() == 1);
        String carouselNodeName = children.keySet().iterator().next() + "";
        assertFalse("Carousel Name is not provided", carouselNodeName == null || carouselNodeName.isEmpty());

        TestPage pathWithCarousel = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        pathWithCarousel.getContent()
            .addChild(new ChildObject(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, pathWithCarousel.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        String slide1Name = "slide-1";
        String image1Path = "/content/asset/slide-1-image";
        before = createTimestampAndWait();
        // Not we are ready to update that component by adding a slide
        NoNameObject insertSlide = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType(EXAMPLE_CAROUSEL_TYPE_PATH)
            .addChild(new ChildObject(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH)
                .addProperties(new Prop("name", slide1Name), new Prop("imagePath", image1Path))
            );
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, insertSlide.toJSon(), 200);

        // Check page now
        TestPage pathWithCarouselAndSlide = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        pathWithCarouselAndSlide.getContent()
            .addChild(new ChildObject(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH)
                .addChild(new ChildObject(slide1Name, EXAMPLE_CAROUSEL_ITEM_TYPE_PATH)
                    .addProperties(new Prop("imagePath", image1Path))
//AS TODO: Check if ignoring the 'name' property is done on purpose
//                    .addProperties(new Prop("name", slide1Name), new Prop("imagePath", image1Path))
                )
            );
        checkResourceByJson(client, folderPath + "/" + pageName, 3, pathWithCarouselAndSlide.toJSon(), true);
        checkLastModified(client, folderPath + "/" + pageName, before);

        // Now remove that child by indicating that the node has to be deleted
        before = createTimestampAndWait();
        // Not we are ready to update that component by adding a slide
        NoNameObject deleteNode = (NoNameObject) new NoNameObject(NT_UNSTRUCTURED).addSlingResourceType(EXAMPLE_CAROUSEL_TYPE_PATH)
            .addProperty(new Prop(DELETION_PROPERTY_NAME, slide1Name));
        response = updateResource(client, folderPath + "/" + pageName + "/" + JCR_CONTENT + "/" + carouselNodeName, deleteNode.toJSon(), 200);

        // Check page now
        TestPage pathWithCarouselWithoutSlide = new TestPage(pageName, EXAMPLE_PAGE_TYPE_PATH, EXAMPLE_TEMPLATE_PATH);
        pathWithCarouselWithoutSlide.getContent()
            .addChild(new ChildObject(carouselNodeName, EXAMPLE_CAROUSEL_TYPE_PATH));
        checkResourceByJson(client, folderPath + "/" + pageName, 3, pathWithCarouselWithoutSlide.toJSon(), true);        checkLastModified(client, folderPath + "/" + pageName, before);
        checkLastModified(client, folderPath + "/" + pageName, before);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
