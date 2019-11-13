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

import com.peregrine.concurrent.Callback;
import com.peregrine.concurrent.DeBouncer;
import com.peregrine.sitemap.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.peregrine.commons.util.PerConstants.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

@Component(service = SiteMapStructureCache.class, immediate = true)
@Designate(ocd = SiteMapStructureCacheImplConfig.class)
public final class SiteMapStructureCacheImpl implements SiteMapStructureCache, Callback<String> {

    private static final String COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER = "Could not get Service Resource Resolver.";
    private static final String COULD_NOT_SAVE_SITE_MAP_CACHE = "Could not save Site Map Cache.";
    private static final String COULD_NOT_SAVE_CHANGES_TO_REPOSITORY = "Could not save changes to repository.";
    public static final String SLASH_JCR_CONTENT = SLASH + JCR_CONTENT;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    private DeBouncer<String> deBouncer;

    private SiteMapStructureCacheImplConfig config;
    private String location;
    private String locationWithSlash;

    @Activate
    public void activate(final SiteMapStructureCacheImplConfig config) {
        this.config = config;

        deBouncer = new DeBouncer<>(this, config.debounceInterval());

        location = config.location();
        locationWithSlash = location + SLASH;

        rebuildAll();
    }

    @Deactivate
    public void deactivate() {
        deBouncer.terminate();
    }

    @Override
    public List<SiteMapEntry> get(final Resource rootPage) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            final Resource cache;
            if (isCached(resourceResolver, rootPage.getPath())) {
                final String cachePath = getCachePath(rootPage);
                cache = resourceResolver.getResource(cachePath);
            } else {
                cache = buildCache(resourceResolver, rootPage);
                resourceResolver.commit();
            }

