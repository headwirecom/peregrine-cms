package com.example.site.models;

/*-
 * #%L
 * example site - Core
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

import com.peregrine.nodetypes.models.Container;
import com.peregrine.nodetypes.models.IComponent;
import javax.inject.Inject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/**
 * Created by rr on 4/18/2017.
 */

//[GEN
@Model(
        adaptables = Resource.class,
        resourceType = "example/components/carousel",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        adapters = IComponent.class
)
@Exporter(
        name = "jackson",
        extensions = "json"
)
//GEN]
public class CarouselModel extends Container {

    //[GEN

    public CarouselModel(Resource resource) {
        super(resource);
    }

    @Inject @Default(values = "5000")
    private String interval;

    @Inject @Default(values = "hover")
    private String pause;

    @Inject @Default(values = "true")
    private String ride;

    @Inject @Default(values = "true")
    private String indicators;

    @Inject @Default(values = "true")
    private String controls;

    @Inject @Default(values = "true")
    private String wrap;

    @Inject @Default(values = "true")
    private String keyboard;

    public String getInterval() {
        return interval;
    }

    public String getPause() {
        return pause;
    }

    public String getRide() {
        return ride;
    }

    public String getControls() {
        return controls;
    }

    public String getIndicators() {
        return indicators;
    }

    public String getWrap() {
        return wrap;
    }

    public String getKeyboard() {
        return keyboard;
    }


    //GEN]
}
