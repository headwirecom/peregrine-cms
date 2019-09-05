package com.peregrine.it.admin;

import com.peregrine.commons.test.AbstractTest;
import com.peregrine.it.basic.JsonTest.BasicImpl;
import com.peregrine.it.basic.JsonTest.BasicListObject;
import com.peregrine.it.basic.JsonTest.BasicObject;
import com.peregrine.it.basic.JsonTest.ObjectComponent;
import com.peregrine.it.basic.JsonTest.Prop;
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

import static com.peregrine.admin.resource.AdminResourceHandlerService.DELETION_PROPERTY_NAME;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.it.basic.BasicTestHelpers.checkResourceByJson;
import static com.peregrine.it.basic.BasicTestHelpers.createFolderStructure;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_OBJECT_TYPE_PATH;
import static com.peregrine.it.util.TestHarness.createObject;
import static com.peregrine.it.util.TestHarness.deleteFolder;
import static com.peregrine.it.util.TestHarness.updateResource;

/**
 * Created by Andreas Schaefer on 6/22/17.
 */
public class UpdateObjectResourceServletIT
    extends AbstractTest
{
    public static final String ROOT_PATH = "/content/tests/test-update-object-resource";

    private static final Logger logger = LoggerFactory.getLogger(UpdateObjectResourceServletIT.class.getName());

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
    public void testUpdateObjectWithListReorder() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-uowlr";
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
                new BasicImpl("14").addProperties(
                    new Prop("name", "14"), new Prop("key", "uno"), new Prop("value", "due")),
                new BasicImpl("15").addProperties(
                    new Prop("name", "15"), new Prop("key", "1"), new Prop("value", "2")),
                new BasicImpl("13").addProperties(
                    new Prop("name", "13"), new Prop("key", "one"), new Prop("value", "two"))
            ));
        response = updateResource(client, folderPath + "/" + objectName, addAndRemoveListItems.toJSon(), 200);

        ObjectComponent updatedSimpleObjectWithList = (ObjectComponent) new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH)
            .addChild(new BasicImpl("list")
                .addChildren(
                    new BasicImpl("11").addProperties(
                        new Prop("key", "one"), new Prop("value", "two")),
                    new BasicImpl("14").addProperties(
                        new Prop("key", "uno"), new Prop("value", "due")),
                    new BasicImpl("15").addProperties(
                        new Prop("key", "1"), new Prop("value", "2")),
                    new BasicImpl("13").addProperties(
                        new Prop("key", "one"), new Prop("value", "two"))
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
    public void testUpdateObjectWithSingleListReorder() throws Exception {
        SlingClient client = slingInstanceRule.getAdminClient();
        SlingHttpResponse response = null;
        String folderPath = ROOT_PATH + "/test-uowslr";
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
                    "five", "three", "one", "seven"
                )
            );
        response = updateResource(client, folderPath + "/" + objectName, addAndRemoveListItems.toJSon(), 200);

        ObjectComponent updatedSimpleObjectWithList = (ObjectComponent) new ObjectComponent(objectName, EXAMPLE_OBJECT_TYPE_PATH)
            .addProperty(new Prop("singleList", "five", "three", "one", "seven"));
        checkResourceByJson(client, folderPath + "/" + objectName, 3, updatedSimpleObjectWithList.toJSon(), true);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
