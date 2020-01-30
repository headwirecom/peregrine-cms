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

import com.peregrine.sitemap.SiteMapConstants;
import com.peregrine.sitemap.SiteMapEntry;
import com.peregrine.sitemap.SiteMapStructureCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static java.util.Objects.isNull;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@SuppressWarnings("serial")
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Apache Rewrite Map Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + SLING_SERVLET_DEFAULT,
        SLING_SERVLET_SELECTORS + EQUALS + SiteMapConstants.SITE_MAP,
        SLING_SERVLET_EXTENSIONS + EQUALS + SiteMapConstants.TXT
    }
)
public final class ApacheRewriteMapServlet extends SlingAllMethodsServlet {

    private static final String PROTOCOLS_DOUBLE_SLASH = ":" + SLASH + SLASH;

    @Reference
    private SiteMapStructureCache structure;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException {
        final Resource resource = request.getResource();
        final List<SiteMapEntry> entries = structure.get(resource);
        if (isNull(entries)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(TEXT_MIME_TYPE);
        response.setCharacterEncoding(UTF_8);
        writeRewriteMap(entries, response.getWriter());
    }

    private void writeRewriteMap(final List<SiteMapEntry> entries, final PrintWriter target) {
        for (final SiteMapEntry entry : entries) {
            target.append(cutUrl(entry.getUrl()));
            target.append(StringUtils.SPACE);
            target.append(entry.getPath());
            target.append(SiteMapConstants.DOT_HTML);
            target.append(StringUtils.LF);
        }
    }

    private String cutUrl(final String url) {
        final StringBuilder result = new StringBuilder(SLASH);
        String string = url;
        if (StringUtils.contains(string, PROTOCOLS_DOUBLE_SLASH)) {
            string = StringUtils.substringAfter(string, PROTOCOLS_DOUBLE_SLASH);
        }

        result.append(StringUtils.substringAfter(string, SLASH));
        return result.toString();
    }

}
