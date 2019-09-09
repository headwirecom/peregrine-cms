package com.peregrine.nodetypes.models;

import com.peregrine.commons.util.BindingsUseUtil;
import com.peregrine.nodetypes.merge.PageMerge;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.junit.Test;

import javax.script.Bindings;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractComponentTest {

    @Test
    public void testSimpleComponent() {
        Resource resource = mock(Resource.class);
        AbstractComponent component = new AbstractComponent(resource);
        String path = "/content/test/resource";
        when(resource.getPath()).thenReturn(path);
        String resourceType = "cms/resource/type";
        when(resource.getResourceType()).thenReturn(resourceType);

        Resource fromResource = component.getResource();
        assertNotNull("Missing Resource", fromResource);
        assertEquals("Wrong Resource returned", resource, fromResource);

        List<IComponent> experiences = component.getExperiences();
        assertNull("Experiences should not be defined", experiences);

        List<IComponent> children = component.getChildren();
        assertNotNull("Children should be a list", children);
        assertTrue("Children must be empty", children.isEmpty());

        assertEquals("Wrong Path", path, component.getPath());

        String componentComponent = component.getComponent();
        assertNotNull("Component of Component must be defined", componentComponent);
        assertEquals("Component of Component is wrong", "cms-resource-type", componentComponent);
    }

    @Test
    public void testContentComponent() {
        Resource resource = mock(Resource.class);
        AbstractComponent component = new AbstractComponent(resource);
        String path = "/content/test/resource";
        String subPath = "/jcr:content/sub/node";
        String contentPath = path + subPath;
        when(resource.getPath()).thenReturn(contentPath);
        String resourceType = "cms/resource/type";
        when(resource.getResourceType()).thenReturn(resourceType);

        Resource fromResource = component.getResource();
        assertNotNull("Missing Resource", fromResource);
        assertEquals("Wrong Resource returned", resource, fromResource);

        assertEquals("Wrong Path", subPath, component.getPath());
    }

    @Test
    public void testRootComponent() {
        Resource resource = mock(Resource.class);
        AbstractComponent component = new AbstractComponent(resource);
        String path = "/content/test/resource/jcr:content";
        String contentPath = path + "/jcr:content";
        when(resource.getPath()).thenReturn(contentPath);
        String resourceType = "cms/resource/type";
        when(resource.getResourceType()).thenReturn(resourceType);

        PageMerge pageMerge = new PageMerge();
        Bindings bindings = mock(Bindings.class);
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        when(bindings.get(eq(BindingsUseUtil.REQUEST))).thenReturn(request);
        SlingScriptHelper slingScriptHelper = mock(SlingScriptHelper.class);
        when(bindings.get(eq(BindingsUseUtil.SLING))).thenReturn(slingScriptHelper);
        Resource rootResource = mock(Resource.class);
        when(request.getResource()).thenReturn(rootResource);
        pageMerge.init(bindings);

        Resource fromResource = component.getResource();
        assertNotNull("Missing Resource", fromResource);
        assertEquals("Wrong Resource returned", resource, fromResource);

        Resource fromRootResource = component.getRootResource();
        assertEquals("Wrong Root Resource", rootResource, fromRootResource);
    }
}
