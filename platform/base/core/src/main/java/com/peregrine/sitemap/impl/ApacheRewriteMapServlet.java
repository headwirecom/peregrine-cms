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

import com.peregrine.commons.util.PerConstants;
import com.peregrine.sitemap.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.peregrine.commons.util.PerUtil.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Apache Rewrite Map Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + PerConstants.SLING_SERVLET_DEFAULT,
        SLING_SERVLET_SELECTORS + EQUALS + SiteMapConstants.SITE_MAP,
        SLING_SERVLET_EXTENSIONS + EQUALS + SiteMapConstants.TXT
    }
)
public final class ApacheRewriteMapServlet extends SlingAllMethodsServlet {

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

        response.setContentType(PerConstants.TEXT_MIME_TYPE);
        response.setCharacterEncoding(PerConstants.UTF_8);
        response.getWriter().write(buildRewriteMap(entries));
    }

    private String buildRewriteMap(final List<SiteMapEntry> entries) {
        final StringBuilder result = new StringBuilder();
        for (final SiteMapEntry entry : entries) {
            result.append(entry.getUrl());
            result.append("\n");
        }

        return result.toString();
    }

}
