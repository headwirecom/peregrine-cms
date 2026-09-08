package com.peregrine.cachebust;

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

import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

import static com.peregrine.commons.Chars.EQ;

/**
 * Content-addressed URLs for front-end assets.
 *
 * The page renderer appends a cache token to every siteJS/siteCSS entry as a
 * Sling-style suffix that keeps the file's extension:
 *
 *    /etc/felibs/adminv2/js/editor.js/cb-18f2a4c9e01.js
 *
 * The resolver mapping at /etc/map/http/felibcb rewrites a tokenized URL
 * onto the clean file path (Sling cuts a request path at the FIRST dot, so
 * an nt:file could never take the suffix on its own). This filter only does
 * the headers: a token changes whenever the content does, so a tokenized
 * response is immutable for a year; everything without a token under
 * /etc/felibs answers no-cache instead - still served with Last-Modified (a
 * conditional GET is a cheap 304), but never trusted blindly. A browser that
 * heuristically cached the admin scripts across two different containers is
 * how v2-preview-issues #1 grew its strangest symptom.
 */
@Component(
    service = { Filter.class },
    property = {
        Constants.SERVICE_DESCRIPTION + EQ + "Peregrine Cache Bust Filter",
        EngineConstants.SLING_FILTER_SCOPE + EQ + EngineConstants.FILTER_SCOPE_REQUEST
    }
)
public final class CacheBustFilter implements Filter {

    private static final Pattern TOKENIZED = Pattern.compile(
        "^(/etc/felibs/.+\\.(js|css))/cb-[0-9a-zA-Z]+\\.\\2$");
    private static final Pattern BARE_ASSET = Pattern.compile(
        "^/etc/felibs/.+\\.(js|css)$");
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String IMMUTABLE = "public, max-age=31536000, immutable";
    private static final String NO_CACHE = "no-cache";

    @Override
    public void init(final FilterConfig filterConfig) { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(
        final ServletRequest request,
        final ServletResponse response,
        final FilterChain chain
    ) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            final HttpServletRequest req = (HttpServletRequest) request;
            final HttpServletResponse res = (HttpServletResponse) response;
            final String method = req.getMethod();
            if ("GET".equals(method) || "HEAD".equals(method)) {
                final String uri = req.getRequestURI();
                if (TOKENIZED.matcher(uri).matches()) {
                    res.setHeader(CACHE_CONTROL, IMMUTABLE);
                } else if (BARE_ASSET.matcher(uri).matches()) {
                    res.setHeader(CACHE_CONTROL, NO_CACHE);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
