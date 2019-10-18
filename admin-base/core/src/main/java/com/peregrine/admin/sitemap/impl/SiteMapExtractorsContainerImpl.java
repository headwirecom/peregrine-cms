package com.peregrine.admin.sitemap.impl;

/*-
 * #%L
 * admin base - Core
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

import com.peregrine.admin.sitemap.SiteMapExtractor;
import com.peregrine.admin.sitemap.SiteMapExtractorsContainer;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component(service = SiteMapExtractorsContainer.class)
public final class SiteMapExtractorsContainerImpl implements SiteMapExtractorsContainer {

    private final ConcurrentLinkedQueue<SiteMapExtractor> items = new ConcurrentLinkedQueue<>();

    @Override
    public boolean add(final SiteMapExtractor item) {
        return items.add(item);
    }

    @Override
    public boolean remove(final SiteMapExtractor item) {
        return items.remove(item);
    }

    @Override
    public SiteMapExtractor findFirstFor(final Resource resource) {
        for (final SiteMapExtractor extractor : items) {
            if (extractor.appliesTo(resource)) {
                return extractor;
            }
        }

        return null;
    }
}
