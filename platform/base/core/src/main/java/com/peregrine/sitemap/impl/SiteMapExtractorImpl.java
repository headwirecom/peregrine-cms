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
import org.apache.sling.api.resource.Resource;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public final class SiteMapExtractorImpl extends SiteMapExtractorBase {

    private final SiteMapExtractorDefaults siteMapExtractorDefaults;

    public SiteMapExtractorImpl(final SiteMapConfiguration config, final SiteMapExtractorDefaults siteMapExtractorDefaults) {
        super(config);
        this.siteMapExtractorDefaults = siteMapExtractorDefaults;
    }

    protected SiteMapUrlBuilder getUrlBuilder() {
        return siteMapExtractorDefaults.getUrlBuilder();
    }

    protected UrlExternalizer getExternalizer() {
        final UrlExternalizer externalizer = super.getExternalizer();
        if (isNull(externalizer)) {
            return siteMapExtractorDefaults.getUrlExternalizer();
        }

        return externalizer;
    }

    @Override
    public boolean appliesTo(final Resource root) {
        final Pattern pattern = configuration.getPagePathPattern();
        if (isNull(pattern)) {
            return true;
        }

        return pattern.matcher(root.getPath()).matches();
    }

    protected Iterable<? extends PropertyProvider> getDefaultPropertyProviders() {
        final List<PropertyProvider> result = new LinkedList<>();
        result.add(siteMapExtractorDefaults.getLastModPropertyProvider());
        result.add(siteMapExtractorDefaults.getChangeFreqPropertyProvider());
        result.add(siteMapExtractorDefaults.getPriorityPropertyProvider());
        return result;
    }

}
