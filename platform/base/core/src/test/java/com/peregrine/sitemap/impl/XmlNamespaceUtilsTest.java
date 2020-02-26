package com.peregrine.sitemap.impl;

import com.peregrine.TestingTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public final class XmlNamespaceUtilsTest {

    @Test
    public void utilClass() {
        TestingTools.testUtilClass(XmlNamespaceUtils.class);
    }

    @Test
    public void parseMappingAddPrefix() {
        final Map<String, String> target = new HashMap<>();
        assertTrue(XmlNamespaceUtils.parseMappingAddPrefix("x=y", target));
        assertFalse(XmlNamespaceUtils.parseMappingAddPrefix("a", target));
        assertEquals("y", target.get("xmlns:x"));
        assertNull(target.get("xmlns:a"));
    }

    @Test
    public void parseMappingsAddPrefix() {
        final Map<String, String> target = XmlNamespaceUtils.parseMappingsAddPrefix("x=y", "a=b");
        assertEquals("y", target.get("xmlns:x"));
        assertEquals("b", target.get("xmlns:a"));
    }

}
