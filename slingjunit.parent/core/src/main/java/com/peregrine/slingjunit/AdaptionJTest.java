package com.peregrine.slingjunit;

import com.peregrine.adaption.PerAsset;
import com.peregrine.adaption.PerPage;
import com.peregrine.nodetypes.merge.PageMerge;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.apache.sling.servlethelpers.MockSlingHttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.script.SimpleBindings;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * Run test using this URL
 * http://localhost:8080/system/sling/junit/com.peregrine.slingjunit.AdaptionJTest.html
 *
 */
@RunWith(SlingAnnotationsTestRunner.class)
public class AdaptionJTest {
    private static String CONTACT_PAGE_PATH = "/content/example/pages/contact";
    private static String ASSET_EXAMPLE = "/content/example/assets/images/logo.png";
    private static String OBJECT_EXAMPLE = "/content/example/objects/sample";
    private static String TEMPLATE_EXAMPLE = "/content/example/templates/base";

    private MockSlingHttpServletRequest request;
    private Resource resource;
    private SimpleBindings bindings;
    @TestReference
    private ResourceResolverFactory resolverFactory;
    private ResourceResolver resourceResolver;

    @TestReference
    private ModelFactory modelFactory;

    @Test
    public void getSiteFromPage(){
        assertNotNull(resourceResolver);
        resource = resourceResolver.getResource(CONTACT_PAGE_PATH);
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

    /**
     * Server Side Rendering SSR Page Merge Scenario 1
     *
     * Template has structural components (nav and footer) no containers
     * Page has content container with children
     *
     * Template: /content/example/templates/base/
     *      _jcr_content/nav
     *      _jcr_content/footer
     *
     * Page: /content/example/pages/contact/
     *      _jcr_content/nfceefd40-b802-4203-8147-9cb2b2c78c6b
     *      _jcr_content/n3736dc36-9cc3-49d7-a7d4-bf4d94e0ea2f
     *          n8680c077-cc22-40d3-8989-d86d832a85d1
     *
     * Resulting Page Merge
     *  /content/example/templates/base/_jcr_content/nav
     *  /content/example/templates/base/_jcr_content/footer
     *  /content/example/pages/contact/_jcr_content/nfceefd40-b802-4203-8147-9cb2b2c78c6b
     *  /content/example/pages/contact/_jcr_content/n3736dc36-9cc3-49d7-a7d4-bf4d94e0ea2f
     *  /content/example/pages/contact/_jcr_content/n3736dc36-9cc3-49d7-a7d4-bf4d94e0ea2f/n8680c077-cc22-40d3-8989-d86d832a85d1
     *
     */
    @Test
    public void ssrPageMergeScenario1(){
        resource = resourceResolver.getResource(CONTACT_PAGE_PATH);
        List<String> resourcePaths = new ArrayList<>();
        resourcePaths.add("/content/example/templates/base/jcr:content/nav");
        resourcePaths.add("/content/example/templates/base/jcr:content/footer");
        resourcePaths.add("/content/example/pages/contact/jcr:content/nfceefd40-b802-4203-8147-9cb2b2c78c6b");
        resourcePaths.add("/content/example/pages/contact/jcr:content/n3736dc36-9cc3-49d7-a7d4-bf4d94e0ea2f");
// Note: children of containers are not included in the resource list.
// /content/example/pages/contact/jcr:content/n3736dc36-9cc3-49d7-a7d4-bf4d94e0ea2f/n8680c077-cc22-40d3-8989-d86d832a85d1
// resources inside containers are rendered when the container is rendered, when it lists all children
        bindings.put("resource", resource);
        PageMerge pageMerge = new PageMerge();
        pageMerge.init(bindings);
        List<Resource> actualResources = pageMerge.getMergedResources();
        assertEquals(resourcePaths.size(), actualResources.size());
        List<String> actualResourcePaths = actualResources.stream()
            .map(Resource::getPath)
            .collect(Collectors.toList());
        assertTrue(actualResourcePaths.containsAll(resourcePaths));
        assertTrue(resourcePaths.containsAll(actualResourcePaths));
    }

    /**
     ssrPageMergeScenario2 test pages based on a sub-template
     sub-template: /content/example/templates/base/sub-template
     page:
     */
    @Test
    public void ssrPageMergeScenario2(){
        resource = resourceResolver.getResource("/content/example/pages/sub-template-page");
        // set up expected resource list
        List<String> resourcePaths = new ArrayList<>();
        resourcePaths.add("/content/example/templates/base/jcr:content/nav");
        resourcePaths.add("/content/example/templates/base/jcr:content/footer");
        resourcePaths.add("/content/example/pages/sub-template-page/jcr:content/container");
        bindings.put("resource", resource);
        PageMerge pageMerge = new PageMerge();
        pageMerge.init(bindings);
        // run test
        List<Resource> actualResources = pageMerge.getMergedResources();
        assertEquals(resourcePaths.size(), actualResources.size());
        List<String> actualResourcePaths = actualResources.stream()
                .map(Resource::getPath)
                .collect(Collectors.toList());
        assertTrue(actualResourcePaths.containsAll(resourcePaths));
        assertTrue(resourcePaths.containsAll(actualResourcePaths));
    }


    @Before
    public void setUp() throws Exception {
        resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
        request = new MockSlingHttpServletRequest(resourceResolver);
        bindings = new SimpleBindings();
        bindings.put("request", request);
        bindings.put("modelFactory", modelFactory);
        bindings.put("resolver", resourceResolver);
    }

    @After
    public void cleanup() {
        resource = null;
        resourceResolver.close();
    }
}
