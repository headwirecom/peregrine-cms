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
import com.peregrine.sitemap.SiteMapUrlBuilder;
import com.peregrine.sitemap.UrlExternalizer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = SiteMapExtractorDefaults.class)
public final class SiteMapExtractorDefaults {

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

    public SiteMapUrlBuilder getUrlBuilder() {
        return urlBuilder;
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
