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
    public String extractSiteMap(final Resource root, final String domain) {
        final StringBuilder result = new StringBuilder(XML_VERSION);
        result.append(URLSET_START_TAG);
        for (final Page page : extractSubPages(new Page(root))) {
            result.append(toUrl(page, domain));
        }

        result.append(URLSET_END_TAG);
        return result.toString();
    }

    private List<Page> extractSubPages(final Page root) {
        final List<Page> result = new LinkedList<>();
        if (!isPage(root)) {
            return result;
        }

        result.add(root);
        for (final Resource child: root.getChildren()) {
            final Page childPage = new Page(child);
            if (isPage(childPage)) {
                result.addAll(extractSubPages(childPage));
            }
        }

        return result;
    }

    private boolean isPage(final Page candidate) {
        if (!candidate.isResourceType("per:Page")) {
            return false;
        }

        if (!candidate.hasContent()) {
            return false;
        }

        return candidate.containsProperty("sling:resourceType");
    }

    private String toUrl(final Page page, final String domain) {
        final StringBuilder result = new StringBuilder("<url>");

        result.append("<loc>");
        result.append(domain);
        result.append(page.getPath());
        result.append(".html");
        result.append("</loc>");

        result.append("<changefreq>always</changefreq>");
        result.append("<priority>0.5</priority>");

        result.append("</url>");
        return result.toString();
    }
}
