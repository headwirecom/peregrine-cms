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
 * Created by Andreas Schaefer on 5/22/17.
 */
public class ImageTransformationConfiguration {
    private String name;
    private String transformationName;
    private Map<String, String> parameters = new HashMap<>();

    public ImageTransformationConfiguration(String format) {
        int index = format.indexOf('|');
        if(index <= 0 || index == format.length() - 1) {
            throw new IllegalArgumentException("Rendition Type format has no name. Configuration must start with '<name>|'");
        }
        String name = format.substring(0, index);
        String configuration = format.substring(index + 1);
        init(name, configuration);
    }

    public ImageTransformationConfiguration(String name, String configuration) {
        init(name, configuration);
    }

    private void init(String name, String configuration) {
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Image Configuration's name is not provided, configuration: '" + configuration);
        }
        this.name = name;
        String[] tokens = configuration.split("\\|");
        if(tokens.length == 0) {
            throw new IllegalArgumentException("Image Configuration has no entries, configuration: '" + configuration + "'. " + getConfigurationFormat());
        }
        String temp = tokens[0];
        this.transformationName = temp;
        if(temp == null || temp.isEmpty()) {
            throw new IllegalArgumentException("Rendition Type format's transformation name is not provided, configuration: '" + configuration + "'. " + getConfigurationFormat());
        } else {
            int index = temp.indexOf('=');
            if(index <= 0 || index >= temp.length() - 1) {
                throw new IllegalArgumentException("Rendition Type format's transformation name is not of type transformation=value , configuration: '" + configuration + "'. " + getConfigurationFormat());
            } else {
                String propertyKey = temp.substring(0, index);
                String propertyValue = temp.substring(index + 1);
                if(!"transformation".equals(propertyKey)) {
                    throw new IllegalArgumentException("Rendition Type format's transformation name does not start with 'transformation' , configuration: '" + configuration + "'. " + getConfigurationFormat());
                } else if(propertyValue.isEmpty()) {
                    throw new IllegalArgumentException("Rendition Type format's transformation name value is not provided , configuration: '" + configuration + "'. " + getConfigurationFormat());
                } else {
                    this.transformationName = propertyValue;
                }
            }
        }
        for(int i = 1; i < tokens.length; i++) {
            String value = tokens[i];
            if(value == null || value.isEmpty()) {
                throw new IllegalArgumentException("Rendition Type format's contains empty token, configuration: '" + configuration + "' on position: " + (i + 1) + "). " + getConfigurationFormat());
            }
            int index = value.indexOf('=');
            if(index <= 0 || index >= value.length() - 1) {
                throw new IllegalArgumentException("Rendition Type format's token is not of type key=value , configuration: '" + configuration + "' on position: " + (i + 1) + "). " + getConfigurationFormat());
            }
            parameters.put(value.substring(0, index), value.substring(index + 1));
        }
    }

    public String getName() {
        return name;
    }

    public String getTransformationName() {
        return transformationName;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    private String getConfigurationFormat() {
        return "Expected Configuration Format: transformation=<transformation name>|<a | separated list of <parameter>=<value>>";
    }

    @Override
    public String toString() {
        return "ImageTransformationConfiguration[name: " + name + ", transformation name: " + transformationName + ", parameters: " + parameters + "]";
    }
}
