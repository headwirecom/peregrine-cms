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
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.isPrimaryType;
import static java.util.Objects.isNull;

@Component(service = { DefaultSiteMapExtractor.class })
public final class DefaultSiteMapExtractor extends SiteMapExtractorBase implements SiteMapConfiguration {

    private final Pattern pattern = Pattern.compile(CONTENT_ROOT + "/[^/]+/" + PAGES + "(/[^/]+)*");
    private final Map<String, String> xmlNamespaces = Collections.emptyMap();
    private final List<PropertyProvider> propertyProviders = new LinkedList<>();
    private final List<PropertyProvider> defaultPropertyProviders = new LinkedList<>();

    @Reference
    private PerPageRecognizer pageRecognizer;

    @Reference
    private DefaultUrlExternalizer urlExternalizer;

    @Reference
    private LastModPropertyProvider lastModPropertyProvider;

    @Reference
    private ChangeFreqPropertyProvider changeFreqPropertyProvider;

    @Reference
    private PriorityPropertyProvider priorityPropertyProvider;

    @Reference
    private SiteMapUrlBuilder urlBuilder;

    @Reference
    private ResourceResolverFactoryProxy resolverFactory;

    @Activate
    public void activate() {
        propertyProviders.add(lastModPropertyProvider);
        propertyProviders.add(changeFreqPropertyProvider);
        propertyProviders.add(priorityPropertyProvider);
    }

    @Override
    public SiteMapConfiguration getConfiguration() {
        return this;
    }

    @Override
    public boolean appliesTo(final Resource root) {
        return super.appliesTo(root) && Optional.ofNullable(root)
                .map(Resource::getPath)
                .map(p -> StringUtils.isNotBlank(urlExternalizer.getPrefix(root.getResourceResolver(), p)))
                .orElse(false);
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
    public UrlExternalizer getUrlExternalizer() {
        return urlExternalizer;
    }

    @Override
    public Map<String, String> getXmlNamespaces() {
        return xmlNamespaces;
    }

    @Override
    public Collection<PropertyProvider> getPropertyProviders() {
        return propertyProviders;
    }

    @Override
    protected Collection<PropertyProvider> getDefaultPropertyProviders() {
        return defaultPropertyProviders;
    }

    @Override
    public Set<String> getMandatoryCachedPaths() {
        try (final ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver()) {
            final Resource content = resourceResolver.getResource(CONTENT_ROOT);
            if (isNull(content)) {
                return Collections.emptySet();
            }

            return StreamSupport.stream(content.getChildren().spliterator(), false)
                    .filter(r -> isPrimaryType(r, SITE_PRIMARY_TYPE))
                    .map(r -> r.getChild(PAGES))
                    .filter(this::appliesTo)
                    .map(Resource::getPath)
                    .collect(Collectors.toSet());
        } catch (final LoginException e) {
            return Collections.emptySet();
        }
    }

    @Override
    protected SiteMapUrlBuilder getUrlBuilder() {
        return urlBuilder;
    }

}
