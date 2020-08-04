/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.peregrine.model.impl;

import com.peregrine.model.api.ImageInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerConstants.SVG_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.IMAGE_HEIGHT;
import static com.peregrine.commons.util.PerUtil.IMAGE_WIDTH;
import static com.peregrine.commons.util.PerUtil.METADATA;
import static com.peregrine.commons.util.PerUtil.PER_DATA;

@Component(
    service={
        Injector.class
    },
    property=Constants.SERVICE_RANKING + "=" + "2000"
)
public class ImageInfoInjector implements Injector {

    private static final Logger log = LoggerFactory.getLogger(ImageInfoInjector.class);

    public static final String IMAGE_INFO_FORMAT = "{'width': %1$d, 'height': %2$d}";

    private static final List<String> UNSUPPORTED_IMAGE_MIMES = Arrays.asList(SVG_MIME_TYPE);

    @Override
    public @NotNull String getName() {
        return "imageinfo";
    }

    @Override
    public Object getValue(@NotNull Object adaptable, String name, @NotNull Type type, @NotNull AnnotatedElement element,
            @NotNull DisposalCallbackRegistry callbackRegistry
    ) {
        String answer = null;
        try {
            if (adaptable != ObjectUtils.NULL) {
                Annotation annotation = element.getDeclaredAnnotation(ImageInfo.class);
                if (annotation != null) {
                    ValueMap map = getValueMap(adaptable);
                    if (map == null) {
                        return null;
                    } else if (type instanceof Class<?>) {
                        Class<?> clazz = (Class<?>) type;
                        if (clazz == String.class) {
                            try {
                                String sourceName = ((ImageInfo) annotation).name();
                                ImageData imageData = getImageData(sourceName, adaptable);
                                if (imageData != null) {
                                    answer = String.format(IMAGE_INFO_FORMAT, imageData.width, imageData.height);
                                }
                            } catch (ClassCastException e) {
                                log.warn("Could not obtain Value from ValueMap with name: '{}'", name);
                            }
                        }
                    }
                }
            }
        } catch(RuntimeException e) {
            // A Runtime Exception is causing the entire page fail to load (due to be children being null)
        }
        return answer;
    }

    private ImageData getImageData(String imagePropertyName, Object adaptable) {
        ImageData answer = null;
        ValueMap map = getValueMap(adaptable);
        String imagePath = map.get(imagePropertyName, String.class);
        // Only handle local images
        if(imagePath.indexOf(":/") < 0) {
            Resource imageResource = imagePath == null ? null : getResourceResolver(adaptable).getResource(imagePath);
            // Obtain Image
            Resource imageContentResource = imageResource == null ? null : imageResource.getChild(JCR_CONTENT);
            // Try to get the 'metadata' and the dimension based on the Image Type
            if (imageContentResource != null) {
                Resource metaData = imageContentResource.getChild(METADATA);
                if (metaData != null) {
                    answer = getDataFromMeta(metaData);
                }
// Right now we do not obtain the info when there is no metadata there
//                if (answer == null) {
//                    answer = getDataFromImage(imageContentResource);
//                }
            }
        }

        return answer;
    }

    private ImageData getDataFromMeta(Resource metaData) {
        ImageData answer = null;
        Resource perData = metaData.getChild(PER_DATA);
        ValueMap pngProperties = perData == null ? null : perData.getValueMap();
        if(pngProperties != null) {
            Integer width = pngProperties.get(IMAGE_WIDTH, Integer.class);
            Integer height = pngProperties.get(IMAGE_HEIGHT, Integer.class);
            if (width != null || height != null) {
                answer = new ImageData().setWidth(width).setHeight(height);
            }
        }
        return answer;
    }

    private ImageData getDataFromImage(Resource imageContentResource) {
        ImageData answer = null;
        ValueMap properties = imageContentResource.getValueMap();
        String mimeType = properties.get(JCR_MIME_TYPE, String.class);
        // SVG images are not handled by ImageIO so we ignore it here
        if (mimeType != null && !UNSUPPORTED_IMAGE_MIMES.contains(mimeType)) {
            InputStream imageStream = properties.get(JCR_DATA, InputStream.class);
            try {
                BufferedImage image = imageStream == null ? null : ImageIO.read(imageStream);
                if (image != null) {
                    // Obtain height and width
                    int height = image.getHeight();
                    int width = image.getWidth();
                    // Store info into property
                    answer = new ImageData().setWidth(width).setHeight(height);
                    // Store data into metadata/per-data node
                    Resource metaData = imageContentResource.getChild(METADATA);
                    if (metaData == null) {
                        Map<String, Object> metadataProperties = new HashMap<>();
                        metadataProperties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
                        metaData = imageContentResource.getResourceResolver().create(imageContentResource, METADATA, metadataProperties);
                        Resource perData = metaData.getChild(PER_DATA);
                        if (perData == null) {
                            Map<String, Object> perDataProperties = new HashMap<>();
                            perDataProperties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
                            perData = imageContentResource.getResourceResolver().create(metaData, PER_DATA, perDataProperties);
                        }
                        ValueMap perDataProperties = perData.getValueMap();
                        perDataProperties.put(IMAGE_WIDTH, width);
                        perDataProperties.put(IMAGE_HEIGHT, height);
                    }
                }
            } catch (IOException e) {
                // Ignore
            }
        }
        return answer;
    }

    private class ImageData {
        private int width;
        private int height;

        public ImageData setWidth(Integer width) {
            this.width = width == null ? 0 : width;
            return this;
        }

        public ImageData setHeight(Integer height) {
            this.height = height == null ? 0 : height;
            return this;
        }
    }

    protected ValueMap getValueMap(Object adaptable) {
        if (adaptable instanceof ValueMap) {
            return (ValueMap) adaptable;
        } else if (adaptable instanceof Adaptable) {
            ValueMap map = ((Adaptable) adaptable).adaptTo(ValueMap.class);
            return map;
        } else {
            return null;
        }
    }

    protected ResourceResolver getResourceResolver(Object adaptable) {
        ResourceResolver resolver = null;
        if (adaptable instanceof Resource) {
            resolver = ((Resource) adaptable).getResourceResolver();
        } else if (adaptable instanceof SlingHttpServletRequest) {
            resolver = ((SlingHttpServletRequest) adaptable).getResourceResolver();
        }
        return resolver;
    }
}
