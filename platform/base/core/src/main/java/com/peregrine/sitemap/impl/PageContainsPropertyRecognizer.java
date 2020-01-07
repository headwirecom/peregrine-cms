package com.peregrine.sitemap.impl;

/*-
 * #%L
 * platform base - Core
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

import com.peregrine.commons.Page;
import com.peregrine.sitemap.PageRecognizer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = PageRecognizer.class, immediate = true)
@Designate(ocd = PageContainsPropertyRecognizerConfig.class, factory = true)
public final class PageContainsPropertyRecognizer implements PageRecognizer {

    private PageContainsPropertyRecognizerConfig config;

    @Activate
    public void activate(final PageContainsPropertyRecognizerConfig config) {
        this.config = config;
    }

    @Override
    public final String getName() {
        return config.name();
    }

    public final boolean isPage(final Page candidate) {
        return candidate.containsProperty(config.propertyName());
    }
}
