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

import com.peregrine.sitemap.SiteMapUrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import static com.peregrine.commons.util.PerConstants.DOT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.sitemap.impl.SiteMapServlet.EXTENSION;
import static com.peregrine.sitemap.impl.SiteMapServlet.SELECTOR;

@Component(service = SiteMapUrlBuilder.class)
public final class SiteMapUrlBuilderImpl implements SiteMapUrlBuilder {

    private static final String BASE_SUFFIX = DOT + SELECTOR + DOT + EXTENSION;
    private static final String INDEXED_SUFFIX = BASE_SUFFIX + SLASH;

    @Override
    public String buildSiteMapUrl(final Resource root, final int index) {
        final String path = root.getPath();
        if (index > 0) {
            return path + INDEXED_SUFFIX + index;
        }

        return path + BASE_SUFFIX;
    }

    @Override
    public int getIndex(final SlingHttpServletRequest request) {
        final String suffix = request.getRequestPathInfo().getSuffix();
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
