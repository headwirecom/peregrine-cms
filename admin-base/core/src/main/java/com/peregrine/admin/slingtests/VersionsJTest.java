package com.peregrine.admin.slingtests;

import com.google.common.collect.Iterators;
import com.peregrine.admin.models.PageModel;
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
import static org.junit.Assert.*;

@RunWith(SlingAnnotationsTestRunner.class)
public class VersionsJTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @TestReference
    private ResourceResolverFactory resolverFactory;

    @TestReference
    AdminResourceHandler resourceManagement;

    private ResourceResolver resourceResolver;
    private Session jcrSession;
    public static final String EXAMPLE_SITE_ROOT = "/content/example/";
    public static final String EXAMPLE_PAGE = "pages/index";
    public static final String EXAMPLE_ASSET = "assets/images/peregrine-logo.png";
    // page objects
    private Resource testPageRes;
    private PageModel testPage;
    private Node pageNode;
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
            VersionHistory vhPage = vmPage.getVersionHistory(pageNode.getPath());
            int size2 = Iterators.size(vhPage.getAllLinearVersions());
            assertEquals(2, size2);
            Version rootVersion = vhPage.getRootVersion();
            Version firstVersion = rootVersion.getLinearSuccessor();
            assertEquals("1.0", firstVersion.getName());
            assertEquals(firstVersion.getName(), version.getName());
            assertTrue(vmPage.isCheckedOut(testPage.getPath()));
//            Second Version
            Version version2 = resourceManagement.createVersion(this.resourceResolver, testPageRes.getPath());
            assertNotNull(version2);
            VersionHistory vhPage2 = vmPage.getVersionHistory(pageNode.getPath());
            int size3 = Iterators.size(vhPage.getAllLinearVersions());
            assertEquals(3, size3);
            rootVersion = vhPage.getRootVersion();
            firstVersion = rootVersion.getLinearSuccessor();
            assertEquals("1.0", firstVersion.getName());
            assertEquals(firstVersion.getName(), version.getName());
            assertTrue(vmPage.isCheckedOut(testPage.getPath()));
        } catch (Exception e) {
            fail("could not create version");
        }
    }

    @Test
    public void restoreFirstPageVersion() {
        try {
//      First Version
            Version version = resourceManagement.createVersion(this.resourceResolver, testPageRes.getPath());
            assertNotNull(version);
            VersionHistory vhPage = vmPage.getVersionHistory(pageNode.getPath());
            int size2 = Iterators.size(vhPage.getAllLinearVersions());
            assertEquals(2, size2);
            Version rootVersion = vhPage.getRootVersion();
            Version firstVersion = rootVersion.getLinearSuccessor();
            assertEquals("1.0", firstVersion.getName());
            assertEquals(firstVersion.getName(), version.getName());
//      Second Version
            Version version2 = resourceManagement.createVersion(this.resourceResolver, testPageRes.getPath());
            assertNotNull(version2);
            // check that the number of versions is 3 (root and two others)
            int size3 = Iterators.size(vhPage.getAllLinearVersions());
            assertEquals(3, size3);
            rootVersion = vhPage.getRootVersion();
            firstVersion = rootVersion.getLinearSuccessor();
            // check that the first version has a name = 1.0
            assertEquals("1.0", firstVersion.getName());
            assertEquals(firstVersion.getName(), version.getName());
            // check that the current version has a name = 1.1
            assertEquals("1.1" , vmPage.getBaseVersion(testPage.getPath()).getName());
            assertTrue(vmPage.isCheckedOut(testPage.getPath()));

//      Restore the first version
            String frozenNodepath = firstVersion.getPath();
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

    @After
    public void cleanUp() {
        try {
//            Clean Up Test Page Versions
            for (NodeType nt : pageNode.getMixinNodeTypes()){
                if (nt.isNodeType("mix:versionable")) {
                    vmPage.checkout(testPage.getPath());
                    pageNode.removeMixin("mix:versionable");
                    resourceResolver.commit();
                }
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
