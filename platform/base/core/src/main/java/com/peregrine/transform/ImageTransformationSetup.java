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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: There is something wrong with the Configuration Name -> check if needed and if not remove it (transformation seems wrong).
 * 
 * Created by Andreas Schaefer on 5/22/17.
 */
@Component(
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service = ImageTransformationSetup.class
)
@Designate(ocd = ImageTransformationSetup.Configuration.class, factory = true)
public class ImageTransformationSetup {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ObjectClassDefinition(
        name = "Peregrine: Image Transformation Setup",
        description = "Each instance provides a detailed configuration of the Image Transformation which can be used to chain Image Transformations"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Name",
            description = "Name of the Setup. Cannot be the same as an Image Transformation Service",
            required = true
        )
        String setupName();

        @AttributeDefinition(
            name = "Configuration",
            description = "A list of image transformation configuration executed in the given order. Format: transformation=<image transformation name>[|<property key>=<property value>]*"
        )
        String[] imageTransformationConfigurations();
    }

    private String name;
    private List<ImageTransformationConfiguration> imageTransformationConfigurations = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<ImageTransformationConfiguration> getImageTransformationConfigurations() {
        return imageTransformationConfigurations;
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) {
        setup(configuration);
    }

    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) {
        setup(configuration);
    }

    private void setup(Configuration configuration) {
        name = configuration.setupName();
        logger.trace("Image Configuration Name: '{}', Image Transformation Configurations: '{}'", name,
            configuration.imageTransformationConfigurations() == null ?
                null : Arrays.asList(configuration.imageTransformationConfigurations())
            );
        for(String imageTransformationConfiguration: configuration.imageTransformationConfigurations()) {
            imageTransformationConfigurations.add(new ImageTransformationConfiguration(name, imageTransformationConfiguration));
        }
    }

    @Override
    public String toString() {
        return "ImageTransformationSetup{" + "name:'" + name + '\'' + ", imageTransformationConfigurations:" + imageTransformationConfigurations + '}';
    }
}
