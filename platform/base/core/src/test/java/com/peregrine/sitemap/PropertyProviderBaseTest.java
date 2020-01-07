package com.peregrine.sitemap;

import com.peregrine.commons.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public final class PropertyProviderBaseTest {

    private static final String PROPERTY_NAME = "propertyName";

    private final PropertyProviderBase model = new PropertyProviderBase(PROPERTY_NAME) {
        @Override
        public Object extractValue(final Page page) {
            return null;
        }
    };

    @Test
    public void getPropertyName() {
        assertEquals(PROPERTY_NAME, model.getPropertyName());
    }

}