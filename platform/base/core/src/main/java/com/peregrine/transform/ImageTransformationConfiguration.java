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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This configuration contains the name of the transformation
 * and its parameters of a single image transformation.
 *
 * Created by Andreas Schaefer on 5/22/17.
 */
public class ImageTransformationConfiguration {

    public static final String IMAGE_CONFIGURATION_NAME_IS_NOT_PROVIDED = "Image Configuration's name is not provided, configuration: '";
    public static final String IMAGE_CONFIGURATION_HAS_NO_ENTRIES = "Image Configuration has no entries, configuration: '";
    public static final String RENDITION_TYPE_FORMAT_TRANSFORMATION_NAME_IS_NOT_PROVIDED = "Rendition Type format's transformation name is not provided, configuration: '";
    public static final String RENDITION_TYPE_FORMAT_TRANSFORMATION_NAME_IS_NOT_OF_TYPE_TRANSFORMATION = "Rendition Type format's transformation name is not of type transformation=value , configuration: '";
    public static final String RENDITION_TYPE_FORMAT_TRANSFORMATION_NAME_DOES_NOT_START_WITH_TRANSFORMATION = "Rendition Type format's transformation name does not start with 'transformation' , configuration: '";
    public static final String RENDITION_TYPE_FORMAT_TRANSFORMATION_NAME_VALUE_IS_NOT_PROVIDED = "Rendition Type format's transformation name value is not provided , configuration: '";
    public static final String RENDITION_TYPE_FORMAT_CONTAINS_EMPTY_TOKEN = "Rendition Type format's contains empty token, configuration: '";
    public static final String RENDITION_TYPE_FORMAT_TOKEN_IS_NOT_OF_TYPE_KEY_VALUE = "Rendition Type format's token is not of type key=value , configuration: '%s' on position: %s: Format: '%s'";
    public static final String EXPECTED_CONFIGURATION_FORMAT_FAILURE = "Expected Configuration Format: transformation=<transformation name>|<a | separated list of <parameter>=<value>>: '%s' on position: %s. Format; '%s'";

    /** Name of the Image Transformation Setup this configuration belongs to **/
    private String name;
    /** Path this transformation applies to **/
    private String path;
    /** Name of the Image Transformation this configuration configures **/
    private String transformationName;
    /** Optional parameters for the Image Transformation **/
    private Map<String, String> parameters = new HashMap<>();

    public ImageTransformationConfiguration(String name, String path, String configuration) {
        init(name, path, configuration);
    }

    private void init(String name, String path, String configuration) {
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException(IMAGE_CONFIGURATION_NAME_IS_NOT_PROVIDED + configuration);
        }
        this.name = name;
        this.path = path == null || path.isEmpty() ? "/" : path;
        String[] tokens = configuration.split("\\|");
        if(tokens.length == 0) {
            throw new IllegalArgumentException(IMAGE_CONFIGURATION_HAS_NO_ENTRIES + configuration + "'. " + getConfigurationFormat());
        }
        String temp = tokens[0];
        if(temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException(RENDITION_TYPE_FORMAT_TRANSFORMATION_NAME_IS_NOT_PROVIDED + configuration + "'. " + getConfigurationFormat());
        } else {
            int index = temp.indexOf('=');
            if(index <= 0 || index >= temp.length() - 1) {
                throw new IllegalArgumentException(RENDITION_TYPE_FORMAT_TRANSFORMATION_NAME_IS_NOT_OF_TYPE_TRANSFORMATION + configuration + "'. " + getConfigurationFormat());
            } else {
                String propertyKey = temp.substring(0, index);
                String propertyValue = temp.substring(index + 1);
                if(!"transformation".equals(propertyKey)) {
                    throw new IllegalArgumentException(RENDITION_TYPE_FORMAT_TRANSFORMATION_NAME_DOES_NOT_START_WITH_TRANSFORMATION + configuration + "'. " + getConfigurationFormat());
                } else if(propertyValue.isEmpty()) {
                    throw new IllegalArgumentException(RENDITION_TYPE_FORMAT_TRANSFORMATION_NAME_VALUE_IS_NOT_PROVIDED + configuration + "'. " + getConfigurationFormat());
                } else {
                    this.transformationName = propertyValue;
                }
            }
        }
        for(int i = 1; i < tokens.length; i++) {
            String value = tokens[i];
            if(value == null || value.isEmpty()) {
                throw new IllegalArgumentException(String.format(RENDITION_TYPE_FORMAT_CONTAINS_EMPTY_TOKEN, configuration, (i + 1), getConfigurationFormat()));
            }
            int index = value.indexOf('=');
            if(index <= 0 || index >= value.length() - 1) {
                throw new IllegalArgumentException(String.format(RENDITION_TYPE_FORMAT_TOKEN_IS_NOT_OF_TYPE_KEY_VALUE, configuration, (i + 1), getConfigurationFormat()));
            }
            parameters.put(value.substring(0, index), value.substring(index + 1));
        }
    }

    /** @return Name of the Image Transformation Setup this Configuration belongs to **/
    public String getName() {
        return name;
    }

    /** @return Path of the Image Transformation Setup this Configuration is applicable on **/
    public String getPath() { return path; }
    /** @return Name of the Image Transformation this configuration configures **/
    public String getTransformationName() {
        return transformationName;
    }

    /**
     * @return Parameters of the Image Transformation which can be empt if there are none 
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /** 
     * @param name Name of parameter
     * @return Single Parameter denoted by the given parameter name. Can be null if not found 
     */
    public String getParameter(String name) {
        return parameters.get(name);
    }

    /** @return Description of the Configuration Format **/
    private String getConfigurationFormat() {
        return EXPECTED_CONFIGURATION_FORMAT_FAILURE;
    }

    @Override
    public String toString() {
        return "ImageTransformationConfiguration[name: " + name + ", path: " + path + ", transformation name: " + transformationName + ", parameters: " + parameters + "]";
    }
}
