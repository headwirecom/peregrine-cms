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

import com.peregrine.sitemap.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class SiteMapExtractorImpl extends SiteMapExtractorBase {

    private final SiteMapConfiguration configuration;
    private final SiteMapUrlBuilder urlBuilder;
    private final UrlExternalizer urlExternalizer;
    private final List<PropertyProvider> propertyProviders;

    public SiteMapExtractorImpl(final SiteMapConfiguration configuration,
                                final SiteMapUrlBuilder urlBuilder,
                                final UrlExternalizer urlExternalizer,
                                final PropertyProvider... propertyProviders) {
        this.configuration = configuration;
        this.urlBuilder = urlBuilder;
        this.urlExternalizer = urlExternalizer;
        this.propertyProviders = Arrays.asList(propertyProviders);
    }

    @Override
    public SiteMapConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected SiteMapUrlBuilder getUrlBuilder() {
        return urlBuilder;
    }

    @Override
    protected UrlExternalizer getUrlExternalizer() {
        return Optional.ofNullable(super.getUrlExternalizer())
                .orElse(urlExternalizer);
    }

    @Override
    protected Iterable<? extends PropertyProvider> getDefaultPropertyProviders() {
        return propertyProviders;
    }

}
