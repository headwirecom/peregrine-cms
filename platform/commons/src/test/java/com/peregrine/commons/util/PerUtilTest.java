package com.peregrine.commons.util;

import com.peregrine.ResourceMock;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PerUtilTest {

    @Test
    public void splitIntoMap() {
        final String[] configurations = new String[] { "data.json=example/components/page|per:Template","infinity.json=per:Object","html=per:Page|per:Template","*~raw=nt:file" };
        final Map<String, List<String>> answer = PerUtil.splitIntoMap(configurations, "=", "\\|");
        final Map<String, List<String>> expected = new HashMap<>();
        expected.put("data.json", Arrays.asList("example/components/page", "per:Template"));
        expected.put("infinity.json", Arrays.asList("per:Object"));
        expected.put("html", Arrays.asList("per:Page", "per:Template"));
        expected.put("*~raw", Arrays.asList("nt:file"));
        assertNotNull("Returned Map was null", answer);
        assertFalse("Returned Map was empty", answer.isEmpty());
        assertEquals("Map did not map", expected, answer);
    }

    @Test
    public void splitIntoParameterMap_entriesAreNull() {
        final Map<String, Map<String, String>> actual = PerUtil
                .splitIntoParameterMap(null, null, null, null);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void splitIntoParameterMap_keySeparatorIsEmpty() {
        final Map<String, Map<String, String>> actual = PerUtil
                .splitIntoParameterMap(new String[0], null, null, null);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void splitIntoParameterMap_emptySplit_break() {
        final String space = " ";
        final String[] entries = new String[] {
                null,
                "",
                space
        };

        final Map<String, Map<String, String>> actual = PerUtil
                .splitIntoParameterMap(entries, space, null, null);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void splitIntoParameterMap() {
        final String noValueKey = "no_value";
        final String space = " ";
        final String[] entries = new String[] {
                space,
                noValueKey,
                "x:y=z,s=t",
                "y:x=z",
                "z:"
        };

        final Map<String, Map<String, String>> expected = new LinkedHashMap<>();

        expected.put(space, new LinkedHashMap<>());
        expected.put(noValueKey, new LinkedHashMap<>());

        final Map<String, String> xValue = new LinkedHashMap<>();
        xValue.put("y", "z");
        xValue.put("s", "t");
        expected.put("x", xValue);

        final Map<String, String> yValue = new LinkedHashMap<>();
        yValue.put("x", "z");
        expected.put("y", yValue);

        expected.put("z", new LinkedHashMap<>());

        final Map<String, Map<String, String>> actual = PerUtil
                .splitIntoParameterMap(entries, ":", ",", "=");
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void splitIntoParameterMap_incorrectEntry_throwIllegalArgumentException() {
        final String[] entries = new String[] { "a:b:c" };
        PerUtil.splitIntoParameterMap(entries, ":", ",", "=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void splitIntoParameterMap_incorrectEntryValue_throwIllegalArgumentException() {
        final String[] entries = new String[] { "a:x=y=z" };
        PerUtil.splitIntoParameterMap(entries, ":", ",", "=");
    }

    @Test
    public void relativePath() {
        assertRelativePath("/content", "/content", null);
        assertRelativePath("/content", "/content/child", "child");
        assertRelativePath("/content/parent", "/content/child", null);
        assertRelativePath("/contents", "/content/child", null);
        assertRelativePath("/content", "/contents/child", null);
        assertRelativePath("/content", "/content/ ", " ");
    }

    private void assertRelativePath(final String rootPath, final String childPath, final String result) {
        final ResourceMock root = new ResourceMock();
        root.setPath(rootPath);
        final ResourceMock child = new ResourceMock();
        child.setPath(childPath);
        assertEquals(result, PerUtil.relativePath(root, child));
    }

    @Test
    public void getProperties() {
        assertNull(PerUtil.getProperties(null, false));

        final Resource resource = mock(Resource.class);
        assertNull(PerUtil.getProperties(resource, false));
        assertNull(PerUtil.getProperties(resource));

        final Resource content = Mockito.mock(Resource.class);
        when(resource.getChild(PerConstants.JCR_CONTENT)).thenReturn(content);
        assertNull(PerUtil.getProperties(resource, false));
        assertNull(PerUtil.getProperties(resource));

        final ValueMap valueMap = mock(ValueMap.class);
        when(resource.getValueMap()).thenReturn(valueMap);
        assertEquals(valueMap, PerUtil.getProperties(resource, false));
        assertNull(PerUtil.getProperties(resource));

        final ValueMap contentValueMap = mock(ValueMap.class);
        when(content.getValueMap()).thenReturn(contentValueMap);
        assertEquals(valueMap, PerUtil.getProperties(resource, false));
        assertEquals(contentValueMap, PerUtil.getProperties(resource));

        when(resource.getName()).thenReturn(PerConstants.JCR_CONTENT);
        assertEquals(valueMap, PerUtil.getProperties(resource, false));
        assertEquals(valueMap, PerUtil.getProperties(resource));
    }

    @Test
    public void listParents() {
        final ResourceMock root = new ResourceMock();
        final ResourceMock parent = new ResourceMock();
        final ResourceMock resource = new ResourceMock();
        final ResourceMock child = new ResourceMock();

        root.setPath("/content");
        parent.setPath("/content/parent");
        resource.setPath("/content/parent/resource");
        child.setPath("/content/parent/resource/jcr:content");

        parent.setParent(root);
        resource.setParent(parent);
        child.setParent(resource);

        List<Resource> result = PerUtil.listParents(root, child);
        assertEquals(2, result.size());
        assertTrue(result.contains(resource));
        assertTrue(result.contains(parent));
        assertEquals(resource, result.get(0));
        assertEquals(parent, result.get(1));

        assertEquals(1, PerUtil.listParents(root, resource).size());
        assertEquals(0, PerUtil.listParents(root, parent).size());
        assertEquals(0, PerUtil.listParents(root, root).size());
        assertEquals(0, PerUtil.listParents(parent, root).size());
    }

    @Test
    public void containsResource() {
        final ResourceMock root = new ResourceMock();
        final ResourceMock parent = new ResourceMock();
        final ResourceMock resource = new ResourceMock();
        final ResourceMock child = new ResourceMock();

        root.setPath("/content");
        parent.setPath("/content/parent");
        resource.setPath("/content/parent/resource");
        child.setPath("/content/parent/resource/jcr:content");

        final List<Resource> list = new LinkedList<>();
        list.add(root);
        list.add(parent);
        list.add(resource);

        assertTrue(PerUtil.containsResource(list, root));
        assertTrue(PerUtil.containsResource(list, parent));
        assertTrue(PerUtil.containsResource(list, resource));
        assertFalse(PerUtil.containsResource(list, child));
        assertTrue(PerUtil.containsResource(list, null));
    }

    @Test
    public void listMissingParents() {
    }

    @Test
    public void getPrimaryType() {
        assertNull(PerUtil.getPrimaryType(null));

        final ResourceMock resource = new ResourceMock();
        final String primaryType = "per/component";
        resource.getProperties().put(JCR_PRIMARY_TYPE, primaryType);
        assertEquals(primaryType, PerUtil.getPrimaryType(resource));
    }

    @Test
    public void getResourceType() {
    }

    @Test
    public void getComponentNameFromResource() {
        final  Resource resource = mock(Resource.class);
        when(resource.getResourceType()).thenReturn("/one/twoThree/FourFive");
        final String componentName = PerUtil.getComponentNameFromResource(resource);
        assertEquals("Component Name Extraction failed", "one-two-three--four-five", componentName);
    }

    @Test
    public void getComponentVariableNameFromString() {
    }
}