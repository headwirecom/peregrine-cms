package com.peregrine.admin.transform;

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

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schaefa on 5/22/17.
 */
@Component(
    service = ImageTransformationConfigurationProvider.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Peregrine Image Transformation Configuration Provider",
        Constants.SERVICE_VENDOR + "=headwire.com, Inc"
    }
)
public class ImageTransformationConfigurationProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, List<ImageTransformationConfiguration>> imageTransformationSetups = new HashMap<String, List<ImageTransformationConfiguration>>();

    public List<ImageTransformationConfiguration> getImageTransformationConfigurations(String name) {
        return imageTransformationSetups.get(name);
    }

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    void bindImageTransformationConfiguration(ImageTransformationSetup imageTransformationSetup) {
        imageTransformationSetups.put(imageTransformationSetup.getName(), imageTransformationSetup.getImageTransformationConfigurations());
        log.info("Image Transformation Setup added '{}'", imageTransformationSetup);
    }

    @SuppressWarnings("unused")
    void unbindImageTransformationConfiguration(ImageTransformationSetup imageTransformationSetup) {
        imageTransformationSetups.remove(imageTransformationSetup);
        log.info("Image Transformation Setup removed '{}'", imageTransformationSetup);
    }
}
