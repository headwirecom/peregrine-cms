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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * This service is a central access point to the Image Transformation
 * Configuration provided by all Image Transformation Setups.
 *
 * Note: this service is listening to ITS registrations but could also listen
 * to ITC registrations. For now they are equivalent but if we allow for
 * other ITC creations then we would need to listen to the later.
 *
 * Created by Andreas Schaefer on 5/22/17.
 */
@Component(
    service = ImageTransformationConfigurationProvider.class,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Image Transformation Configuration Provider",
        SERVICE_VENDOR + EQUAL + PER_VENDOR
    }
)
public class ImageTransformationConfigurationProvider {

    @Reference
    private ImageTransformationProvider imageTransformationProvider;

    private final Logger logger = LoggerFactory.getLogger(getClass());

//    private final Map<String, List<ImageTransformationConfiguration>> imageTransformationSetups = new HashMap<String, List<ImageTransformationConfiguration>>();
//    private final List<ImageTransformationConfiguration> imageTransformationSetups = new ArrayList<>();
    private List<ImageTransformationSetup> imageTransformationSetupList = new ArrayList<>();

//    public boolean isImageTransformationConfigurationEnabled(String name, String path) {
//        boolean answer = false;
//        List<ImageTransformationConfiguration> imageTransformationConfigurationList = imageTransformationSetups.stream()
//            .filter(p -> p.getName().equals(name) && path.startsWith(p.getPath()))
//            .collect(Collectors.toList());
//        if(!imageTransformationConfigurationList.isEmpty()) {
//            boolean ok = true;
//            for(ImageTransformationConfiguration configuration: imageTransformationConfigurationList) {
//                ImageTransformation imageTransformation = imageTransformationProvider.getImageTransformation(configuration.getTransformationName());
//                if(imageTransformation == null) {
//                    ok = false;
//                    break;
//                } else {
//                    if(!imageTransformation.isValid()) {
//                        ok = false;
//                        break;
//                    }
//                }
//            }
//            answer = ok;
//        }
//        return answer;
//    }

    /**
     * Provides the Image Transformation Configurations for a given Setup
     * @param name Image Transformation Setup name
     * @param path Path to configuration
     * @return List of Image Transformation Configurations that belong to that Setup Name. If name is empty or no setup
     *         found the <code>null</code> is returned
     */
    public List<ImageTransformationConfiguration> getImageTransformationConfigurations(String name, String path) {
        List<ImageTransformationConfiguration> answer = null;
        if(isNotEmpty(name)) {
            logger.trace("Obtain Image Transformation Configuration with Name: '{}', with path: '{}'", name, path);
            // Find the best matching
            Optional<ImageTransformationSetup> setup = imageTransformationSetupList.stream()
                .filter(p -> p.getName().equals(name) && path.startsWith(p.getPath()))
                .sorted((e1, e2) -> e1.getPath().length() > e2.getPath().length() ? -1 : 1)
                .findFirst();
            if(setup.isPresent()) {
                answer = setup.get().getImageTransformationConfigurations()
                    .stream()
                    .filter(p ->
                        {
                            ImageTransformation imageTransformation = imageTransformationProvider.getImageTransformation(p.getTransformationName());
                            return imageTransformation != null && imageTransformation.isEnabled();
                        }
                    )
                    .collect(Collectors.toList());
            }
            logger.trace("Applicable Image Transformation Setup Keys: '{}'", answer);
        } else {
            logger.warn("Tried to obtain an Image Transformation Setup with an empty name");
        }
        return answer;
    }

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    void bindImageTransformationConfiguration(ImageTransformationSetup imageTransformationSetup) {
        imageTransformationSetupList.add(imageTransformationSetup);
        logger.info("Image Transformation Setup added '{}', Image Transformation Configurations: '{}'", imageTransformationSetup.getName(), imageTransformationSetup.getImageTransformationConfigurations());
    }

    @SuppressWarnings("unused")
    void unbindImageTransformationConfiguration(ImageTransformationSetup imageTransformationSetup) {
        imageTransformationSetupList.remove(imageTransformationSetup);
        logger.info("Image Transformation Setup removed, name '{}', path: '{}'", imageTransformationSetup.getName(), imageTransformationSetup.getPath());
    }
}
