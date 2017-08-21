package com.peregrine.admin.replication.impl;

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

import com.peregrine.admin.replication.ImageMetadataSelector;
import com.peregrine.commons.util.PerUtil;
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
import java.util.List;

/**
 * Created by schaefa on 5/25/17.
 */
@Component(
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service = ImageMetadataSelector.class,
    immediate = true
)
@Designate(ocd = ImageMetadataSelectorService.Configuration.class, factory = true)
public class ImageMetadataSelectorService
    implements ImageMetadataSelector
{
    @ObjectClassDefinition(
        name = "Peregrine: Image Metadata Selector Service",
        description = "Each instance provides the Selection of the Metadata imported of Images"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Category",
            description = "Metadata Category Name",
            required = true
        )
        String category();
        @AttributeDefinition(
            name = "Image Directory",
            description = "If provided this is the name of the Category from the Image. This will provide a mapping of the Category Name",
            required = false
        )
        String imageDirectory();
        @AttributeDefinition(
            name = "Included",
            description = "If true then the given List is included and all others are ignored. If false then the given list is excluded and all others included",
            required = true
        )
        boolean included() default false;
        @AttributeDefinition(
            name = "Selection List",
            description = "List of all Tag Names that are either included or excluded",
            required = true
        )
        String[] selection();
        @AttributeDefinition(
            name = "JSON",
            description = "If true al properties are included as JSon property named 'raw_tags'",
            required = true
        )
        boolean json() default false;
        @AttributeDefinition(
            name = "Mapping",
            description = "List of Tag Name Mappings in the format of <adjusted name>=<mapped name>. Every Tag mapped here is always included",
            required = true
        )
        String[] mapping();
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) { setup(configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) { setup(configuration); }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String name;
    private Transformation category;
    private boolean included;
    private List<String> selection = new ArrayList<>();
    private boolean json;
    private List<Transformation> mappings = new ArrayList<>();

    private void setup(Configuration configuration) {
        name = configuration.category();
        category = configuration.imageDirectory() == null ?
            new Transformation(configuration.category(), null ) :
            new Transformation(configuration.imageDirectory(), configuration.category());
        included = configuration.included();
        selection.clear();
        for(String item: configuration.selection()) {
            selection.add(item.trim());
        }
        json = configuration.json();
        mappings.clear();
        for(String item: configuration.mapping()) {
            String[] tokens = item.split("=");
            if(tokens.length == 2) {
                mappings.add(new Transformation(tokens[0].trim(), tokens[1].trim()));
            } else {
                log.warn("Ignore bad mapping: '{}' (wrong format)", item);
            }
        }
    }

    @Override
    public boolean asJsonProperty() {
        return json;
    }

    @Override
    public String acceptCategory(String category) {
        return this.category.accept(category) ?
            this.category.getDestination() :
            null;
    }

    @Override
    public String acceptTag(String tag) {
        for(Transformation transformation: mappings) {
            if(transformation.accept(tag)) {
                return transformation.getDestination();
            }
        }
        boolean accept = false;
        String test = PerUtil.adjustMetadataName(tag);
        if(included) {
            for(String item: selection) {
                if(item.equals(test)) {
                    accept = true;
                    break;
                }
            }
        } else {
            accept = true;
            for(String item: selection) {
                if(item.equals(test)) {
                    accept = false;
                    break;
                }
            }
        }
        return accept ? test : null;
    }

    private static class Transformation {
        private String source;
        private String destination;

        public Transformation(String source, String destination) {
            if(source == null || source.isEmpty()) {
                throw new IllegalArgumentException("Source must be provide");
            }
            this.source = PerUtil.adjustMetadataName(source);
            // If no destination then we just return the adjusted source
            this.destination = destination == null ?
                this.source :
                PerUtil.adjustMetadataName(destination);
        }

        public boolean accept(String name) {
            return source.equals(PerUtil.adjustMetadataName(name));
        }

        public String getDestination() {
            return destination;
        }
    }
}
