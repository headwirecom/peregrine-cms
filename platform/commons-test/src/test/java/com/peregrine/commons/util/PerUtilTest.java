package com.peregrine.commons.util;

import com.peregrine.commons.test.SlingResourcesTest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PerUtilTest
    /* To avoid circular dependencies these test cannot use commons-test **/
    extends SlingResourcesTest
{

    private final int initialResourcesSize;

    private final PerUtil.ResourceChecker resourceChecker = mock(PerUtil.ResourceChecker.class);

    public PerUtilTest() {
        initialResourcesSize = resources.size();
    }

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
        root.setPath(rootPath);
        resource.setPath(childPath);
        assertEquals(result, PerUtil.relativePath(root, resource));
    }

    @Test
    public void getProperties() {
        assertNull(PerUtil.getProperties(null, false));

        final Resource resource = mock(Resource.class);
        assertNull(PerUtil.getProperties(resource, false));
        assertNull(PerUtil.getProperties(resource));

        final Resource content = mock(Resource.class);
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
        final List<Resource> result = PerUtil.listParents(root, content);
        assertEquals(2, result.size());
        assertTrue(result.contains(page));
        assertTrue(result.contains(parent));
        assertEquals(page, result.get(0));
        assertEquals(parent, result.get(1));

        assertEquals(1, PerUtil.listParents(root, page).size());
        assertEquals(0, PerUtil.listParents(root, parent).size());
        assertEquals(0, PerUtil.listParents(root, root).size());
        assertEquals(0, PerUtil.listParents(parent, root).size());
    }

    @Test
    public void listMissingResources_nullInputs() {
        PerUtil.listMissingResources(null, resources, resourceChecker, true);
        PerUtil.listMissingResources(root, null, resourceChecker, true);
        PerUtil.listMissingResources(root, resources, null, true);
        assertEquals(initialResourcesSize, resources.size());
    }

    @Test
    public void listMissingResources_doNotAddResources() {
        when(resourceChecker.doAdd(any())).thenReturn(false);
        when(resourceChecker.doAddChildren(any())).thenReturn(false);
        PerUtil.listMissingResources(root, resources, resourceChecker, true);
        assertEquals(initialResourcesSize, resources.size());
    }

    @Test
    public void listMissingResources_doNotAddChildren() {
        when(resourceChecker.doAdd(any())).thenReturn(true);
        when(resourceChecker.doAddChildren(any())).thenReturn(true);
        resources.remove(root);
        PerUtil.listMissingResources(root, resources, resourceChecker, true);
        assertEquals(initialResourcesSize, resources.size());
    }

    @Test
    public void listMissingResources() {
        when(resourceChecker.doAdd(any())).thenReturn(true);
        when(resourceChecker.doAddChildren(any())).thenReturn(true);

        PerUtil.listMissingResources(root, resources, resourceChecker, false);
        assertEquals(initialResourcesSize, resources.size());

        resources.remove(content);
        resources.remove(resource);
        PerUtil.listMissingResources(page, resources, resourceChecker, false);
        assertEquals(initialResourcesSize, resources.size());
    }

    @Test
    public void containsResource() {
        resources.remove(content);

        assertTrue(PerUtil.containsResource(resources, root));
        assertTrue(PerUtil.containsResource(resources, parent));
        assertTrue(PerUtil.containsResource(resources, page));
        assertFalse(PerUtil.containsResource(resources, content));
        assertTrue(PerUtil.containsResource(resources, null));
    }

    @Test
    public void listMissingParents_nullInputs() {
        PerUtil.listMissingParents(null, resources, resource, resourceChecker);
        PerUtil.listMissingParents(resource, null, resource, resourceChecker);
        PerUtil.listMissingParents(resource, resources, null, resourceChecker);
        PerUtil.listMissingParents(resource, resources, resource, null);
        assertEquals(initialResourcesSize, resources.size());
    }

    @Test
    public void listMissingParents() {
        final List<Resource> response = new LinkedList<>();
        response.add(parent);

        when(resourceChecker.doAdd(parent)).thenReturn(true);
        when(resourceChecker.doAdd(page)).thenReturn(true);
        when(resourceChecker.doAdd(content)).thenReturn(false);

        PerUtil.listMissingParents(resource, response, root, resourceChecker);

        assertFalse(response.contains(root));
        assertTrue(response.contains(parent));
        assertTrue(response.contains(page));
        assertFalse(response.contains(content));
        assertFalse(response.contains(resource));
    }

    @Test(expected = IllegalArgumentException.class)
    public void loginService_missingResolverFactory() throws LoginException {
        PerUtil.loginService(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loginService_emptyServiceName() throws LoginException {
        PerUtil.loginService(resolverFactory, null);
    }

    @Test
    public void loginService() throws LoginException {
        when(resolverFactory.getServiceResourceResolver(any())).thenReturn(resourceResolver);
        assertEquals(resourceResolver, PerUtil.loginService(resolverFactory, "service"));
    }

    @Test
    public void adjustMetadataName() {
        assertAdjustMetadataName(null, null);
        assertAdjustMetadataName(" ", "_");
        assertAdjustMetadataName(SLASH, "_");
        assertAdjustMetadataName("Let it be Slash /", "let_it_be_slash__");
    }

    private void assertAdjustMetadataName(final String input, final String expected) {
        assertEquals(expected, PerUtil.adjustMetadataName(input));
    }

    @Test
    public void isResourceType() {
        assertFalse(PerUtil.isResourceType(null, RESOURCE_TYPE));
        assertFalse(PerUtil.isResourceType(mock(Resource.class), RESOURCE_TYPE));
        assertFalse(PerUtil.isResourceType(resource, RESOURCE_TYPE));

        resource.putProperty(SLING_RESOURCE_TYPE, RESOURCE_TYPE);
        assertFalse(PerUtil.isResourceType(resource, PRIMARY_TYPE));
        assertTrue(PerUtil.isResourceType(resource, RESOURCE_TYPE));
    }

    @Test
    public void isPrimaryType() {
        assertFalse(PerUtil.isPrimaryType(null, PRIMARY_TYPE));
        assertFalse(PerUtil.isPrimaryType(mock(Resource.class), PRIMARY_TYPE));
        assertFalse(PerUtil.isPrimaryType(resource, PRIMARY_TYPE));

        resource.putProperty(JCR_PRIMARY_TYPE, PRIMARY_TYPE);
        assertFalse(PerUtil.isPrimaryType(resource, RESOURCE_TYPE));
        assertTrue(PerUtil.isPrimaryType(resource, PRIMARY_TYPE));
    }

    @Test
    public void getPrimaryType() {
        assertNull(PerUtil.getPrimaryType(null));

        resource.putProperty(JCR_PRIMARY_TYPE, PRIMARY_TYPE);
        assertEquals(PRIMARY_TYPE, PerUtil.getPrimaryType(resource));
    }

    @Test
    public void getResourceType() {
        assertNull(PerUtil.getResourceType(null));
        assertNull(PerUtil.getResourceType(mock(Resource.class)));

        assertNull(PerUtil.getResourceType(resource));

        page.getContent().putProperty(SLING_RESOURCE_TYPE, RESOURCE_TYPE);
        assertEquals(RESOURCE_TYPE, PerUtil.getResourceType(page));
    }

    @Test
    public void getComponentNameFromResource() {
        assertEquals("", PerUtil.getComponentNameFromResource(resource));
        when(resource.getResourceType()).thenReturn("/one/twoThree/FourFive");
        final String componentName = PerUtil.getComponentNameFromResource(resource);
        assertEquals("Component Name Extraction failed", "one-two-three--four-five", componentName);
    }

    @Test
    public void getComponentVariableNameFromString() {
        assertEquals("", PerUtil.getComponentVariableNameFromString(null));
        assertEquals("", PerUtil.getComponentVariableNameFromString(""));
        assertEquals("", PerUtil.getComponentVariableNameFromString("   "));

        assertEquals("cmpPeregrineComponent", PerUtil.getComponentVariableNameFromString("peregrine/component"));
        assertEquals("cmpPeregrineComponent", PerUtil.getComponentVariableNameFromString("/peregrine/component"));
    }

    @Test
    public void convertToMap() throws IOException {
        assertTrue(PerUtil.convertToMap(null).isEmpty());
        assertEquals(5, PerUtil.convertToMap("{\"property\": 5}").get("property"));
    }

    @Test
    public void isPropertyPresentAndEqualsTrue() throws RepositoryException {
        final Node node = resource.getNode();
        assertFalse(PerUtil.isPropertyPresentAndEqualsTrue(node, PROPERTY_NAME));
        resource.putProperty(PROPERTY_NAME, false);
        assertFalse(PerUtil.isPropertyPresentAndEqualsTrue(node, PROPERTY_NAME));
        resource.putProperty(PROPERTY_NAME, true);
        assertTrue(PerUtil.isPropertyPresentAndEqualsTrue(node, PROPERTY_NAME));
        when(node.hasProperty(any())).thenThrow(RepositoryException.class);
        assertFalse(PerUtil.isPropertyPresentAndEqualsTrue(node, PROPERTY_NAME));
    }
}