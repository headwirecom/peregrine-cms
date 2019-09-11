package com.peregrine.transform;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Provider of all available Image Transformations
 * which can be found by its (transformation) name
 *
 * ATTENTION: It is important that Image Transformations
 * do provide a unique transformation name.
 */
@Component(
    service = ImageTransformationProvider.class,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Image Transformation Service Provider",
        SERVICE_VENDOR + EQUAL + PER_VENDOR
    }
)
public class ImageTransformationProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Map of all registered Image Transformation by their name **/
    private Map<String, ImageTransformation> imageTransformations = new HashMap<>();

    /**
     * Provides the Image Transformation
     * @param transformationName Name of the Image Transformation
     * @return Image Transformation if found otherwise null
     */
    public ImageTransformation getImageTransformation(String transformationName) {
        return imageTransformations.get(transformationName);
    }

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    public void bindImageTransformation(ImageTransformation imageTransformation) {
        String transformationName = imageTransformation.getTransformationName();
        if(transformationName != null && !transformationName.isEmpty()) {
            imageTransformations.put(transformationName, imageTransformation);
        } else {
            log.error("Image Transformation: '{}' does not provide an operation name -> binding is ignored", imageTransformation);
        }
    }

    @SuppressWarnings("unused")
    public void unbindImageTransformation(ImageTransformation imageTransformation) {
        String transformationName = imageTransformation.getTransformationName();
        if(imageTransformations.containsKey(transformationName)) {
            imageTransformations.remove(transformationName);
        } else {
            log.error("Image Transformation: '{}' is not register with operation name: '{}' -> unbinding is ignored", imageTransformation, transformationName);
        }
    }
}
