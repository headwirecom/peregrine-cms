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

import com.peregrine.sitemap.ConfigurationFactoryContainerBase;
import com.peregrine.sitemap.SiteMapConfiguration;
import com.peregrine.sitemap.SiteMapConfigurationsContainer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;
import java.util.HashSet;

@Component(service = { SiteMapConfigurationsContainer.class })
public final class SiteMapConfigurationsContainerImpl extends ConfigurationFactoryContainerBase<SiteMapConfiguration>
        implements SiteMapConfigurationsContainer {

    @Reference
    private SiteMapExtractorsContainerImpl extractors;

    @Reference
    private DefaultSiteMapExtractor defaultSiteMapExtractor;

    protected void addImpl(final SiteMapConfiguration item) {
        extractors.add(item);
    }

    protected void removeImpl(final SiteMapConfiguration item) {
        extractors.remove(item);
    }

    @Override
    public Collection<SiteMapConfiguration> getAll() {
        final HashSet<SiteMapConfiguration> result = new HashSet<>(items);
        result.add(defaultSiteMapExtractor);
        return result;
    }
}
