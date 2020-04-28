package com.peregrine.slingtests;


import com.peregrine.admin.models.PageModel;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.peregrine.admin.resource.AdminResourceHandler;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionManager;

import static org.junit.Assert.*;


@RunWith(SlingAnnotationsTestRunner.class)
public class VersionsJTest {

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
// get a per:Asset
        testAssetRes = resourceResolver.getResource(EXAMPLE_SITE_ROOT+EXAMPLE_ASSET);
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
            assertTrue(pageNode.canAddMixin("mix:versionable"));
        } catch (RepositoryException e) {
            fail("no type mix:versionable");
        }
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
}
