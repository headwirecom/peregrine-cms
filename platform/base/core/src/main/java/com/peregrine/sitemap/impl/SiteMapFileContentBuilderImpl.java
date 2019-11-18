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

import com.peregrine.sitemap.SiteMapFileContentBuilder;
import com.peregrine.sitemap.SiteMapEntry;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.peregrine.sitemap.SiteMapConstants.*;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component(service = SiteMapFileContentBuilder.class)
@Designate(ocd = SiteMapFileContentBuilderImplConfig.class)
public final class SiteMapFileContentBuilderImpl implements SiteMapFileContentBuilder,
        SiteMapEntry.PropertiesVisitor<Integer> {

    private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    private static final String EQ = "=";
    private static final String URL_SET_END_TAG = close(URL_SET);

    private static final int TAG_SYMBOLS_LENGTH = 5;
    private static final int BASE_ENTRY_LENGTH = baseTagLength(URL);

    private final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    private String urlSetStartTag;
    private int baseSiteMapLength;

    @Activate
    public void activate(final SiteMapFileContentBuilderImplConfig config) {
        urlSetStartTag = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\"";

        final String[] xmlnsMappings = config.xmlnsMappings();
        if (nonNull(xmlnsMappings)) {
            for (final String mapping : xmlnsMappings) {
                if (StringUtils.contains(mapping, EQ)) {
                    urlSetStartTag += " xmlns:";
                    urlSetStartTag += StringUtils.substringBefore(mapping, EQ);
                    urlSetStartTag += "=\"";
                    urlSetStartTag += StringUtils.substringAfter(mapping, EQ);
                    urlSetStartTag += "\"";
                }
            }
        }

        urlSetStartTag += ">";
        baseSiteMapLength = XML_VERSION.length()
                + urlSetStartTag.length() + URL_SET_END_TAG.length();
    }

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
        result.append(urlSetStartTag);
        for (final SiteMapEntry entry : entries) {
            if (!isEmpty(entry)) {
                result.append(toUrl(entry));
            }
        }

        result.append(URL_SET_END_TAG);
        return result.toString();
    }

    private boolean isEmpty(final SiteMapEntry entry) {
        return isBlank(entry.getUrl());
    }

    private String toUrl(final SiteMapEntry entry) {
        final StringBuilder result = new StringBuilder(open(URL));
        for (final Map.Entry<String, Object> e : entry.getProperties().entrySet()) {
            append(result, e.getKey(), String.valueOf(e.getValue()));
        }

        result.append(close(URL));
        return result.toString();
    }

    private void append(final StringBuilder builder, final String tagName, final String content) {
        if (isBlank(content)) {
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

        return entry.walk(this, BASE_ENTRY_LENGTH);
    }

    @Override
    public Integer visit(final String propertyName, final String propertyValue, final Integer size) {
        return visit(propertyName, size) + propertyValue.length();
    }

    @Override
    public Integer visit(final String mapName, final Integer size) {
        return size + baseTagLength(mapName);
    }

    @Override
    public int getBaseSiteMapLength() {
        return baseSiteMapLength;
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
            if (nonNull(lastModified)) {
                append(result, LAST_MOD, dateFormat.format(lastModified));
            }

            result.append("</sitemap>");
        }

        result.append("</sitemapindex>");
        return result.toString();
    }
}
