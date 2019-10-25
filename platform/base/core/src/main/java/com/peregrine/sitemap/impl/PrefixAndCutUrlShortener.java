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

import com.peregrine.sitemap.UrlShortener;
import com.peregrine.sitemap.UrlShortenerBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = UrlShortener.class, immediate = true)
@Designate(ocd = PrefixAndCutUrlShortenerConfig.class, factory = true)
public final class PrefixAndCutUrlShortener extends UrlShortenerBase {

    private static final String SLASH = "/";

    private PrefixAndCutUrlShortenerConfig config;
    private int cutCount;
    private int siteMapPosition;
    private String prefix;

    @Activate
    public void activate(final PrefixAndCutUrlShortenerConfig config) {
        this.config = config;
        cutCount = config.cutCount();
        siteMapPosition = cutCount - 1;
        prefix = config.prefix();
    }

    @Override
    public String getName() {
        return config.name();
    }

    @Override
    public String map(final ResourceResolver resourceResolver, final String url) {
        final String[] split = StringUtils.split(url, SLASH);
        final int length = split.length;
        int start = cutCount;
        if (siteMapPosition < length && length <= siteMapPosition + 2 && split[siteMapPosition].endsWith(".sitemap.xml")) {
            start = siteMapPosition;
            split[start] = "sitemap.xml";
        }

        StringBuilder result = new StringBuilder(prefix);
        for (int i = start; i < split.length; i++) {
            result.append(SLASH);
            result.append(split[i]);
        }

        return result.toString();
    }

}