            return extractEntriesFromChildren(cache);
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
            return null;
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
            return null;
        }
    }

    private List<SiteMapEntry> extractEntriesFromChildren(final Resource parent) {
        final LinkedList<SiteMapEntry> result = new LinkedList<>();
        for (final Resource child : parent.getChildren()) {
            result.add(extractEntry(child));
        }

        return result;
    }

    private SiteMapEntry extractEntry(final Resource resource) {
        final ValueMap properties = resource.getValueMap();
        final SiteMapEntry entry = new SiteMapEntry();
        for (final Map.Entry<String, Object> e : properties.entrySet()) {
            final String key = e.getKey();
            if (!key.startsWith("jcr:")) {
                final Object value = e.getValue();
                entry.putProperty(key, String.valueOf(value));
            }
        }

        return entry;
    }

    private ResourceResolver getServiceResourceResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver();
    }

    private boolean isCached(final ResourceResolver resourceResolver, final String path) {
        final String cachePath = getCachePath(path);
        return Optional.of(resourceResolver)
                .map(rr -> rr.getResource(cachePath))
                .isPresent();
    }

    private String getCachePath(final Resource rootPage) {
        return getCachePath(rootPage.getPath());
    }

    private String getCachePath(final String rootPagePath) {
        return location + rootPagePath + SLASH_JCR_CONTENT;
    }

    private String getOriginalPath(final Resource cache) {
        return getOriginalPath(cache.getPath());
    }

    private String getOriginalPath(final String cachePath) {
        if (!StringUtils.startsWith(cachePath, locationWithSlash)) {
            return null;
        }

        if (!StringUtils.endsWith(cachePath, SLASH_JCR_CONTENT)) {
            return null;
        }

        String result = StringUtils.substringAfter(cachePath, location);
        result = StringUtils.substringBeforeLast(result, SLASH_JCR_CONTENT);
        return result;
    }

    private Resource buildCache(final ResourceResolver resourceResolver, final Resource rootPage) {
        try {
            final String cachePath = getCachePath(rootPage);
            final Resource cache = Utils.getOrCreateResource(resourceResolver, cachePath, SLING_ORDERED_FOLDER);
            return buildCache(rootPage, cache);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
            return null;
        }
    }

    private Resource buildCache(final Resource rootPage, final Resource cache) throws PersistenceException {
        final SiteMapExtractor extractor = siteMapExtractorsContainer.findFirstFor(rootPage);
        if (isNull(extractor)) {
            putSiteMapsInCache(null, cache);
            return null;
        }

        final Collection<SiteMapEntry> entries = extractor.extract(rootPage);
        putSiteMapsInCache(entries, cache);

        return cache;
    }

    private void putSiteMapsInCache(final Collection<SiteMapEntry> source, final Resource target) throws PersistenceException {
        final int siteMapsSize = nonNull(source) ? source.size() : 0;
        final Iterator<SiteMapEntry> iterator = source.iterator();
        final ResourceResolver resourceResolver = target.getResourceResolver();
        for (int i = 0; i < siteMapsSize; i++) {
            final String childPath = Integer.toString(i);
            Resource child = target.getChild(childPath);
            if (nonNull(child)) {
                resourceResolver.delete(child);
            }

            final SiteMapEntry entry = iterator.next();
            final Map<String, Object> properties = new HashMap<>();
            properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            properties.put(SiteMapConstants.LOC, entry.getUrl());
            for (final Map.Entry<String, String> e : entry.getProperties()) {
                properties.put(e.getKey(), e.getValue());
            }

            resourceResolver.create(target, childPath, properties);
        }

        removeCachedItemsAboveIndex(target, siteMapsSize);
    }

    private void removeCachedItemsAboveIndex(final Resource target, final int startItemIndex) throws PersistenceException {
        final ResourceResolver resourceResolver = target.getResourceResolver();
        int i = startItemIndex;
        String childPath = Integer.toString(i);
        Resource child = target.getChild(childPath);
        while (nonNull(child)) {
            resourceResolver.delete(child);
            childPath = Integer.toString(++i);
            child = target.getChild(childPath);
        }
    }

    @Override
    public void rebuild(final String rootPagePath) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            cleanRemovedChildren(resourceResolver, rootPagePath);
            String path = rootPagePath;
            while (isNotBlank(path)) {
                if (isCached(resourceResolver, path)) {
                    deBouncer.call(path);
                }

                path = substringBeforeLast(path, SLASH);
            }

            resourceResolver.commit();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

    private void cleanRemovedChildren(final ResourceResolver resourceResolver, final String rootPagePath)
            throws PersistenceException {
        final Resource cache = resourceResolver.getResource(getCachePath(rootPagePath));
        final Resource rootPage = resourceResolver.getResource(rootPagePath);
        cleanRemovedChildren(cache, rootPage);
    }

    private void cleanRemovedChildren(final Resource cache, final Resource rootPage)
            throws PersistenceException {
        if (isNull(cache)) {
            return;
        }

        if (isNull(rootPage)) {
            cache.getResourceResolver().delete(cache);
        } else {
            final Iterator<Resource> iterator = cache.listChildren();
            while (iterator.hasNext()) {
                final Resource child = iterator.next();
                cleanRemovedChildren(child, rootPage.getChild(child.getName()));
            }
        }
    }

    private void buildCache(final ResourceResolver resourceResolver, final String rootPagePath) {
        buildCache(resourceResolver, resourceResolver.getResource(rootPagePath));
    }

    @Override
    public void rebuildAll() {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            cleanRemovedChildren(resourceResolver, SLASH);
            rebuildMandatoryContent();
            rebuildExistingCache(resourceResolver);
            resourceResolver.commit();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

    private void rebuildMandatoryContent() {
        if (isNull(config) || isNull(config.mandatoryCachedRootPaths())) {
            return;
        }

        for (final String path : config.mandatoryCachedRootPaths()) {
            deBouncer.call(path);
        }
    }

    private void rebuildExistingCache(final ResourceResolver resourceResolver) {
        final String cacheRoot = getCachePath(StringUtils.EMPTY);
        final Resource root = resourceResolver.getResource(cacheRoot);
        if (nonNull(root)) {
            rebuildCacheInTree(root);
        }
    }

    private void rebuildCacheInTree(final Resource cache) {
        final Iterator<Resource> iterator = cache.listChildren();
        if (nonNull(cache)) {
            final String rootPagePath = getOriginalPath(cache);
            deBouncer.call(rootPagePath);
        }

        while (iterator.hasNext()) {
            rebuildCacheInTree(iterator.next());
        }
    }

    @Override
    public void call(final String rootPagePath) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            buildCache(resourceResolver, rootPagePath);
            resourceResolver.commit();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

}
