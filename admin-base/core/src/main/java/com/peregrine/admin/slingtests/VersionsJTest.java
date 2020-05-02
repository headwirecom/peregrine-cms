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
    static final String EXAMPLE_PAGE = "pages/index";
    static final String EXAMPLE_DELETE = "pages/about";
    static final String EXAMPLE_ASSET = "assets/images/peregrine-logo.png";

    // page objects
    private Resource testPageRes;
    private PageModel testPage;
    private Node pageNode;
    private Resource deletedRes;
    private Node deletedNode;
    // asset objects
    private Resource testAssetRes;
    private Node assetNode;
    // version managers
    private VersionManager vmPage;
    private VersionManager vmAsset;

    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
        jcrSession = resourceResolver.adaptTo(Session.class);
        // get a per:Page
        testPageRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+EXAMPLE_PAGE);
        testPage = testPageRes.adaptTo(PageModel.class);
        pageNode = testPageRes.adaptTo(Node.class);
        // get a per:Asset (just a resource?)
        testAssetRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+EXAMPLE_ASSET);
        assetNode = testAssetRes.adaptTo(Node.class);
        // version managers
        vmPage = pageNode.getSession().getWorkspace().getVersionManager();
        vmAsset = assetNode.getSession().getWorkspace().getVersionManager();

        deletedRes = resourceResolver.resolve(EXAMPLE_SITE_ROOT+EXAMPLE_DELETE);
        deletedNode = deletedRes.adaptTo(Node.class);
    }

    @Test
    public void resourcesNotNull() {
        assertNotNull(resolverFactory);
        assertNotNull(resourceResolver);
        assertNotNull(jcrSession);
        assertNotNull(resourceManagement);
        assertNotNull(testPageRes);
        assertNotNull(testAssetRes);
        assertNotNull(testPage);
    }

    @Test
    public void pageCanBeVersionable() {
        try {
            if (pageNode.canAddMixin("mix:versionable")) {
                assertTrue(pageNode.canAddMixin("mix:versionable"));
                logger.info("page can become versionable but currently is not");
                return;
            }
            if (vmPage.getVersionHistory(testPageRes.getPath()) == null) {
                logger.info("page is versionable and has a version history");
                return;
            } else {
                NodeType[] nodeTypes = pageNode.getMixinNodeTypes();
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
            Version version = resourceManagement.createVersion(this.resourceResolver, testPageRes.getPath());
            assertNotNull(version);
            VersionHistory vhPage = vmPage.getVersionHistory(pageNode.getPath());
            // check that the number of versions is 2 (root and one version)
            int size = Iterators.size(vhPage.getAllLinearVersions());
            assertEquals(2, size);
            Version rootVersion = vhPage.getRootVersion();
            Version firstVersion = rootVersion.getLinearSuccessor();
            assertEquals("1.0", firstVersion.getName());
            assertEquals(firstVersion.getName(), version.getName());
            assertTrue(vmPage.isCheckedOut(testPage.getPath()));
        } catch (Exception e) {
            fail("could not create version");
        }
    }

    @Test
    public void make2ndPageVersion() {
        try {
//            First Version
            Version version = resourceManagement.createVersion(this.resourceResolver, testPageRes.getPath());
            assertNotNull(version);
            assertTrue(vmPage.isCheckedOut(testPage.getPath()));
//            Second Version
            Version version2 = resourceManagement.createVersion(this.resourceResolver, testPageRes.getPath());
            assertNotNull(version2);
            // check that the number of versions is 2 (root and two versions)
            VersionHistory vhPage2 = vmPage.getVersionHistory(pageNode.getPath());
            int size3 = Iterators.size(vhPage2.getAllLinearVersions());
            assertEquals(3, size3);
            assertTrue(vmPage.isCheckedOut(testPage.getPath()));
        } catch (Exception e) {
            fail("could not create version");
        }
    }

    @Test
    public void restoreFirstPageVersion() {
        try {
            // First Version
            Version version = resourceManagement.createVersion(this.resourceResolver, testPageRes.getPath());
            // Second Version
            Version version2 = resourceManagement.createVersion(this.resourceResolver, testPageRes.getPath());
            assertNotNull(version2);
            // check that the current version has a name = 1.1
            assertEquals("1.1" , vmPage.getBaseVersion(testPage.getPath()).getName());
            assertTrue(vmPage.isCheckedOut(testPage.getPath()));
            // Restore the first version
            String frozenNodepath = version.getPath();
            Node frozenNode = jcrSession.getNode(frozenNodepath);
            Version versionToRestore = (Version) frozenNode;
            assertNotNull(versionToRestore);
            Resource restoredResource = resourceManagement.restoreVersion(resourceResolver, testPage.getPath(), frozenNodepath, true);
            assertNotNull(restoredResource);
            assertEquals("1.0" , vmPage.getBaseVersion(restoredResource.getPath()).getName());
            // check that the test page is not left in a "checked out" or locked state
            assertTrue(vmPage.isCheckedOut(testPage.getPath()));
        } catch (Exception e) {
            fail("could not create version");
        }
    }

    @Test
    public void resolveSiteHome() {
        // get home from a page
        String home = resourceManagement.getSiteHomePath(resourceResolver, testPageRes);
        assertNotNull(home);
        assertEquals("/content/example", home);
        // get home from an asset
        home = resourceManagement.getSiteHomePath(resourceResolver, testAssetRes);
        assertNotNull(home);
        assertEquals("/content/example", home);
    }

    @Test
    public void createRecyclable(){
        try {
            Recyclable recyclable = resourceManagement.createRecyclable(resourceResolver, testPageRes);
            assertNotNull(recyclable);
            assertEquals(testPageRes.getPath(), recyclable.getResourcePath());
            assertTrue(recyclable.getFrozenNodePath().startsWith("/jcr:system/jcr:versionStorage/"));
            assertEquals("/content/example/recyclebin/content/example/pages/index", recyclable.getResource().getPath());
        } catch (AdminResourceHandler.ManagementException e) {
            fail("execption while creating recyclable");
        }
    }

    @Test
    public void findAndRestoreRecyclable (){
        try {
            Recyclable recyclable = resourceManagement.createRecyclable(resourceResolver, deletedRes);
            assertNotNull(recyclable);
            // Delete the page
            resourceResolver.delete(deletedRes);
            resourceResolver.commit();
            deletedRes = resourceResolver.getResource(recyclable.getResourcePath());
            assertNull(deletedRes);
            resourceManagement.recycleDeleted(resourceResolver, recyclable, true );
            resourceResolver.refresh();
            resourceResolver.commit();
        } catch (Exception e) {
            fail("could not find and restore recyclable");
        }
    }

    @Test
    public void restoreDeletedPage() {
        try {
            // Create a Version
            Version version = resourceManagement.createVersion(this.resourceResolver, deletedNode.getPath());
            VersionHistory vhPage = vmPage.getVersionHistory(deletedNode.getPath());
            assertEquals(2, Iterators.size(vhPage.getAllLinearVersions()));
            assertNotNull(version);
            // Record paths to restore
            String versionPath = version.getPath();
            String resourcePath = deletedNode.getPath();
            // Delete the page
            resourceResolver.delete(deletedRes);
            deletedRes = resourceResolver.getResource(resourcePath);
            assertNull(deletedRes);
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
            deletedRes = restoredResource;
            deletedNode = restoredResource.adaptTo(Node.class);

        } catch (Exception e) {
            fail("could not restore deleted");
        }
    }

    @After
    public void cleanUp() {
        resourceResolver.refresh();
        try {
//            Clean Up Test Page Versions
            for (NodeType nt : pageNode.getMixinNodeTypes()){
                if (nt.isNodeType("mix:versionable")) {
                    vmPage.checkout(testPage.getPath());
                    pageNode.removeMixin("mix:versionable");
                    resourceResolver.commit();
                }
            }

            for (NodeType nt : deletedNode.getMixinNodeTypes()){
                if (nt.isNodeType("mix:versionable")) {
                    vmPage.checkout(deletedNode.getPath());
                    deletedNode.removeMixin("mix:versionable");
                    resourceResolver.commit();
                }
            }
            Node exampleRecycling = JcrUtils.getNodeIfExists(RECYCLE_BIN+"/content/example", jcrSession);
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
        }
    }
}
