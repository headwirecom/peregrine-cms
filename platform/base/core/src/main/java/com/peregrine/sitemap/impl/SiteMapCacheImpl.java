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
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = SiteMapCache.class)
@Designate(ocd = SiteMapCacheImplConfig.class)
public final class SiteMapCacheImpl implements SiteMapCache {

    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    @Reference
    private SiteMapBuilder siteMapBuilder;

    private int maxEntriesCount;
    private int maxFileSize;

    @Activate
    public void activate(final SiteMapCacheImplConfig config) {
        maxEntriesCount = config.maxEntriesCount();
        if (maxEntriesCount <= 0) {
            maxEntriesCount = Integer.MAX_VALUE;
        }

        maxFileSize = config.maxFileSize();
        if (maxFileSize <= 0) {
            maxFileSize = Integer.MAX_VALUE;
        }
    }

    @Override
    public String get(final Resource root, final int index) {
        final String path = root.getPath();
        if (!cache.containsKey(path)) {
            final SiteMapExtractor extractor = siteMapExtractorsContainer.findFirstFor(root);
            if (extractor == null) {
                return null;
            }

            final Collection<SiteMapEntry> entries = extractor.extract(root);
            final LinkedList<List<SiteMapEntry>> splitEntries = splitEntries(entries);
            final ArrayList<String> strings = new ArrayList<>();
            cache.put(path, strings);
            if (splitEntries.size() > 1) {
                strings.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
                        "   <sitemap>\n" +
                        "      <loc>http://www.example.com/sitemap1.xml.gz</loc>\n" +
                        "      <lastmod>2014-10-01T18:23:17+00:00</lastmod>\n" +
                        "   </sitemap>\n" +
                        "</sitemapindex>");
            }

            for (final List<SiteMapEntry> list : splitEntries) {
                strings.add(siteMapBuilder.build(list));
            }
        }

        final List<String> strings = cache.get(path);
        return index >= 0 && index < strings.size() ? strings.get(index) : null;
    }

    private LinkedList<List<SiteMapEntry>> splitEntries(final Collection<SiteMapEntry> entries) {
        final LinkedList<List<SiteMapEntry>> result = new LinkedList<>();
        int index = 0;
        int size = siteMapBuilder.getBaseSiteMapLength();
        List<SiteMapEntry> split = new LinkedList<>();
        result.add(split);
        for (final SiteMapEntry entry : entries) {
            final int entrySize = siteMapBuilder.getSize(entry);
            if (index < maxEntriesCount && size + entrySize <= maxFileSize) {
                split.add(entry);
                index++;
                size += entrySize;
            } else {
                index = 0;
                size = siteMapBuilder.getBaseSiteMapLength();
                split = new LinkedList<>();
                result.add(split);
            }
        }

        return result;
    }
}
