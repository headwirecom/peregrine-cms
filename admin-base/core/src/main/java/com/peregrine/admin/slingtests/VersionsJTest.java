package com.peregrine.admin.slingtests;

import com.google.common.collect.Iterators;
import com.peregrine.admin.models.PageModel;
import com.peregrine.admin.models.Recyclable;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.peregrine.admin.resource.AdminResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import static com.peregrine.commons.util.PerConstants.RECYCLE_BIN;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

@RunWith(SlingAnnotationsTestRunner.class)
public class VersionsJTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @TestReference
    private ResourceResolverFactory resolverFactory;

    @TestReference
    AdminResourceHandler resourceManagement;

    private ResourceResolver resourceResolver;
    private Session jcrSession;

    static final String EXAMPLE_SITE_ROOT = "/content/example/";
    static final String SITE_RECYCLINGBIN = EXAMPLE_SITE_ROOT + "recyclebin";
    static final String EXAMPLE_PAGES = EXAMPLE_SITE_ROOT +"pages";
    static final String EXAMPLE_INDEX = "pages/index";
    static final String EXAMPLE_ABOUT = "pages/about";
    static final String EXAMPLE_ASSET = "assets/images/peregrine-logo.png";
    static final String [] EXAMPLE_PAGE_PATHS = { "/index", "/about", "/services",
            "/contact", "/services/jcr:content/content/row/col1/text2" };

    // page objects
    private Resource indexRes;
    private PageModel indexPage;
    private Node indexNode;
    private Resource aboutRes;
    private Node aboutNode;
    private Resource pagesRes;
    private Node pagesNode;
    // asset objects
    private Resource assetRes;
    private Node assetNode;
    // version managers
    private VersionManager vmPage;
    private VersionManager vmAsset;

    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
        jcrSession = resourceResolver.adaptTo(Session.class);
        // get a per:Page
        indexRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+ EXAMPLE_INDEX);
        indexPage = indexRes.adaptTo(PageModel.class);
        indexNode = indexRes.adaptTo(Node.class);
        // get a per:Asset (just a resource?)
        assetRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+EXAMPLE_ASSET);
        assetNode = assetRes.adaptTo(Node.class);
        // version managers
        vmPage = indexNode.getSession().getWorkspace().getVersionManager();
        vmAsset = assetNode.getSession().getWorkspace().getVersionManager();

        aboutRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+ EXAMPLE_ABOUT);
        aboutNode = aboutRes.adaptTo(Node.class);

        pagesRes = resourceResolver.getResource(EXAMPLE_PAGES);
        pagesNode = pagesRes.adaptTo(Node.class);
    }

    @Test
    public void resourcesNotNull() {
        assertNotNull(resolverFactory);
        assertNotNull(resourceResolver);
        assertNotNull(jcrSession);
        assertNotNull(resourceManagement);
        assertNotNull(indexRes);
        assertNotNull(assetRes);
        assertNotNull(indexPage);
        assertNotNull(pagesRes);
        assertNotNull(pagesNode);
    }

    @Test
    public void pageCanBeVersionable() {
        try {
            if (indexNode.canAddMixin("mix:versionable")) {
                assertTrue(indexNode.canAddMixin("mix:versionable"));
                logger.info("page can become versionable but currently is not");
                return;
            }
            if (vmPage.getVersionHistory(indexRes.getPath()) == null) {
                logger.info("page is versionable and has a version history");
                return;
            } else {
                NodeType[] nodeTypes = indexNode.getMixinNodeTypes();
                for (NodeType nt : nodeTypes) {
                    if (nt.isNodeType("mix:versionable")){
                        logger.info("page is versionable but has no history");
                        return;
                    }
                }
                fail("not versionable; mixin type mix:versionable cant be added");
            }
        } catch (RepositoryException e) {
            logger.error("RepositoryException", e);
        }
        fail("page is not versionable");
    }

    @Test
    public void assetResourceCanBeVersionable() {
        try {
            assertTrue(assetNode.canAddMixin("mix:versionable"));
        } catch (RepositoryException e) {
            fail("no type mix:versionable");
        }
    }

    @Test
    public void makeFirstPageVersion() {
        try {
            Version version = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            assertNotNull(version);
            VersionHistory vhPage = vmPage.getVersionHistory(indexNode.getPath());
            // check that the number of versions is 2 (root and one version)
            int size = Iterators.size(vhPage.getAllLinearVersions());
            assertEquals(2, size);
            Version rootVersion = vhPage.getRootVersion();
            Version firstVersion = rootVersion.getLinearSuccessor();
            assertEquals("1.0", firstVersion.getName());
            assertEquals(firstVersion.getName(), version.getName());
            assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
        } catch (Exception e) {
            fail("could not create version");
        }
    }

    @Test
    public void make2ndPageVersion() {
        try {
//            First Version
            Version version = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            assertNotNull(version);
            assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
//            Second Version
            Version version2 = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            assertNotNull(version2);
            // check that the number of versions is 2 (root and two versions)
            VersionHistory vhPage2 = vmPage.getVersionHistory(indexNode.getPath());
            int size3 = Iterators.size(vhPage2.getAllLinearVersions());
            assertEquals(3, size3);
            assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
        } catch (Exception e) {
            fail("could not create version");
        }
    }

    @Test
    public void restoreFirstPageVersion() {
        try {
            // First Version
            Version version = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            // Second Version
            Version version2 = resourceManagement.createVersion(this.resourceResolver, indexRes.getPath());
            assertNotNull(version2);
            // check that the current version has a name = 1.1
            assertEquals("1.1" , vmPage.getBaseVersion(indexPage.getPath()).getName());
            assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
            // Restore the first version
            String frozenNodepath = version.getPath();
            Node frozenNode = jcrSession.getNode(frozenNodepath);
            Version versionToRestore = (Version) frozenNode;
            assertNotNull(versionToRestore);
            Resource restoredResource = resourceManagement.restoreVersion(resourceResolver, indexPage.getPath(), frozenNodepath, true);
            assertNotNull(restoredResource);
            assertEquals("1.0" , vmPage.getBaseVersion(restoredResource.getPath()).getName());
            // check that the test page is not left in a "checked out" or locked state
            assertTrue(vmPage.isCheckedOut(indexPage.getPath()));
        } catch (Exception e) {
            fail("could not create version");
        }
    }

    @Test
    public void resolveSiteHome() {
        // get home from a page
        String home = resourceManagement.getSiteHomePath(resourceResolver, indexRes);
        assertNotNull(home);
        assertEquals("/content/example", home);
        // get home from an asset
        home = resourceManagement.getSiteHomePath(resourceResolver, assetRes);
        assertNotNull(home);
        assertEquals("/content/example", home);
    }

    @Test
    public void createRecyclable(){
        try {
            Recyclable recyclable = resourceManagement.createRecyclable(resourceResolver, indexRes);
            assertNotNull(recyclable);
            assertEquals(indexRes.getPath(), recyclable.getResourcePath());
            assertTrue(recyclable.getFrozenNodePath().startsWith("/jcr:system/jcr:versionStorage/"));
            assertEquals("/content/example/recyclebin/content/example/pages/index", recyclable.getResource().getPath());
        } catch (AdminResourceHandler.ManagementException e) {
            fail("execption while creating recyclable");
        }
    }

    @Test
    public void findAndRestoreRecyclable (){
        try {
            Recyclable recyclable = resourceManagement.createRecyclable(resourceResolver, aboutRes);
            assertNotNull(recyclable);
            // Delete the page
            resourceResolver.delete(aboutRes);
            resourceResolver.commit();
            aboutRes = resourceResolver.getResource(recyclable.getResourcePath());
            assertNull(aboutRes);
            resourceManagement.recycleDeleted(resourceResolver, recyclable, true );
            resourceResolver.refresh();
            resourceResolver.commit();
        } catch (Exception e) {
            fail("could not find and restore recyclable");
        }
    }

    @Test
    public void deleteAllPagesAndRestore () {
        assertNotNull(pagesRes);
        assertEquals(EXAMPLE_PAGES, pagesRes.getPath());
        try {
            Recyclable createdRecyclable = resourceManagement.createRecyclable(resourceResolver, pagesRes);
            assertNotNull(createdRecyclable);
            resourceResolver.delete(pagesRes);
            resourceResolver.refresh();
            resourceResolver.commit();
            // all gone
            for (String path : EXAMPLE_PAGE_PATHS) {
                assertNull(resourceResolver.getResource(EXAMPLE_PAGES + path));
            }

            Recyclable foundRecyclable = resourceManagement.getRecyclable(resourceResolver,
                    SITE_RECYCLINGBIN + EXAMPLE_PAGES);
            assertNotNull(foundRecyclable);
            resourceManagement.recycleDeleted(resourceResolver,foundRecyclable, false);
            // all back
            for (String path : EXAMPLE_PAGE_PATHS) {
                assertNotNull(resourceResolver.getResource(EXAMPLE_PAGES + path));
            }
        } catch (AdminResourceHandler.ManagementException e) {
            fail("Could create recyclable for pages: " + EXAMPLE_PAGES);
        } catch (PersistenceException e) {
            fail("Could delete resource: " + EXAMPLE_PAGES);
        }
    }

    @Test
    public void restoreDeletedPage() {
        try {
            // Create a Version
            Version version = resourceManagement.createVersion(this.resourceResolver, aboutNode.getPath());
            VersionHistory vhPage = vmPage.getVersionHistory(aboutNode.getPath());
            assertEquals(2, Iterators.size(vhPage.getAllLinearVersions()));
            assertNotNull(version);
            // Record paths to restore
            String versionPath = version.getPath();
            String resourcePath = aboutNode.getPath();
            // Delete the page
            resourceResolver.delete(aboutRes);
            aboutRes = resourceResolver.getResource(resourcePath);
            assertNull(aboutRes);
            resourceResolver.commit();
            Recyclable recyclable = new Recyclable();

            // Restore at the recorded version
            Resource restoredResource = resourceManagement.restoreDeleted(resourceResolver, resourcePath, versionPath, true);
            assertNotNull(restoredResource);
            assertNotNull(resourceResolver.getResource(resourcePath));
            assertEquals("1.0" , vmPage.getBaseVersion(restoredResource.getPath()).getName());
            // check that the test page is not left in a "checked out" or locked state
            assertTrue(vmPage.isCheckedOut(restoredResource.getPath()));
            // Clean up
            aboutRes = restoredResource;
            aboutNode = restoredResource.adaptTo(Node.class);

        } catch (Exception e) {
            fail("could not restore deleted");
        }
    }

    @After
    public void cleanUp() {
        resourceResolver.refresh();
        try {
//            Clean Up Test Nodes
            Node[] nodesToCleanUp = {indexNode, aboutNode, pagesNode};
            for (Node node : nodesToCleanUp) {
                for (NodeType nt : node.getMixinNodeTypes()){
                    if (nt.isNodeType("mix:versionable")) {
                        vmPage.checkout(node.getPath());
                        node.removeMixin("mix:versionable");
                    }
                }
            }
            resourceResolver.commit();

            Node exampleRecycling = JcrUtils.getNodeIfExists("/content/example/"+RECYCLE_BIN, jcrSession);
            if (exampleRecycling != null) {
                exampleRecycling.remove();
                jcrSession.save();
            }
        } catch (RepositoryException e) {
            logger.error("test resources were not versionable", e);
        } catch (PersistenceException e) {
            logger.error("Could not restore example", e);
        } finally {
            resourceResolver.close();
            resourceResolver = null;
            jcrSession = null;
        }
    }
}
