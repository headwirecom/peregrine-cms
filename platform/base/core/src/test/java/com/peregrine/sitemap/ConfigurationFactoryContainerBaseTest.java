package com.peregrine.sitemap;

import com.peregrine.SlingResourcesTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationFactoryContainerBaseTest extends SlingResourcesTest {

    private final ConfigurationFactoryContainerBase<Object> model = new ConfigurationFactoryContainerBase<Object>() {};

    @Test
    public void isPage() {
        assertFalse(model.remove(this));
        assertTrue(model.add(this));
        assertTrue(model.remove(this));
    }

}