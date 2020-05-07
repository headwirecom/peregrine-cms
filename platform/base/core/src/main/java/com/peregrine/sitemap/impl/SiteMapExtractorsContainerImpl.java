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

import com.peregrine.sitemap.SiteMapConfiguration;
import com.peregrine.sitemap.SiteMapExtractor;
import com.peregrine.sitemap.SiteMapExtractorsContainer;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component(service = { SiteMapExtractorsContainer.class, SiteMapExtractorsContainerImpl.class })
public final class SiteMapExtractorsContainerImpl implements SiteMapExtractorsContainer {

    private final Map<SiteMapConfiguration, SiteMapExtractorImpl> items = new HashMap<>();

    @Reference
    private SiteMapUrlBuilder urlBuilder;

    @Reference
    private EtcMapUrlExternalizer etcMapUrlExternalizer;

    @Reference
    private LastModPropertyProvider lastModPropertyProvider;

    @Reference
    private ChangeFreqPropertyProvider changeFreqPropertyProvider;

    @Reference
    private PriorityPropertyProvider priorityPropertyProvider;

    @Reference
    private DefaultSiteMapExtractor defaultSiteMapExtractor;

    public boolean add(final SiteMapConfiguration config) {
        if (isNull(config.getPagePathPattern())) {
            return false;
        }

        items.put(config, new SiteMapExtractorImpl(
                config,
                urlBuilder,
                etcMapUrlExternalizer,
                lastModPropertyProvider,
                changeFreqPropertyProvider,
                priorityPropertyProvider)
        );
        return true;
    }

    public boolean remove(final SiteMapConfiguration config) {
        return nonNull(items.remove(config));
    }

    @Override
    public SiteMapExtractor findFirstFor(final Resource resource) {
        for (final SiteMapExtractor extractor : items.values()) {
            if (extractor.appliesTo(resource)) {
                return extractor;
            }
        }

        return Optional.ofNullable(defaultSiteMapExtractor)
                .filter(e -> e.appliesTo(resource))
                .orElse(null);
    }
}
