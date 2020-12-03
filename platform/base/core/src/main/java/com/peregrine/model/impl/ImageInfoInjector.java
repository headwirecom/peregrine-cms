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
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Optional;

import static com.peregrine.commons.util.PerConstants.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + "=" + "2000"
)
public final class ImageInfoInjector implements Injector {

    @Override
    public String getName() {
        return "imageinfo";
    }

    @Override
    public Object getValue(Object adaptable, String name, Type type, AnnotatedElement element,
                           DisposalCallbackRegistry callbackRegistry
    ) {
        if (isNull(adaptable) || type != Dimension.class) {
            return null;
        }

        final Resource resource;
        if (adaptable instanceof Resource) {
            resource = (Resource) adaptable;
        } else if (adaptable instanceof SlingHttpServletRequest) {
            resource = ((SlingHttpServletRequest) adaptable).getResource();
        } else {
            return null;
        }

        final Annotation annotation = element.getDeclaredAnnotation(ImageInfo.class);
        if (isNull(annotation)) {
            return null;
        }

        final BufferedImage image = getImage(resource, ((ImageInfo) annotation).name());
        if (isNull(image)) {
            return null;
        }

        return new Dimension(image.getWidth(), image.getHeight());
    }

    private BufferedImage getImage(Resource resource, String imagePropertyName) {
        return Optional.of(resource)
                .map(Resource::getValueMap)
                .map(map -> map.get(imagePropertyName, String.class))
                .filter(StringUtils::isNotBlank)
                .filter(path -> !StringUtils.contains(path, ":/"))
                .map(resource.getResourceResolver()::getResource)
                .map(r -> r.getChild(JCR_CONTENT))
                .map(this::getImage)
                .orElse(null);
    }

    private BufferedImage getImage(final Resource content) {
        final ValueMap properties = content.getValueMap();
        final String mimeType = properties.get(JCR_MIME_TYPE, String.class);
        // SVG images are not handled by ImageIO so we ignore it here
        if (isBlank(mimeType) || SVG_MIME_TYPE.equals(mimeType)) {
            return null;
        }

        final InputStream stream = properties.get(JCR_DATA, InputStream.class);
        if (isNull(stream)) {
            return null;
        }

        try {
            return ImageIO.read(stream);
        } catch (IOException e) {
            return null;
        }
    }


}
