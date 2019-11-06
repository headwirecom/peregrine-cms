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
import com.peregrine.sitemap.SiteMapUrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

@Component(service = SiteMapBuilder.class)
public final class SiteMapBuilderImpl implements SiteMapBuilder {

    private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";

    private static final String URL_SET = "urlset";
    private static final String URL_SET_START_TAG = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            " xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">";
    private static final String URL_SET_END_TAG = close(URL_SET);

    private static final String URL = "url";
    private static final String LOC = "loc";
    private static final String LAST_MOD = "lastmod";
    private static final String CHANGE_FREQ = "changefreq";
    private static final String PRIORITY = "priority";

    private static final int TAG_SYMBOLS_LENGTH = 5;
    private static final int BASE_SITE_MAP_LENGTH = XML_VERSION.length()
            + URL_SET_START_TAG.length() + URL_SET_END_TAG.length();
    private static final int BASE_ENTRY_LENGTH = baseTagLength(URL) + baseTagLength(LOC);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.sXXX");
    public static final int DATE_LENGTH = 28;
    public static final String ALWAYS = "always";
    public static final String PRIORITY_0_5 = "0.5";

    private static String open(final String tagName) {
        return "<" + tagName + ">";
    }

    private static String close(final String tagName) {
        return "</" + tagName + ">";
    }

    private static int baseTagLength(final String tagName) {
        return TAG_SYMBOLS_LENGTH + 2 * tagName.length();
    }

    @Override
    public String buildUrlSet(final Collection<SiteMapEntry> entries) {
        final StringBuilder result = new StringBuilder(XML_VERSION);
        result.append(URL_SET_START_TAG);
        for (final SiteMapEntry entry : entries) {
            if (!isEmpty(entry)) {
                result.append(toUrl(entry));
            }
        }

        result.append(URL_SET_END_TAG);
        return result.toString();
    }

    private boolean isEmpty(final SiteMapEntry entry) {
        return StringUtils.isBlank(entry.getUrl());
    }

    private String toUrl(final SiteMapEntry entry) {
        final StringBuilder result = new StringBuilder(open(URL));
        append(result, LOC, entry.getUrl());

        final Date lastModified = entry.getPage().getLastModifiedDate();
        if (lastModified != null) {
            append(result, LAST_MOD, DATE_FORMAT.format(lastModified));
        for (final Map.Entry<String, String> e : entry.getProperties()) {
            append(result, e.getKey(), e.getValue());
        }

        append(result, CHANGE_FREQ, ALWAYS);
        append(result, PRIORITY, PRIORITY_0_5);

        result.append(close(URL));
        return result.toString();
    }

    private void append(final StringBuilder builder, final String tagName, final String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }

        builder.append(open(tagName));
        builder.append(content);
        builder.append(close(tagName));
    }

    @Override
    public int getSize(final SiteMapEntry entry) {
        if (isEmpty(entry)) {
            return 0;
        }

        int length = BASE_ENTRY_LENGTH;
        length += entry.getUrl().length();

        final Date lastModified = entry.getPage().getLastModifiedDate();
        if (lastModified != null) {
            length += baseTagLength(LAST_MOD) + DATE_LENGTH;
        for (final Map.Entry<String, String> e : entry.getProperties()) {
            length += baseTagLength(e.getKey()) + e.getValue().length();
        }

        length += baseTagLength(CHANGE_FREQ) + ALWAYS.length();
        length += baseTagLength(PRIORITY) + PRIORITY_0_5.length();

        return length;
    }

    @Override
    public int getBaseSiteMapLength() {
        return BASE_SITE_MAP_LENGTH;
    }

    @Override
    public String buildSiteMapIndex(final Resource root, final SiteMapUrlBuilder urlBuilder, final int numberOfParts) {
        final StringBuilder result = new StringBuilder(XML_VERSION);
        result.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for (int part = 1; part <= numberOfParts; part++) {
            final String url = urlBuilder.buildSiteMapUrl(root, part);
            result.append("<sitemap>");
            append(result, LOC, url);
            final Date lastModified = new Date(System.currentTimeMillis());
            if (lastModified != null) {
                append(result, LAST_MOD, DATE_FORMAT.format(lastModified));
            }

            result.append("</sitemap>");
        }

        result.append("</sitemapindex>");
        return result.toString();
    }
}
