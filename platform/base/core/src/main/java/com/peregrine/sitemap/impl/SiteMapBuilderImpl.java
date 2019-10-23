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

import com.peregrine.sitemap.SiteMapBuilder;
import com.peregrine.sitemap.SiteMapEntry;
import org.osgi.service.component.annotations.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

@Component(service = SiteMapBuilder.class)
public final class SiteMapBuilderImpl implements SiteMapBuilder {

    private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
    private static final String URLSET_START_TAG = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
            "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "   xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n";
    private static final String URLSET_END_TAG = "</urlset>";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.sXXX");

    @Override
    public String build(final Collection<SiteMapEntry> entries) {
        final StringBuilder result = new StringBuilder(XML_VERSION);
        result.append(URLSET_START_TAG);
        for (final SiteMapEntry entry : entries) {
            result.append(toUrl(entry));
        }

        result.append(URLSET_END_TAG);
        return result.toString();
    }

    private String toUrl(final SiteMapEntry entry) {
        final StringBuilder result = new StringBuilder("<url>");

        result.append("<loc>");
        result.append(entry.getUrl());
        result.append("</loc>");

        final Date lastModified = entry.getPage().getLastModifiedDate();
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
