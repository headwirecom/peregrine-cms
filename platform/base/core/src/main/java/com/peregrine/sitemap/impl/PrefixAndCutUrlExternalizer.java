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

import com.peregrine.sitemap.UrlExternalizer;
import com.peregrine.sitemap.UrlExternalizerBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import static com.peregrine.commons.Strings.DOT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.sitemap.SiteMapConstants.SITE_MAP;
import static com.peregrine.sitemap.SiteMapConstants.XML;

@Component(service = UrlExternalizer.class, immediate = true)
@Designate(ocd = PrefixAndCutUrlExternalizerConfig.class, factory = true)
public final class PrefixAndCutUrlExternalizer extends UrlExternalizerBase {

    private static final String DOT_REGEX = "\\.";
    private PrefixAndCutUrlExternalizerConfig config;
    private int cutCount;
    private int siteMapPosition;
    private String prefix;

    @Activate
    public void activate(final PrefixAndCutUrlExternalizerConfig config) {
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
        int start = cutCount;
        if (isSiteMap(split)) {
            start = siteMapPosition;
            final String[] nameSplit = split[siteMapPosition].split(DOT_REGEX);
            final StringBuilder name = new StringBuilder(SITE_MAP);
            for (int i = 2; i < nameSplit.length; i++) {
                name.append(DOT);
                name.append(nameSplit[i]);
            }

            split[start] = name.toString();
        }

        final StringBuilder result = new StringBuilder(prefix);
        for (int i = start; i < split.length; i++) {
            result.append(SLASH);
            result.append(split[i]);
        }

        return result.toString();
    }

    private boolean isSiteMap(final String[] url) {
        int length = url.length;
        if (siteMapPosition >= length || length > siteMapPosition + 2) {
            return false;
        }

        final String[] siteMapPart = url[siteMapPosition].split(DOT_REGEX);
        length = siteMapPart.length;
        if (length >= 3 && siteMapPart[1].equals(SITE_MAP) && siteMapPart[length - 1].equals(XML)) {
            return true;
        }

        return false;
    }

}
