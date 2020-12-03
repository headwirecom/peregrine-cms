package com.peregrine.model.impl;

import com.peregrine.model.api.ImageInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.spi.Injector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ImageInfoInjectorTest {

    private static final String IMAGE_NAME = "013.jpg";
    private static final String IMAGE_PATH = "/content/unitTest/assets/images/" + IMAGE_NAME;

    private final Injector model = new ImageInfoInjector();

    private final ResourceResolver resourceResolver = mock(ResourceResolver.class, "Test Resource Resolver");
    private final Resource testPageResource = createMockResource(
            "test page", null, "per:Page",
            "imagePath", IMAGE_PATH
    );
    private final Resource assetTestResource = createMockResource(
            IMAGE_NAME, null, "per:Asset"
    );
    private final Resource assetContent = createMockResource(
                JCR_CONTENT, assetTestResource, "per:AssetContent",
            "jcr:mimeType", "image/jpeg"
    );

    @ImageInfo(name = "imagePath")
    private Dimension imageInfo;

    private Field annotatedField;

    @Before
    public void setup() throws NoSuchFieldException {
        annotatedField = getClass().getDeclaredField("imageInfo");
        // Resource Resolver needs to return Asset
        when(testPageResource.getResourceResolver()).thenReturn(resourceResolver);
        when(resourceResolver.getResource(IMAGE_PATH)).thenReturn(assetTestResource);
    }

    private Resource createMockResource(String name, Resource parent, String primaryType, Object...properties) {
        Resource answer = name == null ? mock(Resource.class) : mock(Resource.class, name);
        when(answer.getResourceResolver()).thenReturn(resourceResolver);
        if(parent != null) {
            when(parent.getParent()).thenReturn(parent);
            when(parent.getChild(name)).thenReturn(answer);
        }
        HashMap<String, Object> values = new HashMap<>();
        if(primaryType != null) {
            values.put(JCR_PRIMARY_TYPE, primaryType);
        }
        ValueMap vm;
        if(properties != null) {
            if(properties.length % 2 != 0) {
                throw new IllegalArgumentException("Properties list must be on even number of entries but was: " + properties.length);
            }
            int count = properties.length / 2;
            for(int i = 0; i < count; i++) {
                values.put(properties[2 * i] + "", properties[(2*i)+1]);
            }
            vm = new ValueMapDecorator(values);
        } else {
            vm = new ValueMapDecorator(Collections.EMPTY_MAP);
        }
        when(answer.getValueMap()).thenReturn(vm);
        when(answer.adaptTo(ValueMap.class)).thenReturn(vm);
        return answer;
    }

    private Dimension getValue(final Resource resource) {
        return (Dimension) model.getValue(resource, null, Dimension.class, annotatedField, null);
    }

    @Test
    public void testImageDataFromMeta() {
        final var map = new HashMap<String, Object>();
        map.put("jcr:mimeType", "image/jpeg");
        map.put(JCR_DATA, getClass().getResourceAsStream("/mj.jpg"));
        when(assetContent.getValueMap()).thenReturn(new ValueMapDecorator(map));
        final var dimension = getValue(testPageResource);
        assertEquals(614, dimension.getWidth(), 0.1);
        assertEquals(410, dimension.getHeight(), 0.1);
    }

    @Test
    public void testImageWithNoData() {
        assertNull(getValue(testPageResource));
    }

}
