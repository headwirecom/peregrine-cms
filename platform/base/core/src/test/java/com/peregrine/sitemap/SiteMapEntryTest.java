package com.peregrine.sitemap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SiteMapEntryTest {

    private static final String PATH = "/content/sites/page";
    private static final String URL = "http://www.example.com";
    private static final String NAME = "propertyName";
    private static final Object VALUE = "propertyValue";

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
    public void walk() {
    }

    @Test
    public void testWalk() {
    }
}