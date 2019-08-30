package com.peregrine.commons.util;

import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

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
        final Resource root = Mockito.mock(Resource.class);
        Mockito.when(root.getPath()).thenReturn(rootPath);
        final Resource child = Mockito.mock(Resource.class);
        Mockito.when(child.getPath()).thenReturn(childPath);
        assertEquals(result, PerUtil.relativePath(root, child));
    }

    @Test
    public void getProperties() {
    }

    @Test
    public void listParents() {
    }

    @Test
    public void containsResource() {
    }

    @Test
    public void listMissingParents() {
    }

    @Test
    public void loginService() {
    }

    @Test
    public void getPrimaryType() {
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