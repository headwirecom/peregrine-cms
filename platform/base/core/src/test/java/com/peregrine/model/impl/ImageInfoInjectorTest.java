package com.peregrine.model.impl;

import com.peregrine.testmodels.DefaultImageInfoModel;
import com.peregrine.commons.test.AbstractTest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.impl.ModelAdapterFactory;
import org.apache.sling.models.impl.ModelAdapterFactorySetup;
import org.apache.sling.models.impl.injectors.ValueMapInjector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageInfoInjectorTest extends AbstractTest {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ModelAdapterFactory factory;
    private ResourceResolver resourceResolver;

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Before
    public void setup() {
        super.setup();
        try {
            factory = ModelAdapterFactorySetup.createModelAdapterFactory(null, DefaultImageInfoModel.class);
            // Add the Resource in Constructor Injection
            ModelAdapterFactorySetup.addInjector(factory, new ImageInfoInjector(), 1);
            // Add the Value Map properties Injection
            ModelAdapterFactorySetup.addInjector(factory, new ValueMapInjector(), 2);
            resourceResolver = mock(ResourceResolver.class, "Test Resource Resolver");
        } catch (Exception e) {
            throw new RuntimeException("Setup Failed", e);
        }
    }

    @Test
    public void testImageDataFromMeta() {
        String title = "testImageInfo";
        String caption = "Test Image Info";
        String imageName = "013.jpg";
        String imagePath = "/content/unitTest/assets/images/" + imageName;
        int width = 111;
        int height = 999;

        Resource testPageResource = createMockResource(
            "test page", null, "per:Page",
            "title", title, "caption", caption, "imagePath", imagePath
        );
        // Asset Test Resource
        Resource assetTestResource = createMockResource(
            imageName, null, "per:Asset"
        );
        // Resource Resolver needs to return Asset
        when(testPageResource.getResourceResolver()).thenReturn(resourceResolver);
        when(resourceResolver.getResource(imagePath)).thenReturn(assetTestResource);

        // Create JCR Content, Metadata and Per Data node
        Resource jcrContentTestResource = createMockResource(
            "jcr:content", assetTestResource, "per:AssetContent",
            "jcr:mimeType", "image/jpeg"
        );
        Resource metaDataResource = createMockResource("metadata", jcrContentTestResource, "sling:Folder");
        Resource perDataResource = createMockResource(
            "per-data", metaDataResource, "sling:Folder",
            "width", width, "height", height
        );

        DefaultImageInfoModel model = factory.getAdapter(testPageResource, DefaultImageInfoModel.class);
        assertNotNull(model);
        assertEquals(title, model.getTitle());
        assertEquals(caption, model.getCaption());
        assertEquals(imagePath, model.getImagePath());
        assertEquals("{'width': " + width + ", 'height': " + height + "}", model.getImageInfo());
    }

    @Test
    public void testImageDataWitNoData() {
        String title = "noDataTestImage";
        String caption = "Test Image Info";
        String imageName = "013.jpg";
        String imagePath = "/content/unitTest/assets/images/" + imageName;

        Resource testPageResource = createMockResource(
            "test page", null, "per:Page",
            "title", title, "caption", caption, "imagePath", imagePath
        );
        // Asset Test Resource
        Resource assetTestResource = createMockResource(
            imageName, null, "per:Asset"
        );
        // Resource Resolver needs to return Asset
        when(testPageResource.getResourceResolver()).thenReturn(resourceResolver);
        when(resourceResolver.getResource(imagePath)).thenReturn(assetTestResource);

        // Create JCR Content, Metadata and Per Data node
        Resource jcrContentTestResource = createMockResource(
            "jcr:content", assetTestResource, "per:AssetContent",
            "jcr:mimeType", "image/jpeg"
        );

        DefaultImageInfoModel model = factory.getAdapter(testPageResource, DefaultImageInfoModel.class);
        assertNotNull(model);
        assertEquals(title, model.getTitle());
        assertEquals(caption, model.getCaption());
        assertEquals(imagePath, model.getImagePath());
        assertNull(model.getImageInfo());
    }

//    @Test
//    public void testImageDataFromImage() throws Exception {
//        String title = "testImageInfo";
//        String caption = "Test Image Info";
//        String imageName = "013.jpg";
//        String imagePath = "/content/unitTest/assets/images/" + imageName;
//        int width = 1280;
//        int height = 1024;
//
//        Resource testPageResource = createMockResource(
//            "test page", null, "per:Page",
//            "title", title, "caption", caption, "imagePath", imagePath
//        );
//        // Asset Test Resource
//        Resource assetTestResource = createMockResource(
//            imageName, null, "per:Asset"
//        );
//        // Resource Resolver needs to return Asset
//        when(testPageResource.getResourceResolver()).thenReturn(resourceResolver);
//        when(resourceResolver.getResource(imagePath)).thenReturn(assetTestResource);
//
//        InputStream is = getClass().getResourceAsStream("/test.image.jpg");
//        // Create JCR Content, Metadata and Per Data node
//        Resource jcrContentTestResource = createMockResource(
//            "jcr:content", assetTestResource, "per:AssetContent",
//            "jcr:mimeType", "image/jpeg",
//            "jcr:data", is
//        );
//
//        // Creation of the Metadata resource must be delated until it is created by the ImageInfoInjector and with it the per-data resource
//        Map<String, Resource> createdResource = new HashMap<>();
//        when(resourceResolver.create(eq(jcrContentTestResource), eq("metadata"), any(Map.class)))
//            .thenAnswer(
//                invocation -> {
//                    Resource answer = createMockResource("metadata", jcrContentTestResource, "sling:Folder");
//                    createdResource.put("metadata", answer);
//                    return answer;
//                }
//            );
//        when(resourceResolver.create(any(Resource.class), eq("per-data"), any(Map.class)))
//            .thenAnswer(
//                invocation -> {
//                    Resource metaDataResource = createdResource.get("metadata");
//                    Resource answer = createMockResource("per-data", metaDataResource, "sling:Folder");
//                    createdResource.put("per-data", answer);
//                    return answer;
//                }
//            );
//
//        DefaultImageInfoModel model = factory.getAdapter(testPageResource, DefaultImageInfoModel.class);
//        assertNotNull("Failed to create Model", model);
//        assertEquals(title, model.getTitle());
//        assertEquals(caption, model.getCaption());
//        assertEquals(imagePath, model.getImagePath());
//        assertEquals("{'width': " + width + ", 'height': " + height + "}", model.getImageInfo());
//        Resource perDataResource = createdResource.get("per-data");
//        assertNotNull("per-data resource not found", perDataResource);
//        ValueMap properties = perDataResource.getValueMap();
//        assertEquals(properties.get("width"), width);
//        assertEquals(properties.get("height"), height);
//    }

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
}
