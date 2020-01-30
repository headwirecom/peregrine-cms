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
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import static com.peregrine.commons.Strings.DOT;
import static com.peregrine.sitemap.SiteMapConstants.SITE_MAP;
import static com.peregrine.sitemap.SiteMapConstants.XML;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Component(service = SiteMapUrlBuilder.class)
public final class SiteMapUrlBuilderImpl implements SiteMapUrlBuilder {

    @Override
    public String buildSiteMapUrl(final Resource root, final int index) {
        final String path = root.getPath();
        if (index > 0) {
            return path + DOT + SITE_MAP + DOT + index + DOT + XML;
        }

        return path + DOT + SITE_MAP + DOT + XML;
    }

    @Override
    public int getIndex(final SlingHttpServletRequest request) {
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (isNull(selectors) || selectors.length <= 1) {
            return 0;
        }

        final String selector = selectors[1];
        if (isBlank(selector)) {
            return 0;
        }

        if (isNumeric(selector)) {
            return Integer.parseInt(selector);
        }

        return -1;
    }

}
