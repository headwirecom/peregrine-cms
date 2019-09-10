package com.peregrine.commons.util;

//import com.peregrine.commons.test.AbstractTest;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PerUtilTest {
    /* To avoid circular dependencies these test cannot use commons-test **/
//    AbstractTest {

    private static final Logger logger = LoggerFactory.getLogger(PerUtilTest.class.getName());

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
}