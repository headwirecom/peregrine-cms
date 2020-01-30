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

import com.peregrine.sitemap.SiteMapEntry;
import com.peregrine.sitemap.SiteMapFileContentBuilder;
import com.peregrine.sitemap.SiteMapUrlBuilder;
import com.peregrine.sitemap.XMLBuilder;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.sitemap.SiteMapConstants.*;
import static com.peregrine.sitemap.impl.XmlNamespaceUtils.XMLNS;
import static org.apache.commons.lang3.StringUtils.*;

@Component(service = SiteMapFileContentBuilder.class)
@Designate(ocd = SiteMapFileContentBuilderImplConfig.class)
public final class SiteMapFileContentBuilderImpl implements SiteMapFileContentBuilder {

    private static final int BASE_ENTRY_LENGTH = XMLBuilder.getBasicElementLength(URL);
    private static final Map<String, String> SITE_MAP_INDEX_ATTRIBUTES = new HashMap<>();
    private static final Map<String, String> URL_SET_ATTRIBUTES = new HashMap<>();

    static {
        SITE_MAP_INDEX_ATTRIBUTES.put(XMLNS, "http://www.sitemaps.org/schemas/sitemap/0.9");
        URL_SET_ATTRIBUTES.putAll(SITE_MAP_INDEX_ATTRIBUTES);
        URL_SET_ATTRIBUTES.put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        URL_SET_ATTRIBUTES.put("xsi:schemaLocation",
                "http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd");
    }

    private final SiteMapEntrySizeVisitor siteMapEntrySizeVisitor = new SiteMapEntrySizeVisitor();
    private final UrlSetMapPropertiesVisitor urlSetMapPropertiesVisitor = new UrlSetMapPropertiesVisitor();
    private final Map<String, String> urlSetAttributes = new HashMap<>(URL_SET_ATTRIBUTES);
    private int baseSiteMapLength;

    @Activate
    public void activate(final SiteMapFileContentBuilderImplConfig config) {
        urlSetAttributes.clear();
        urlSetAttributes.putAll(URL_SET_ATTRIBUTES);
        final String[] xmlnsMappings = config.xmlnsMappings();
        final Map<String, String> mappings = XmlNamespaceUtils.parseMappingsAddPrefix(xmlnsMappings);
        urlSetAttributes.putAll(mappings);
        baseSiteMapLength = XMLBuilder.XML_VERSION.length();
        baseSiteMapLength += XMLBuilder.getBasicElementLength(URL_SET, urlSetAttributes);
    }

    @Override
    public String buildSiteMapIndex(final Resource root, final SiteMapUrlBuilder urlBuilder, final List<? extends List<SiteMapEntry>> splitEntries) {
        final XMLBuilder result = new XMLBuilder();
        result.startElement(SITE_MAP_INDEX, SITE_MAP_INDEX_ATTRIBUTES);
        int part = 1;
        for (final List<SiteMapEntry> entries : splitEntries) {
            final String url = urlBuilder.buildSiteMapUrl(root, part++);
            result.startElement(SITE_MAP);
            result.addElement(LOC, url);
            final String lastModified = getLastModified(entries);
            if (isNotBlank(lastModified)) {
                result.addElement(LAST_MOD, lastModified);
            }

            result.endElement();
        }

        result.endElement();
        return result.toString();
    }

    private String getLastModified(final List<SiteMapEntry> source) {
        String result = EMPTY;
        for (final SiteMapEntry entry : source) {
            final String next = entry.getLastModified();
            if (isNotBlank(next) && next.compareTo(result) > 0) {
                result = next;
            }
        }

        return result;
    }

    @Override
    public int getBaseSiteMapLength() {
        return baseSiteMapLength;
    }

    @Override
    public int getSize(final SiteMapEntry entry) {
        if (isEmpty(entry)) {
            return 0;
        }

        return entry.walk(siteMapEntrySizeVisitor, BASE_ENTRY_LENGTH, URL);
    }

    private boolean isEmpty(final SiteMapEntry entry) {
        return isBlank(entry.getUrl());
    }

    @Override
    public String buildUrlSet(final Collection<SiteMapEntry> entries, final Map<String, String> xmlns) {
        final XMLBuilder result = new XMLBuilder();
        final Map<String, String> attributes = new HashMap<>(urlSetAttributes);
        attributes.putAll(xmlns);
        result.startElement(URL_SET, attributes);
        for (final SiteMapEntry entry : entries) {
            if (!isEmpty(entry)) {
                entry.walk(urlSetMapPropertiesVisitor, result, URL);
            }
        }

        result.endElement();
        return result.toString();
    }

    private static final class SiteMapEntrySizeVisitor implements SiteMapEntry.Visitor<Integer> {

        @Override
        public Integer visit(final String mapName, final Map<String, String> properties, final Integer size) {
            return size;
        }

        @Override
        public Integer visit(final String propertyName, final String propertyValue, final Integer size) {
            return size + XMLBuilder.getBasicElementLength(propertyName) + propertyValue.length();
        }

        @Override
        public Integer endVisit(final String mapName, final Integer size) {
            return size + XMLBuilder.getBasicElementLength(mapName);
        }

    }

    private static final class UrlSetMapPropertiesVisitor implements SiteMapEntry.Visitor<XMLBuilder> {

        @Override
        public XMLBuilder visit(final String mapName, final Map<String, String> properties, final XMLBuilder builder) {
            return builder.startElement(mapName);
        }

        @Override
        public XMLBuilder visit(final String propertyName, final String propertyValue, final XMLBuilder builder) {
            return builder.addElement(propertyName, propertyValue);
        }

        @Override
        public XMLBuilder endVisit(final String mapName, final XMLBuilder builder) {
            return builder.endElement();
        }
    }

}
