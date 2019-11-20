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

import com.peregrine.sitemap.PropertyProvider;
import com.peregrine.sitemap.SiteMapConfiguration;
import com.peregrine.sitemap.SiteMapExtractorBase;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import org.apache.sling.api.resource.Resource;

import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public final class SiteMapExtractorImpl extends SiteMapExtractorBase {

    private final Pattern pattern;

    private final DefaultSiteMapExtractor defaultSiteMapExtractor;

    public SiteMapExtractorImpl(final SiteMapConfiguration config, final DefaultSiteMapExtractor defaultSiteMapExtractor) {
        pattern = config.getPagePathPattern();
        this.defaultSiteMapExtractor = defaultSiteMapExtractor;
        pageRecognizer = config.getPageRecognizer();
        urlExternalizer = config.getUrlExternalizer();
        if (isNull(urlExternalizer)) {
            urlExternalizer = defaultSiteMapExtractor.getUrlExternalizer();
        }

        for (final PropertyProvider provider : config.getPropertyProviders()) {
            addPropertyProvider(provider);
        }

        addPropertyProvider(defaultSiteMapExtractor.getLastModPropertyProvider());
        addPropertyProvider(defaultSiteMapExtractor.getChangeFreqPropertyProvider());
        addPropertyProvider(defaultSiteMapExtractor.getPriorityPropertyProvider());
    }

    protected SiteMapUrlBuilder getUrlBuilder() {
        return defaultSiteMapExtractor.getUrlBuilder();
    }

    @Override
    public boolean appliesTo(final Resource root) {
        return pattern.matcher(root.getPath()).matches();
    }

}
