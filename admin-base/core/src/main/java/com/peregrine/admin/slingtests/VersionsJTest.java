package com.peregrine.admin.slingtests;

import com.peregrine.admin.models.PageModel;
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
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
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
    public static final String EXAMPLE_SITE_ROOT = "/content/example/";
    public static final String EXAMPLE_PAGE = "pages/index";
    public static final String EXAMPLE_ASSET = "assets/images/peregrine-logo.png";
    private Resource testPageRes;
    private PageModel testPage;
    private Resource testAssetRes;
    private VersionManager versionManager;

    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
        // get a per:Page
        testPageRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+EXAMPLE_PAGE);
        testPage = testPageRes.adaptTo(PageModel.class);
        // get a per:Asset (just a resource?)
        testAssetRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+EXAMPLE_ASSET);
    }

    @After
    public void cleanUp() {
        Node pageNode = testPageRes.adaptTo(Node.class);
        Node assetNode = testAssetRes.adaptTo(Node.class);
        VersionManager vmPage;
        VersionManager vmAsset;
        try {
//            Clean Up Test Page Versions
            vmPage = pageNode.getSession().getWorkspace().getVersionManager();
            VersionHistory vhPage = vmPage.getVersionHistory(testPage.getPath());
            Version rootVersion = vhPage.getRootVersion();
            VersionIterator vi = vhPage.getAllVersions();
            // remove all versions except for root version which is protected and cannot be removed
            while (vi.hasNext()) {
                Version v = vi.nextVersion();
                if (!rootVersion.getPath().equals(v.getPath())) {
                    v.remove();
                }
            }
//            Clean Up Test Asset Versions
            vmAsset = assetNode.getSession().getWorkspace().getVersionManager();
            VersionHistory vhAsset = vmAsset.getVersionHistory(testAssetRes.getPath());
            Version rootVersionAsset = vhAsset.getRootVersion();
            while (vi.hasNext()) {
                Version v = vi.nextVersion();
                if (!rootVersionAsset.getPath().equals(v.getPath())) {
                    v.remove();
                }
            }
        } catch (RepositoryException e) {
            logger.warn("test resources were not versionable");
        } finally {
            resourceResolver.close();
        }
    }

    @Test
    public void resourcesNotNull() {
        assertNotNull(resolverFactory);
        assertNotNull(resourceResolver);
        assertNotNull(resourceManagement);
        assertNotNull(testPageRes);
        assertNotNull(testAssetRes);
        assertNotNull(testPage);
    }

    @Test
    public void pageCanBeVersionable() {
        Node pageNode = testPageRes.adaptTo(Node.class);
        try {
            VersionManager vm = pageNode.getSession().getWorkspace().getVersionManager();
            if (vm.getVersionHistory(testPageRes.getPath()) == null) {
                assertTrue(pageNode.canAddMixin("mix:versionable"));
                return;
            } else {
                NodeType[] nodeTypes = pageNode.getMixinNodeTypes();
                for (NodeType nt : nodeTypes) {
                    if (nt.isNodeType("mix:versionable")){
                        return;
                    }
                }
                fail("no type mix:versionable");
            }
        } catch (RepositoryException e) {
            logger.error("RepositoryException", e);
        }
        fail("page could not be versioned");
    }

    @Test
    public void assetResourceCanBeVersionable() {
        Node assetNode = testAssetRes.adaptTo(Node.class);
        try {
            assertTrue(assetNode.canAddMixin("mix:versionable"));
        } catch (RepositoryException e) {
            fail("no type mix:versionable");
        }
    }

    @Test
    public void makeNewPageVersion() {
        try {
            Version version = resourceManagement.createVersion(this.resourceResolver,EXAMPLE_SITE_ROOT+EXAMPLE_PAGE);
            assertNotNull( version);
            String versionPath = version.getFrozenNode().getPath();
            logger.info("Created version {}", versionPath);
            resourceResolver.commit();
        } catch (Exception e) {
            fail("could not create version");
        }
    }
}
