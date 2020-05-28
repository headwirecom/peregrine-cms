package com.peregrine.sitemap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class SiteMapEntryTest implements SiteMapEntry.Visitor<Integer> {

    private static final String PATH = "/content/example/pages/index";
    private static final String URL = "http://www.example.com";
    private static final String NAME = "propertyName";
    private static final String VALUE = "propertyValue";

    private final SiteMapEntry model = new SiteMapEntry(PATH);

    @Test
    public void getPath() {
        assertEquals(PATH, model.getPath());
    }

    @Test
    public void setUrl() {
        model.setUrl(URL);
        assertEquals(URL, model.getUrl());
    }

    @Test
    public void putProperty() {
        assertNull(model.putProperty(null, VALUE));
        assertNull(model.putProperty(NAME, VALUE));
        assertEquals(VALUE, model.putProperty(NAME, null));
    }

    @Test
    public void getProperty() {
        assertNull(model.getProperty(NAME, Integer.class));
        assertNull(model.putProperty(NAME, VALUE));
        assertNull(model.getProperty(NAME, Integer.class));
        assertEquals(VALUE, model.getProperty(NAME, String.class));
    }

    @Test
    public void getLastModified() {
        assertNull(model.getLastModified());
        model.setLastModified(VALUE);
        assertEquals(VALUE, model.getLastModified());
    }

    @Test
    public void walk() {
        model.putProperty("x", 1);
        final Map<String, Object> y = new HashMap<>();
        y.put("x", 2);
        model.putProperty("y", y);
        assertEquals((Object)7, model.walk(this, 0));
    }

    @Override
    public Integer visit(final String mapName, final Map<String, String> properties, final Integer result) {
        return result + properties.size();
    }

    @Override
    public Integer visit(final String propertyName, final String propertyValue, final Integer result) {
        return result + Integer.parseInt(propertyValue);
    }

    @Override
    public Integer endVisit(final String mapName, final Integer result) {
        return result + 1;
    }
}