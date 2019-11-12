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

import com.peregrine.sitemap.*;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component(service = SiteMapExtractor.class, immediate = true)
@Designate(ocd = SiteMapExtractorImplConfig.class, factory = true)
public final class SiteMapExtractorImpl extends SiteMapExtractorBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private NamedServiceRetriever serviceRetriever;

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    @Reference
    private DefaultSiteMapExtractor defaultSiteMapExtractor;

    private Pattern pattern;

    @Activate
    public void activate(final SiteMapExtractorImplConfig config) {
        try {
            pattern = Pattern.compile(config.pathRegex());
        } catch (final PatternSyntaxException e) {
            logger.error("The path regex is not valid.", e);
        }

        pageRecognizer = getNamedService(PageRecognizer.class, config.pageRecognizer());
        urlExternalizer = getNamedService(UrlExternalizer.class, config.urlExternalizer());
        if (isNull(urlExternalizer)) {
            urlExternalizer = defaultSiteMapExtractor.getUrlExternalizer();
        }

        final String[] propertyProviders = config.propertyProviders();
        if (nonNull(propertyProviders)) {
            setPropertyProviders(propertyProviders);
        }

        addPropertyProvider(defaultSiteMapExtractor.getLastModPropertyProvider());
        addPropertyProvider(defaultSiteMapExtractor.getChangeFreqPropertyProvider());
        addPropertyProvider(defaultSiteMapExtractor.getPriorityPropertyProvider());

        if (isValid()) {
            siteMapExtractorsContainer.add(this);
        }
    }

    private <S extends HasName> S getNamedService(final Class<S> clazz, final String name) {
        final S service = serviceRetriever.getNamedService(clazz, name);
        if (isNull(service)) {
            logger.error("The service '{}' of type {} was not found. Please check your configuration.",
                    name, clazz.getName());
        }

        return service;
    }

    private void setPropertyProviders(final String[] names) {
        for (final String name: names) {
            final PropertyProvider provider = getNamedService(PropertyProvider.class, name);
            addPropertyProvider(provider);
        }
    }

    private boolean isValid() {
        return nonNull(pattern) && nonNull(pageRecognizer) && nonNull(urlExternalizer);
    }

    @Deactivate
    public void deactivate() {
        if (isValid()) {
            siteMapExtractorsContainer.remove(this);
        }

        clear();
        pattern = null;
    }

    protected SiteMapUrlBuilder getUrlBuilder() {
        return defaultSiteMapExtractor.getUrlBuilder();
    }

    @Override
    public boolean appliesTo(final Resource root) {
        return pattern.matcher(root.getPath()).matches();
    }

}
