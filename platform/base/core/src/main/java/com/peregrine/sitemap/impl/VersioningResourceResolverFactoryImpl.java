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

import com.peregrine.sitemap.VersioningResourceResolverFactory;
import com.peregrine.versions.VersioningResourceResolver;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.PUBLISHED_LABEL;

@Component(service = VersioningResourceResolverFactory.class)
public final class VersioningResourceResolverFactoryImpl implements VersioningResourceResolverFactory {

    private static final String SITE_MAPS_SUB_SERVICE = "sitemaps";
    private static final Map<String, Object> AUTHENTICATION_INFO = new HashMap<>();

    static {
        AUTHENTICATION_INFO.put(ResourceResolverFactory.SUBSERVICE, SITE_MAPS_SUB_SERVICE);
    }

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public VersioningResourceResolver createResourceResolver() throws LoginException {
        final ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(AUTHENTICATION_INFO);
        return new VersioningResourceResolver(resolver, PUBLISHED_LABEL);
    }

}