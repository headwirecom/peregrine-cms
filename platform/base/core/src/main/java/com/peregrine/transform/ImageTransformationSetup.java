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
 * Each instance of this factory provides a single
 * configuration for a image transformation.
 *
 * This way we can group Image Transformations together
 * and provide Setup specific parameters.
 *
 * This service allows the user to create an Image Transformation
 * that is comprised on one or more Image Transformation through
 * the Image Transformation Configuration with specific parameters
 * if needed.
 *
 * The Image Transformation Configuration Provider is the central
 * access point to these configurations so that the user does not
 * have to deal with all setup services.
 *
 * The Image Transformation Provider is then providing access to the
 * actual Image Transformation Service defined by the transformation
 * name.
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
            description = "Name of the Setup. Cannot be the same as an Image Transformation Service. The extension of this name defines the format of the rendition",
            required = true
        )
        String setupName();

        @AttributeDefinition(
            name = "Path",
            description = "The root path of nodes that are handled with this setup. If empty or not specified then the root is '/'. If provided the path must be absolute",
            required = false
        )
        String path();

        @AttributeDefinition(
            name = "Configuration",
            description = "A list of image transformation configuration executed in the given order. Format: transformation=<image transformation name>[|<property key>=<property value>]*"
        )
        String[] imageTransformationConfigurations();
    }

    private String name;
    private String path;
    private List<ImageTransformationConfiguration> imageTransformationConfigurations = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
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
        String temp = configuration.path();
        path = temp == null || temp.isEmpty() ? "/" : temp;
        if(path.charAt(0) != '/') {
            throw new IllegalArgumentException("Path must be absolute but was: " + path);
        }
        logger.trace("Image Configuration Name: '{}', Path: '{}', Image Transformation Configurations: '{}'", name, path,
            configuration.imageTransformationConfigurations() == null ?
                null : Arrays.asList(configuration.imageTransformationConfigurations())
            );
        // Remove all existing Image Transformation Configuration with the name of this Setup
        imageTransformationConfigurations.removeIf(p -> p.getName().equals(name));
        for(String imageTransformationConfiguration: configuration.imageTransformationConfigurations()) {
            imageTransformationConfigurations.add(new ImageTransformationConfiguration(name, path, imageTransformationConfiguration));
        }
    }

    @Override
    public String toString() {
        return "ImageTransformationSetup{" + "name:'" + name + '\'' + ", imageTransformationConfigurations:" + imageTransformationConfigurations + '}';
    }
}
