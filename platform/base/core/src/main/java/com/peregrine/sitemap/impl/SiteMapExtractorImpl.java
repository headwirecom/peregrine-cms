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
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component(service = SiteMapExtractor.class, immediate = true)
@Designate(ocd = SiteMapExtractorImplConfig.class, factory = true)
public final class SiteMapExtractorImpl implements SiteMapExtractor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private NamedServiceRetriever serviceRetriever;

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    @Reference
    private EtcMapUrlShortener defaultUrlShortener;

    @Reference
    private SiteMapUrlBuilder basicUrlBuilder;

    private Pattern pattern;

    private PageRecognizer pageRecognizer;

    private UrlShortener urlShortener;

    @Activate
    public void activate(final SiteMapExtractorImplConfig config) {
        try {
            pattern = Pattern.compile(config.pathRegex());
        } catch (final PatternSyntaxException e) {
            logger.error("The path regex is not valid.", e);
        }

        pageRecognizer = getNamedService(PageRecognizer.class, config.pageRecognizer());
        urlShortener = getNamedService(UrlShortener.class, config.urlShortener());
        if (urlShortener == null) {
            urlShortener = defaultUrlShortener;
        }

        if (isValid()) {
            siteMapExtractorsContainer.add(this);
        }
    }

    private boolean isValid() {
        return pattern != null && pageRecognizer != null && urlShortener != null;
    }

    @Deactivate
    public void deactivate() {
        if (isValid()) {
            siteMapExtractorsContainer.remove(this);
        }

        urlShortener = null;
        pageRecognizer = null;
        pattern = null;
    }

    @Override
    public boolean appliesTo(final Resource root) {
        return pattern.matcher(root.getPath()).matches();
    }

    private <S extends HasName> S getNamedService(final Class<S> clazz, final String name) {
        final S service = serviceRetriever.getNamedService(clazz, name);
        if (service == null) {
            logger.error("The service '{}' of type {} was not found. Please check your configuration.",
                    name, clazz.getName());
        }

        return service;
    }

    @Override
    public List<SiteMapEntry> extract(final Resource root) {
        return extract(new Page(root));
    }

    private List<SiteMapEntry> extract(final Page root) {
        final List<SiteMapEntry> result = new LinkedList<>();
        if (!pageRecognizer.isPage(root)) {
            return result;
        }

        result.add(createEntry(root));
        for (final Resource child: root.getChildren()) {
            final Page childPage = new Page(child);
            if (pageRecognizer.isPage(childPage)) {
                result.addAll(extract(childPage));
            }
        }

        return result;
    }

    private SiteMapEntry createEntry(final Page page) {
        final SiteMapEntry entry = new SiteMapEntry(page);
        entry.setUrl(urlShortener.map(page));
        return entry;
    }

    @Override
    public String buildSiteMapUrl(final Resource siteMapRoot, final int index) {
        final String url = basicUrlBuilder.buildSiteMapUrl(siteMapRoot, index);
        return urlShortener.map(siteMapRoot.getResourceResolver(), url);
    }

    @Override
    public int getIndex(final SlingHttpServletRequest request) {
        return basicUrlBuilder.getIndex(request);
    }

}
