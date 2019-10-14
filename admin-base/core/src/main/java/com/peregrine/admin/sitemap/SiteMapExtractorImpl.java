package com.peregrine.admin.sitemap;

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

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import java.util.LinkedList;
import java.util.List;

@Component(service = SiteMapExtractor.class)
public final class SiteMapExtractorImpl implements SiteMapExtractor {

    private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
    private static final String URLSET_START_TAG = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
            "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "   xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n";
    private static final String URLSET_END_TAG = "</urlset>";

    @Override
    public String extractSiteMap(final Resource root) {
        final StringBuilder result = new StringBuilder(XML_VERSION);
        result.append(URLSET_START_TAG);
        for (final Resource page : extractSubPages(root)) {
            result.append(toUrl(page));
        }

        result.append(URLSET_END_TAG);
        return result.toString();
    }

    private List<Resource> extractSubPages(final Resource root) {
        final List<Resource> result = new LinkedList<>();
        for (final Resource child: root.getChildren()) {
            if (isPage(child)) {
                result.add(child);
                result.addAll(extractSubPages(child));
            }
        }

        return result;
    }

    private boolean isPage(final Resource resource) {
        if (!resource.isResourceType("per:Page")) {
            return false;
        }

        final Resource content = resource.getChild("jcr:content");
        if (content == null) {
            return false;
        }

        return content.getValueMap().containsKey("sling:resourceType");
    }

    private String toUrl(final Resource page) {
        return "    <url>\n" +
                "        <loc>http://example.com/" + page.getPath() + ".html</loc>\n" +
                "        <lastmod>2006-11-18</lastmod>\n" +
                "        <changefreq>always</changefreq>\n" +
                "        <priority>0.5</priority>\n" +
                "    </url>\n";
    }
}
