package com.peregrine.commons;

import com.peregrine.SlingResourcesTest;
import org.apache.jackrabbit.JcrConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public final class PageTest extends SlingResourcesTest {

    private static final String NAME = "propertyName";
    private static final Object VALUE = "propertyValue";
    private static final Object DEFAULT_VALUE = "defaultValue";

    private final Page missingContent = new Page(parent);
    private final Page properPage = new Page(page);

    @Test
    public void hasContent() {
        assertFalse(missingContent.hasContent());
        assertTrue(properPage.hasContent());
    }

    @Test
    public void getContent() {
        assertNull(missingContent.getContent());
        assertEquals(jcrContent, properPage.getContent());
    }

    @Test
    public void containsProperty() {
        assertFalse(missingContent.containsProperty(NAME));
        parent.putProperty(NAME, VALUE);
        assertTrue(missingContent.containsProperty(NAME));

        assertFalse(properPage.containsProperty(NAME));
        jcrContent.putProperty(NAME, VALUE);
        assertTrue(properPage.containsProperty(NAME));
    }

    @Test
    public void getProperty() {
        assertNull(missingContent.getProperty(NAME));
        parent.putProperty(NAME, VALUE);
        assertEquals(VALUE, missingContent.getProperty(NAME));
    }

    @Test
    public void getProperty_type() {
        assertNull(properPage.getProperty(NAME, String.class));
        jcrContent.putProperty(NAME, VALUE);
        assertEquals(VALUE, properPage.getProperty(NAME, String.class));
    }

    @Test
    public void getProperty_defaultValue() {
        assertEquals(DEFAULT_VALUE, properPage.getProperty(NAME, DEFAULT_VALUE));
        jcrContent.putProperty(NAME, VALUE);
        assertEquals(VALUE, properPage.getProperty(NAME, DEFAULT_VALUE));

        jcrContent.putProperty(NAME, null);
        assertNull(properPage.getProperty(NAME, DEFAULT_VALUE));
    }

    @Test
    public void getLastModified() {
        assertNull(properPage.getLastModified());
        assertNull(properPage.getLastModifiedDate());

        final Calendar created = Calendar.getInstance();
        jcrContent.putProperty(JcrConstants.JCR_CREATED, created);
        assertEquals(created, properPage.getLastModified());
        assertEquals(created.getTime(), properPage.getLastModifiedDate());

        final Calendar lastModified = Calendar.getInstance();
        jcrContent.putProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);
        assertEquals(lastModified, properPage.getLastModified());
        assertEquals(lastModified.getTime(), properPage.getLastModifiedDate());
    }

}