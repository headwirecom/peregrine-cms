package com.peregrine.it.wrapper;

import com.peregrine.adaption.PerPage;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.test.AbstractTest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.rules.TeleporterRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.it.basic.TestConstants.EXAMPLE_PAGE_TYPE_PATH;
import static com.peregrine.it.basic.TestConstants.EXAMPLE_TEMPLATE_PATH;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.commons.util.PerUtil.getResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * These tests ensure the proper traversing of a page tree using the PerPage
 * getNext() / getPrevious() methods.
 *
 * Keep in mind that because of the stateless nature of these methods the
 * end of the traversing is defined by the first resource found that is not
 * a Page. The IntermediatePageTraversing are illustrating / testing that point.
 *
 * Created by Andreas Schaefer on 7/5/17.
 */
public class PageTraversingIT
    extends AbstractTest
{

    private static final Logger logger = LoggerFactory.getLogger(PageTraversingIT.class.getName());

    public static final String ROOT_PATH = "/content/tests/server-side/page-traversing";

    @Rule
    public final TeleporterRule teleporter = TeleporterRule.forClass(getClass(), "PeregrineTeleporter");

    /** Just make sure that the Resource Resolver is available **/
    @Test
    public void testResourceResolver() throws IOException, LoginException {
        logger.info("Start Resource Resolver Check");
        ResourceResolver resourceResolver = getResourceResolver();
        Resource testFolder = resourceResolver.getResource("/content");
        logger.info("Got Test Folder: '{}'", testFolder);
        assertNotNull("Test Folder is not found", testFolder);
        Iterable<Resource> testFolders = testFolder.getChildren();
        boolean ok = false;
        for(Resource child: testFolders) {
            logger.info("Test Folder child: '{}'", child.getPath());
            ok = true;
        }
        assertTrue("No Children found in /content", ok);
    }

    /** Test the forward traversing of a Page Tree **/
    @Test
    public void testForwardPageTraversing() throws Exception {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = getResourceResolver();
            // Create a Page Structure to be traversed in the test
            AdminResourceHandler resourceManagement = teleporter.getService(AdminResourceHandler.class);
            String testFolderPath = ROOT_PATH + "/test-fpt";
            Resource testFolder = createFolderStructure(resourceResolver, resourceManagement, testFolderPath);
            String rootPageName = "root";
            Resource root = createChildPage(resourceManagement, testFolder, rootPageName);
            Resource l1 = createChildPage(resourceManagement, testFolder, "l-1");
            Resource r1 = createChildPage(resourceManagement, testFolder, "r-1");
            logger.info("Root: '{}'", root == null ? "null" : root.getPath());
            // Create child structure
            Resource child1 = createChildPage(resourceManagement, root, "child-1");
            Resource child2 = createChildPage(resourceManagement, root, "child-2");
            Resource child11 = createChildPage(resourceManagement, child1, "child-11");
            Resource child12 = createChildPage(resourceManagement, child1, "child-12");
            Resource child13 = createChildPage(resourceManagement, child1, "child-13");
            Resource child111 = createChildPage(resourceManagement, child11, "child-111");
            Resource child112 = createChildPage(resourceManagement, child11, "child-112");
            Resource child121 = createChildPage(resourceManagement, child12, "child-121");
            Resource child21 = createChildPage(resourceManagement, child2, "child-21");
            Resource child22 = createChildPage(resourceManagement, child2, "child-22");
            Resource[] tree = new Resource[]{child1, child11, child111, child112, child12, child121, child13, child2, child21, child22};

            // Now traverse and check the correct folder
            PerPage page = root.adaptTo(PerPage.class);
            for(int i = 0; i < tree.length; i++) {
                String name = page.getName();
                page = page.getNext();
                assertEquals("Wrong Page on Next of " + name, tree[i].getName(), page.getName());
            }
            page = page.getNext();
            assertNull("At least one more page found", page);
        } finally {
            if(resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    /** Test the forward traversing of a Page Tree **/
    @Test
    public void testForwardIntermediatePageTraversing() throws Exception {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = getResourceResolver();
            // Create a Page Structure to be traversed in the test
            AdminResourceHandler resourceManagement = teleporter.getService(AdminResourceHandler.class);
            String testFolderPath = ROOT_PATH + "/test-fipt";
            Resource testFolder = createFolderStructure(resourceResolver, resourceManagement, testFolderPath);
            String rootPageName = "root";
            Resource root = createChildPage(resourceManagement, testFolder, rootPageName);
            Resource l1 = createChildPage(resourceManagement, testFolder, "l-1");
            Resource r1 = createChildPage(resourceManagement, testFolder, "r-1");
            logger.info("Root: '{}'", root == null ? "null" : root.getPath());
            // Create child structure
            Resource child1 = createChildPage(resourceManagement, root, "child-1");
            Resource child2 = createChildPage(resourceManagement, root, "child-2");
            Resource child11 = createChildPage(resourceManagement, child1, "child-11");
            Resource child12 = createChildPage(resourceManagement, child1, "child-12");
            Resource child13 = createChildPage(resourceManagement, child1, "child-13");
            Resource child111 = createChildPage(resourceManagement, child11, "child-111");
            Resource child112 = createChildPage(resourceManagement, child11, "child-112");
            Resource child121 = createChildPage(resourceManagement, child12, "child-121");
            Resource child21 = createChildPage(resourceManagement, child2, "child-21");
            Resource child22 = createChildPage(resourceManagement, child2, "child-22");
            Resource[] tree = new Resource[]{
                child112, child12, child121, child13, child2, child21, child22
            };

            // Now obtain an intermediate page and start traversing from there
            PerPage page = child111.adaptTo(PerPage.class);
            for(int i = 0; i < tree.length; i++) {
                String name = page.getName();
                page = page.getNext();
                assertEquals("Wrong Page on Next of " + name, tree[i].getName(), page.getName());
            }
            page = page.getNext();
            assertNull("At least one more page found", page);
        } finally {
            if(resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    /** Test the backwards traversing of a Page Tree **/
    @Test
    public void testBackwardsPageTraversing() throws Exception {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = getResourceResolver();
            // Create a Page Structure to be traversed in the test
            AdminResourceHandler resourceManagement = teleporter.getService(AdminResourceHandler.class);
            String testFolderPath = ROOT_PATH + "/test-bpt";
            Resource testFolder = createFolderStructure(resourceResolver, resourceManagement, testFolderPath);
            String rootPageName = "root";
            Resource root = createChildPage(resourceManagement, testFolder, rootPageName);
            Resource l1 = createChildPage(resourceManagement, testFolder, "l-1");
            Resource r1 = createChildPage(resourceManagement, testFolder, "r-1");
            logger.info("Root: '{}'", root == null ? "null" : root.getPath());
            // Create child structure
            Resource child1 = createChildPage(resourceManagement, root, "child-1");
            Resource child2 = createChildPage(resourceManagement, root, "child-2");
            Resource child11 = createChildPage(resourceManagement, child1, "child-11");
            Resource child12 = createChildPage(resourceManagement, child1, "child-12");
            Resource child13 = createChildPage(resourceManagement, child1, "child-13");
            Resource child111 = createChildPage(resourceManagement, child11, "child-111");
            Resource child112 = createChildPage(resourceManagement, child11, "child-112");
            Resource child121 = createChildPage(resourceManagement, child12, "child-121");
            Resource child21 = createChildPage(resourceManagement, child2, "child-21");
            Resource child22 = createChildPage(resourceManagement, child2, "child-22");
            Resource[] tree = new Resource[] {
                child2, child22, child21, child1, child13, child12, child121, child11, child112, child111
            };

            // Now traverse and check the correct folder
            PerPage page = root.adaptTo(PerPage.class);
            String name = null;
            for(int i = 0; i < tree.length; i++) {
                name = page.getName();
                page = page.getPrevious();
                logger.info("Before Page: '{}', Previous Page: '{}'", name, page.getName());
                assertEquals("Wrong Page on Previous of " + name, tree[i].getName(), page.getName());
            }
            name = page.getName();
            page = page.getPrevious();
            logger.info("(LAST) Before Page: '{}', Previous Page: '{}'", name, page == null ? "null" : page.getName());
            assertNull("At least one page found before first (before: " + name + ")", page);
        } finally {
            if(resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    /** Test the backwards traversing of a Page Tree **/
    @Test
    public void testBackwardsIntermediatePageTraversing() throws Exception {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = getResourceResolver();
            // Create a Page Structure to be traversed in the test
            AdminResourceHandler resourceManagement = teleporter.getService(AdminResourceHandler.class);
            String testFolderPath = ROOT_PATH + "/test-bipt";
            Resource testFolder = createFolderStructure(resourceResolver, resourceManagement, testFolderPath);
            String rootPageName = "root";
            Resource root = createChildPage(resourceManagement, testFolder, rootPageName);
            Resource l1 = createChildPage(resourceManagement, testFolder, "l-1");
            Resource r1 = createChildPage(resourceManagement, testFolder, "r-1");
            logger.info("Root: '{}'", root == null ? "null" : root.getPath());
            // Create child structure
            Resource child1 = createChildPage(resourceManagement, root, "child-1");
            Resource child2 = createChildPage(resourceManagement, root, "child-2");
            Resource child11 = createChildPage(resourceManagement, child1, "child-11");
            Resource child12 = createChildPage(resourceManagement, child1, "child-12");
            Resource child13 = createChildPage(resourceManagement, child1, "child-13");
            Resource child111 = createChildPage(resourceManagement, child11, "child-111");
            Resource child112 = createChildPage(resourceManagement, child11, "child-112");
            Resource child121 = createChildPage(resourceManagement, child12, "child-121");
            Resource child21 = createChildPage(resourceManagement, child2, "child-21");
            Resource child22 = createChildPage(resourceManagement, child2, "child-22");
            Resource[] tree = new Resource[] {
                child1, child13, child12, child121, child11, child112, child111
            };

            // Now traverse and check the correct folder
            PerPage page = child21.adaptTo(PerPage.class);
            String name = null;
            for(int i = 0; i < tree.length; i++) {
                name = page.getName();
                page = page.getPrevious();
                logger.info("Before Page: '{}', Previous Page: '{}'", name, page.getName());
                assertEquals("Wrong Page on Previous of " + name, tree[i].getName(), page.getName());
            }
            name = page.getName();
            page = page.getPrevious();
            logger.info("(LAST) Before Page: '{}', Previous Page: '{}'", name, page == null ? "null" : page.getName());
            assertNull("At least one page found before first (before: " + name + ")", page);
        } finally {
            if(resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    private ResourceResolver getResourceResolver() throws LoginException {
        final ResourceResolverFactory resourceResolverFactory = teleporter.getService(ResourceResolverFactory.class);
        logger.info("Got Resource Resolver Factory: '{}'", resourceResolverFactory);
        assertNotNull("Teleporter should provide a Resource Resolver Factory", resourceResolverFactory);
        ResourceResolver resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
        logger.info("Got Resource Resolver: '{}'", resourceResolver);
        assertNotNull("Wasn't able to create an Admin Resource Resolver", resourceResolver);
        return resourceResolver;
    }

    private Resource createChildPage(AdminResourceHandler resourceManagement, Resource parent, String childPageName) throws ManagementException, IOException {
        Resource newPage = resourceManagement.createPage(
            parent.getResourceResolver(), parent.getPath(), childPageName, EXAMPLE_TEMPLATE_PATH
        );
        logger.info("New Page: '{}'", newPage == null ? "null" : newPage.getPath());
        assertNotNull("No Page was created", newPage);
        testPage(newPage, childPageName);
        return newPage;
    }

    private void testPage(Resource pageResource, String pageName) throws IOException {
        Map<String, Object> expected = new LinkedHashMap<String, Object>();
        expected.put(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        Map<String, Object> expectedJcrContent = new LinkedHashMap<String, Object>();
        expected.put(JCR_CONTENT, expectedJcrContent);
        expectedJcrContent.put(JCR_PRIMARY_TYPE, PAGE_CONTENT_TYPE);
        expectedJcrContent.put(SLING_RESOURCE_TYPE, EXAMPLE_PAGE_TYPE_PATH);
        expectedJcrContent.put(JCR_TITLE, pageName);
        expectedJcrContent.put(TEMPLATE, EXAMPLE_TEMPLATE_PATH);
        checkResource(pageResource, expected);
    }

    private void checkResource(Resource resource, Map<String, Object> expected) {
        for(Entry<String, Object> entry: expected.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof Map) {
                // This is a child -> check if the child resource exist and then compare this one
                Resource child = getResource(resource, name);
                assertNotNull("Child Resource with Nama: '" + name + "' not found", child);
                checkResource(child, (Map<String, Object>) value);
            } else {
                assertEquals("Property: '" + name + "' did not match", resource.getValueMap().get(name), value);
            }
        }
    }

    private Resource createFolderStructure(ResourceResolver resourceResolver, AdminResourceHandler resourceManagement, String folderPath) throws ManagementException {
        String[] folders = folderPath.split("/");
        String path = "/";
        Resource parent = getResource(resourceResolver, path);
        for(String folder: folders) {
            Resource folderResource = parent.getChild(folder);
            if(folderResource == null) {
                folderResource = resourceManagement.createFolder(resourceResolver, parent.getPath(), folder);
                assertNotNull("Folder: '" + folder + "' could not be in: '" + parent.getPath() + "'", folderResource);
            }
            parent = folderResource;
        }
        return parent;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
