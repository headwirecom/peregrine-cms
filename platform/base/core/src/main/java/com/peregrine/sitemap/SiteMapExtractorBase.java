package com.peregrine.sitemap;

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

import com.peregrine.commons.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public abstract class SiteMapExtractorBase implements SiteMapExtractor {

    protected final SiteMapConfiguration configuration;

    protected SiteMapExtractorBase(final SiteMapConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SiteMapConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public List<SiteMapEntry> extract(final Resource root) {
        return extract(new Page(root));
    }

    private List<SiteMapEntry> extract(final Page root) {
        final List<SiteMapEntry> result = new LinkedList<>();
        if (!isPage(root)) {
            return result;
        }

        result.add(createEntry(root));
        for (final Resource child: root.getChildren()) {
            final Page childPage = new Page(child);
            if (isPage(childPage)) {
                result.addAll(extract(childPage));
            }
        }

        return result;
    }

    private boolean isPage(final Page page) {
        final PageRecognizer recognizer = configuration.getPageRecognizer();
        return isNull(recognizer) || recognizer.isPage(page);
    }

    private SiteMapEntry createEntry(final Page page) {
        final SiteMapEntry entry = new SiteMapEntry(page.getPath());
        entry.setUrl(externalize(page));
        final Map<String, PropertyProvider> propertyProviders = getPropertyProviders();
        for (final Map.Entry<String, PropertyProvider> e : propertyProviders.entrySet()) {
            entry.putProperty(e.getKey(), e.getValue().extractValue(page));
        }

        return entry;
    }

    private Map<String, PropertyProvider> getPropertyProviders() {
        final Map<String, PropertyProvider> result = new LinkedHashMap<>();
        for (final PropertyProvider provider : configuration.getPropertyProviders()) {
            addPropertyProvider(result, provider);
        }

        for (final PropertyProvider provider : getDefaultPropertyProviders()) {
            addPropertyProvider(result, provider);
        }

        return result;
    }

    private void addPropertyProvider(final Map<String, PropertyProvider> propertyProviders, final PropertyProvider provider) {
        if (isNull(provider)) {
            return;
        }

        final String propertyName = provider.getPropertyName();
        if (!propertyProviders.containsKey(propertyName)) {
            propertyProviders.put(propertyName, provider);
        }
    }

    protected abstract Iterable<? extends PropertyProvider> getDefaultPropertyProviders();

    private String externalize(final Page page) {
        final UrlExternalizer externalizer = getExternalizer();
        if (isNull(externalizer)) {
            return page.getPath() + SiteMapConstants.DOT_HTML;
        }

        return externalizer.map(page);
    }

    protected UrlExternalizer getExternalizer() {
        return configuration.getUrlExternalizer();
    }

    protected abstract SiteMapUrlBuilder getUrlBuilder();

    @Override
    public String buildSiteMapUrl(final Resource siteMapRoot, final int index) {
        final String url = getUrlBuilder().buildSiteMapUrl(siteMapRoot, index);
        return externalize(siteMapRoot.getResourceResolver(), url);
    }

    private String externalize(final ResourceResolver resourceResolver, final String url) {
        final UrlExternalizer externalizer = getExternalizer();
        if (isNull(externalizer)) {
            return url;
        }

        return externalizer.map(resourceResolver, url);
    }

    @Override
    public int getIndex(final SlingHttpServletRequest request) {
        return getUrlBuilder().getIndex(request);
    }

}
