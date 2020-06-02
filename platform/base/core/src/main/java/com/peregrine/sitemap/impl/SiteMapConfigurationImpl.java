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
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

@Component(service = SiteMapConfiguration.class, immediate = true)
@Designate(ocd = SiteMapConfigurationImplConfig.class, factory = true)
public final class SiteMapConfigurationImpl implements SiteMapConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private SiteMapConfigurationsContainer container;

    @Reference
    private NamedServiceRetriever serviceRetriever;

    private SiteMapConfigurationImplConfig config;

    @Activate
    public void activate(final SiteMapConfigurationImplConfig config) {
        this.config = config;
        if (config.enabled()) {
            container.add(this);
        }
    }

    @Deactivate
    public void deactivate() {
        container.remove(this);
    }

    @Override
    public Pattern getPagePathPattern() {
        try {
            return Pattern.compile(config.pathRegex());
        } catch (final PatternSyntaxException e) {
            logger.error("The path regex is not valid.", e);
        }

        return null;
    }

    @Override
    public PageRecognizer getPageRecognizer() {
        final List<PageRecognizer> recognizers
                = getServices(SiteMapConfigurationImplConfig::pageRecognizers, PageRecognizer.class);
        return new PageRecognizersAndChain(recognizers);
    }

    private <S extends HasName> List<S> getServices(final Function<? super SiteMapConfigurationImplConfig, String[]> getNames, final Class<S> clazz) {
        final Optional<Stream<String>> stream = Optional.of(config)
                .map(getNames)
                .map(Arrays::stream);
        if (!stream.isPresent()) {
            return Collections.emptyList();
        }

        return stream.get()
                .filter(StringUtils::isNotBlank)
                .map(name -> getNamedService(clazz, name))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private <S extends HasName> S getNamedService(final Class<S> clazz, final String name) {
        final S service = serviceRetriever.getNamedService(clazz, name);
        if (isNull(service)) {
            logger.error("The service '{}' of type {} was not found. Please check your configuration.",
                    name, clazz.getName());
        }

        return service;
    }

    @Override
    public UrlExternalizer getUrlExternalizer() {
        return getNamedService(UrlExternalizer.class, config.urlExternalizer());
    }

    @Override
    public Map<String, String> getXmlNamespaces() {
        return XmlNamespaceUtils.parseMappingsAddPrefix(config.xmlnsMappings());
    }

    @Override
    public Collection<PropertyProvider> getPropertyProviders() {
        return getServices(SiteMapConfigurationImplConfig::propertyProviders, PropertyProvider.class);
    }

    @Override
    public Set<String> getMandatoryCachedPaths() {
        if (config.enabled()) {
            final Set<String> result = new HashSet<>();
            Collections.addAll(result, config.mandatoryCachedRootPaths());
            return result;
        }

        return Collections.emptySet();
    }

}
