package com.peregrine.commons.util;

import com.peregrine.commons.test.AbstractTest;
import net.bytebuddy.matcher.CollectionSizeMatcher;
import org.apache.sling.api.resource.Resource;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PerUtilTest
    extends AbstractTest
{
    private static final Logger logger = LoggerFactory.getLogger(PerUtilTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testSplitIntoMap() throws Exception {
        String[] configurations = new String[] { "data.json=example/components/page|per:Template","infinity.json=per:Object","html=per:Page|per:Template","*~raw=nt:file" };
        Map<String, List<String>> answer = PerUtil.splitIntoMap(configurations, "=", "\\|");
        Map<String, List<String>> expected = new HashMap<>();
        expected.put("data.json", Arrays.asList("example/components/page", "per:Template"));
        expected.put("infinity.json", Arrays.asList("per:Object"));
        expected.put("html", Arrays.asList("per:Page", "per:Template"));
        expected.put("*~raw", Arrays.asList("nt:file"));
        assertNotNull("Returned Map was null", answer);
        assertFalse("Returned Map was empty", answer.isEmpty());
        assertEquals("Map did not map", expected, answer);
    }

    @Test
    public void testGetComponentNameFromResource() throws Exception {
        Resource resource = mock(Resource.class);
        when(resource.getResourceType()).thenReturn("/one/twoThree/FourFive");
        String componentName = PerUtil.getComponentNameFromResource(resource);
        logger.info("Component Name: '{}'", componentName);
        assertEquals("Component Name Extraction failed", "one-two-three--four-five", componentName);
    }

    @Test
    public void stripJcrContentAndDescendants() {
        assertEquals("/content/page", PerUtil.stripJcrContentAndDescendants("/content/page/jcr:content/child"));
        assertEquals("/content/page", PerUtil.stripJcrContentAndDescendants("/content/page/jcr:content/"));
        assertEquals("/content/page", PerUtil.stripJcrContentAndDescendants("/content/page/jcr:content"));
        assertEquals("/content/page/", PerUtil.stripJcrContentAndDescendants("/content/page/"));
        assertEquals("/content/page", PerUtil.stripJcrContentAndDescendants("/content/page"));
    }

    @Test
    public void keysInMapHavingStringValueMatchingPredicate() {
        Predicate<String> hasLength5 = s -> s.length() == 5;
        assertThat(PerUtil.keysInMapHavingStringValueMatchingPredicate(
                Map.of("k", "1"),
                hasLength5), hasItems());
        assertThat(PerUtil.keysInMapHavingStringValueMatchingPredicate(
                Map.of("k", "12345"),
                hasLength5), hasItems("k"));
        assertThat(PerUtil.keysInMapHavingStringValueMatchingPredicate(
                Map.of("k", new String[] { "1", "2", "3", "4", "5"}),
                hasLength5), hasItems());
        assertThat(PerUtil.keysInMapHavingStringValueMatchingPredicate(
                Map.of("k", new String[] { "1", "12345", "123", "12", "1234"}),
                hasLength5), hasItems("k"));
    }

}