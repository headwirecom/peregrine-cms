package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DirectPropertyProviderTest extends SlingResourcesTest {

    private static final String NAME = "name";
    private static final String ELEMENT_NAME = "elementName";
    private static final String PROPERTY_NAME = "propertyName";

    private final DirectPropertyProvider model = new DirectPropertyProvider();
    private final Page candidate = new Page(page);

    @Mock
    private DirectPropertyProviderConfig config;

    @Before
    public void setUp() {
        page.putProperty(NAME, NAME);
        page.putProperty(ELEMENT_NAME, ELEMENT_NAME);
        page.putProperty(PROPERTY_NAME, PROPERTY_NAME);
    }

    private void activate(final String name, final String elementName, final String propertyName) {
        when(config.name()).thenReturn(name);
        when(config.elementName()).thenReturn(elementName);
        when(config.propertyName()).thenReturn(propertyName);
        model.activate(config);
    }

    @Test
    public void nameOnly() {
        activate(NAME, null, null);
        assertEquals(NAME, model.getName());
        assertEquals(NAME, model.getPropertyName());
        assertEquals(NAME, model.extractValue(candidate));
    }

    @Test
    public void elementNameOnly() {
        activate(null, ELEMENT_NAME, null);
        assertEquals(ELEMENT_NAME, model.getName());
        assertEquals(ELEMENT_NAME, model.getPropertyName());
        assertEquals(ELEMENT_NAME, model.extractValue(candidate));
    }

    @Test
    public void propertyNameOnly() {
        activate(null, null, PROPERTY_NAME);
        assertEquals(PROPERTY_NAME, model.getName());
        assertEquals(PROPERTY_NAME, model.getPropertyName());
        assertEquals(PROPERTY_NAME, model.extractValue(candidate));
    }

    @Test
    public void withoutName() {
        activate(null, ELEMENT_NAME, PROPERTY_NAME);
        assertEquals(ELEMENT_NAME, model.getName());
        assertEquals(ELEMENT_NAME, model.getPropertyName());
        assertEquals(PROPERTY_NAME, model.extractValue(candidate));
    }

    @Test
    public void withoutElementName() {
        activate(NAME, null, PROPERTY_NAME);
        assertEquals(NAME, model.getName());
        assertEquals(PROPERTY_NAME, model.getPropertyName());
        assertEquals(PROPERTY_NAME, model.extractValue(candidate));
    }

    @Test
    public void withoutPropertyName() {
        activate(NAME, ELEMENT_NAME, null);
        assertEquals(NAME, model.getName());
        assertEquals(ELEMENT_NAME, model.getPropertyName());
        assertEquals(ELEMENT_NAME, model.extractValue(candidate));
    }

    @Test
    public void allNames() {
        activate(NAME, ELEMENT_NAME, PROPERTY_NAME);
        assertEquals(NAME, model.getName());
        assertEquals(ELEMENT_NAME, model.getPropertyName());
        assertEquals(PROPERTY_NAME, model.extractValue(candidate));
    }

    @Test
    public void missingProperty() {
        activate(null, null, "missingProperty");
        assertNull(model.extractValue(candidate));
    }

}