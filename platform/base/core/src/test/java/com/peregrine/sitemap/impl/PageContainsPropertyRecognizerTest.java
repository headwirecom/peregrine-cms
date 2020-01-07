package com.peregrine.sitemap.impl;

import com.peregrine.SlingResourcesTest;
import com.peregrine.commons.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public final class PageContainsPropertyRecognizerTest extends SlingResourcesTest {

    private static final String NAME = "my-name";
    private static final String PROPERTY_NAME = "my-property";

    private final PageContainsPropertyRecognizer model = new PageContainsPropertyRecognizer();
    private final Page candidate = new Page(page);

    @Mock
    private PageContainsPropertyRecognizerConfig config;

    @Before
    public void setUp() {
        Mockito.when(config.name()).thenReturn(NAME);
        Mockito.when(config.propertyName()).thenReturn(PROPERTY_NAME);
        model.activate(config);
    }

    @Test
    public void getName() {
        assertEquals(NAME, model.getName());
    }

    @Test
    public void isPage() {
        assertFalse(model.isPage(candidate));
        page.putProperty(PROPERTY_NAME, "value");
        assertTrue(model.isPage(candidate));
    }

}