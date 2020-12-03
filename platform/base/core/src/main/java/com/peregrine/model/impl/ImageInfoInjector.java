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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Optional;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerUtil.IMAGE_HEIGHT;
import static com.peregrine.commons.util.PerUtil.IMAGE_WIDTH;
import static com.peregrine.commons.util.PerUtil.METADATA;
import static com.peregrine.commons.util.PerUtil.PER_DATA;
import static java.util.Objects.isNull;

@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + "=" + "2000"
)
public final class ImageInfoInjector implements Injector {

    private static final String IMAGE_INFO_FORMAT = "{'width': %1$d, 'height': %2$d}";

    @Override
    public String getName() {
        return "imageinfo";
    }

    @Override
    public Object getValue(Object adaptable, String name, Type type, AnnotatedElement element,
            DisposalCallbackRegistry callbackRegistry
    ) {
        if (isNull(adaptable) || type != String.class) {
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

        final ValueMap map = getImagePerData(resource, ((ImageInfo) annotation).name());
        int width = map.get(IMAGE_WIDTH, 0);
        int height = map.get(IMAGE_HEIGHT, 0);
        if (width > 0 || height > 0) {
            return String.format(IMAGE_INFO_FORMAT, width, height);
        }

        return null;
    }

    private ValueMap getImagePerData(Resource resource, String imagePropertyName) {
        return Optional.of(resource)
                .map(Resource::getValueMap)
                .map(map -> map.get(imagePropertyName, String.class))
                .filter(StringUtils::isNotBlank)
                .filter(path -> !StringUtils.contains(path, ":/"))
                .map(resource.getResourceResolver()::getResource)
                .map(r -> r.getChild(JCR_CONTENT))
                .map(r -> r.getChild(METADATA))
                .map(r -> r.getChild(PER_DATA))
                .map(Resource::getValueMap)
                .orElse(ValueMap.EMPTY);
    }

}
