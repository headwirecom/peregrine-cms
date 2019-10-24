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

import com.peregrine.sitemap.SiteMapCache;
import com.peregrine.sitemap.SiteMapUrlBuilder;
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

import static com.peregrine.commons.util.PerUtil.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Site Map Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "sling/servlet/default",
        SLING_SERVLET_SELECTORS + EQUALS + SiteMapServlet.SELECTOR,
        SLING_SERVLET_EXTENSIONS + EQUALS + SiteMapServlet.EXTENSION
    }
)
public final class SiteMapServlet extends SlingAllMethodsServlet implements SiteMapUrlBuilder {

    public static final String SELECTOR = "sitemap";
    public static final String EXTENSION = "xml";

    private static final String SLASH = "/";
    private static final String DOT = ".";

    private static final String UTF_8 = "utf-8";
    private static final String APPLICATION_XML = "application/xml";

    @Reference
    private SiteMapCache cache;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException {
        final Resource resource = request.getResource();
        final int index = getIndexFromSuffix(request.getRequestPathInfo().getSuffix());
        final String string = index >= 0 ? cache.get(resource, index, this) : null;
        if (StringUtils.isBlank(string)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(APPLICATION_XML);
        response.setCharacterEncoding(UTF_8);
        response.getWriter().write(string);
    }

    @Override
    public String buildSiteMapUrl(final Resource root, final int index) {
        return root.getPath() + DOT + SELECTOR + DOT + EXTENSION + SLASH + index;
    }

    private int getIndexFromSuffix(final String suffix) {
        if (StringUtils.isBlank(suffix)) {
            return 0;
        }

        final String string = StringUtils.substringAfter(suffix, SLASH);
        if (StringUtils.isBlank(string)) {
            return 0;
        }

        if (StringUtils.isNumeric(string)) {
            return Integer.parseInt(string);
        }

        return -1;
    }

}
