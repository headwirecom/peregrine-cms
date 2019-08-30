package com.peregrine.commons.util;

import org.apache.sling.api.resource.Resource;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    @Test
    public void splitIntoParameterMap() {
    }

    @Test
    public void relativePath() {
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