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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@Component(service = { DefaultSiteMapExtractor.class })
public final class DefaultSiteMapExtractor extends SiteMapExtractorBase implements SiteMapConfiguration {

    private final Pattern pattern = Pattern.compile("/content/[^/]+/pages(/[^/]+)*");

    @Reference
    private PerPageRecognizer pageRecognizer;

    @Reference
    private DefaultUrlExternalizer urlExternalizer;

    @Reference
    private SiteMapUrlBuilder urlBuilder;

    @Reference
    private LastModPropertyProvider lastModPropertyProvider;

    @Reference
    private ChangeFreqPropertyProvider changeFreqPropertyProvider;

    @Reference
    private PriorityPropertyProvider priorityPropertyProvider;

    @Override
    public SiteMapConfiguration getConfiguration() {
        return this;
    }

    @Override
    public boolean appliesTo(final Resource root) {
        final Pattern pattern = getPagePathPattern();
        if (isNull(pattern)) {
            return true;
        }

        return pattern.matcher(root.getPath()).matches();
    }

    @Override
    public UrlExternalizer getUrlExternalizer() {
        return urlExternalizer;
    }

    @Override
    protected SiteMapUrlBuilder getUrlBuilder() {
        return urlBuilder;
    }

    @Override
    protected Collection<PropertyProvider> getDefaultPropertyProviders() {
        return Arrays.asList(lastModPropertyProvider, changeFreqPropertyProvider, priorityPropertyProvider);
    }

    @Override
    public Pattern getPagePathPattern() {
        return pattern;
    }

    @Override
    public PageRecognizer getPageRecognizer() {
        return pageRecognizer;
    }

    @Override
    public Map<String, String> getXmlNamespaces() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<PropertyProvider> getPropertyProviders() {
        return getDefaultPropertyProviders();
    }

    @Override
    public Set<String> getMandatoryCachedPaths() {
        return Collections.emptySet();
    }

}
