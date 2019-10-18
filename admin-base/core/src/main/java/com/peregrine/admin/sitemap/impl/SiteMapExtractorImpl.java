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

import com.peregrine.admin.sitemap.*;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component(service = SiteMapExtractor.class, immediate = true)
@Designate(ocd = SiteMapExtractorImplConfig.class, factory = true)
public final class SiteMapExtractorImpl implements SiteMapExtractor {

    private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
    private static final String URLSET_START_TAG = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
            "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "   xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n";
    private static final String URLSET_END_TAG = "</urlset>";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.sXXX");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private NamedServiceRetriever serviceRetriever;

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

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
    public String extractSiteMap(final Resource root) {
        final StringBuilder result = new StringBuilder(XML_VERSION);
        result.append(URLSET_START_TAG);
        for (final Page page : extractSubPages(new Page(root))) {
            result.append(toUrl(page));
        }

        result.append(URLSET_END_TAG);
        return result.toString();
    }

    private List<Page> extractSubPages(final Page root) {
        final List<Page> result = new LinkedList<>();
        if (!pageRecognizer.isPage(root)) {
            return result;
        }

        result.add(root);
        for (final Resource child: root.getChildren()) {
            final Page childPage = new Page(child);
            if (pageRecognizer.isPage(childPage)) {
                result.addAll(extractSubPages(childPage));
            }
        }

        return result;
    }

    private String toUrl(final Page page) {
        final StringBuilder result = new StringBuilder("<url>");

        result.append("<loc>");
        result.append(urlShortener.map(page));
        result.append("</loc>");

        final Date lastModified = page.getLastModifiedDate();
        if (lastModified != null) {
            result.append("<lastmod>");
            result.append(DATE_FORMAT.format(lastModified));
            result.append("</lastmod>");
        }

        result.append("<changefreq>always</changefreq>");
        result.append("<priority>0.5</priority>");

        result.append("</url>");
        return result.toString();
    }
}
