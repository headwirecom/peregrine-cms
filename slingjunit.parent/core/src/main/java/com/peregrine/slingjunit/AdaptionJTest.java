package com.peregrine.slingjunit;

import com.peregrine.adaption.PerAsset;
import com.peregrine.adaption.PerPage;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;


@RunWith(SlingAnnotationsTestRunner.class)
public class AdaptionJTest {
    private static String PAGE_EXAMPLE = "/content/example/pages/contact";
    private static String ASSET_EXAMPLE = "/content/example/assets/images/logo.png";
    private static String OBJECT_EXAMPLE = "/content/example/objects/sample";
    private static String TEMPLATE_EXAMPLE = "/content/example/templates/base";


    private Resource resource;
    @TestReference
    private ResourceResolverFactory resolverFactory;
    private ResourceResolver resourceResolver;


    @Test
    public void getSiteFromPage(){
        assertNotNull(resourceResolver);
        resource = resourceResolver.getResource(PAGE_EXAMPLE);
        assertFalse( ResourceUtil.isNonExistingResource(resource));
        PerPage page = resource.adaptTo(PerPage.class);
        assertNotNull(page);
        assertSiteResource(page.getSiteResource());
    }

    @Test
    public void getSiteFromAsset(){
        resource = resourceResolver.getResource(ASSET_EXAMPLE);
        assertFalse( ResourceUtil.isNonExistingResource(resource));
        PerAsset asset = resource.adaptTo(PerAsset.class);
        assertNotNull(asset);
        assertSiteResource(asset.getSiteResource());
    }

    private void assertSiteResource(Resource tenantRes){
        assertNotNull(tenantRes);
        Node tenantNode = tenantRes.adaptTo(Node.class);
        try {
            NodeType nt = tenantNode.getPrimaryNodeType();
            assertEquals("per:Site", nt.getName());
        } catch (RepositoryException e) {
            fail("Could not get tenant nodetype");
        }
    }

    @Test
    public void getSiteFromTemplate(){
        assertNotNull(resourceResolver);
        resource = resourceResolver.getResource(TEMPLATE_EXAMPLE);
        assertFalse( ResourceUtil.isNonExistingResource(resource));
        PerPage template = resource.adaptTo(PerPage.class);
        assertNotNull(template);
        assertSiteResource(template.getSiteResource());
    }

//    TODO: Object needs Adaption implemented
//    @Test
//    public void getSiteFromObject(){
//    }

    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
    }

    @After
    public void cleanup() {
        resource = null;
        resourceResolver.close();
    }
}
