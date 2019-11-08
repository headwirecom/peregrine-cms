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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import static java.util.Objects.nonNull;

@Component(service = DefaultSiteMapExtractor.class)
public final class DefaultSiteMapExtractor extends SiteMapExtractorBase {

    @Reference
    private SiteMapUrlBuilder urlBuilder;

    @Reference
    private PerPageRecognizer perPageRecognizer;

    @Reference
    private EtcMapUrlExternalizer etcMapUrlExternalizer;

    @Reference
    private LastModPropertyProvider lastModPropertyProvider;

    @Reference
    private ChangeFreqPropertyProvider changeFreqPropertyProvider;

    @Reference
    private PriorityPropertyProvider priorityPropertyProvider;

    @Activate
    public void activate() {
        pageRecognizer = perPageRecognizer;
        urlExternalizer = etcMapUrlExternalizer;
        addPropertyProvider(lastModPropertyProvider);
        addPropertyProvider(changeFreqPropertyProvider);
        addPropertyProvider(priorityPropertyProvider);
    }

    @Deactivate
    public void deactivate() {
        clear();
    }

    public SiteMapUrlBuilder getUrlBuilder() {
        return urlBuilder;
    }

    @Override
    public boolean appliesTo(final Resource root) {
        return nonNull(root);
    }

    public PageRecognizer getPageRecognizer() {
        return perPageRecognizer;
    }

    public UrlExternalizer getUrlExternalizer() {
        return etcMapUrlExternalizer;
    }

    public PropertyProvider getLastModPropertyProvider() {
        return lastModPropertyProvider;
    }

    public PropertyProvider getChangeFreqPropertyProvider() {
        return changeFreqPropertyProvider;
    }

    public PropertyProvider getPriorityPropertyProvider() {
        return priorityPropertyProvider;
    }
}
