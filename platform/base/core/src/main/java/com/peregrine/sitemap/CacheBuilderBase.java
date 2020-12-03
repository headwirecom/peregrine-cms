package com.peregrine.sitemap;

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

import com.peregrine.commons.ResourceUtils;
import com.peregrine.versions.VersioningResourceResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public abstract class CacheBuilderBase<V, L extends CacheBuilder.RefreshListener<V>> implements CacheBuilder<V, L> {

    protected static final String COULD_NOT_SAVE_SITE_MAP_CACHE = "Could not save Site Map Cache.";
    protected static final String COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER = "Could not get Service Resource Resolver.";
    protected static final String COULD_NOT_SAVE_CHANGES_TO_REPOSITORY = "Could not save changes to repository.";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<L> refreshListeners = new HashSet<>();

    protected String location;
    protected String locationWithSlash;

    protected final void setLocation(final String location) {
        this.location = location;
        locationWithSlash = location + SLASH;
    }

    protected final Resource getCache(final ResourceResolver resourceResolver, final Resource rootPage) {
        if (!isCached(resourceResolver, rootPage.getPath())) {
            try {
                final Resource cache = build(resourceResolver, rootPage);
                resourceResolver.commit();
                return cache;
            } catch (final PersistenceException e) {
                logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
            }
        }

        final String path = getCachePath(rootPage);
        return resourceResolver.getResource(path);
    }

    protected abstract VersioningResourceResolver createResourceResolver() throws LoginException;

    protected final boolean isCached(final ResourceResolver resourceResolver, final String path) {
        return Optional.of(path)
                .map(this::getCacheContainerPath)
                .map(resourceResolver::getResource)
                .map(this::containsCacheAlready)
                .orElse(false);
    }

    protected final String getCachePath(final Resource rootPage) {
        return Optional.ofNullable(rootPage)
                .map(Resource::getPath)
                .map(this::getCachePath)
                .orElse(null);
    }

    private String getCacheContainerPath(final String rootPagePath) {
        return isRepositoryRoot(rootPagePath) ? location : location + rootPagePath;
    }

    private String getCachePath(final String rootPagePath) {
        return isRepositoryRoot(rootPagePath) ? location : getCachePathImpl(location + rootPagePath);
    }

    protected String getCachePathImpl(final String cachePath) {
        return cachePath;
    }

    protected static boolean isRepositoryRoot(final String path) {
        return StringUtils.equals(SLASH, StringUtils.trim(path));
    }

    protected final String getOriginalPath(final Resource cache) {
        return getOriginalPath(cache.getPath());
    }

    public final String getOriginalPath(final String cachePath) {
        if (!StringUtils.startsWith(cachePath, locationWithSlash)) {
            return null;
        }

        return getOriginalPathImpl(StringUtils.substringAfter(cachePath, location));
    }

    protected String getOriginalPathImpl(final String originalPath) {
        return originalPath;
    }

    protected boolean containsCacheAlready(final Resource cache) {
        return nonNull(cache);
    }

    protected boolean isCacheNode(final Resource cache) {
        return containsCacheAlready(cache);
    }

    protected final Resource build(final ResourceResolver resourceResolver, final Resource rootPage) {
        if (isNull(rootPage)) {
            return null;
        }

        try {
            final Resource cache = getOrCreateCacheResource(resourceResolver, rootPage);
            return build(rootPage, cache);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
            return null;
        }
    }

    protected final Resource getOrCreateCacheResource(final ResourceResolver resourceResolver, final Resource rootPage) throws PersistenceException {
        final String cachePath = getCachePath(rootPage);
        return ResourceUtils.getOrCreateResource(resourceResolver, cachePath, SLING_ORDERED_FOLDER);
    }

    protected abstract Resource build(Resource rootPage, Resource cache) throws PersistenceException;

    @Override
    public final void rebuild(final String rootPagePath) {
        try (final ResourceResolver resourceResolver = createResourceResolver()) {
            String path = rootPagePath;
            while (isNotBlank(path)) {
                if (isCached(resourceResolver, path)) {
                    rebuildImpl(path);
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

    protected final void cleanRemovedChildren(final ResourceResolver resourceResolver, final String rootPagePath)
            throws PersistenceException {
        final String cacheContainerPath = getCacheContainerPath(rootPagePath);
        final Resource cache = resourceResolver.getResource(cacheContainerPath);
        final Resource rootPage = resourceResolver.getResource(rootPagePath);
        if (nonNull(cache)) {
            cleanRemovedChildren(cache, rootPage);
        }
    }

    private void cleanRemovedChildren(final Resource cache, final Resource rootPage)
            throws PersistenceException {
        if (isNull(rootPage)) {
            cache.getResourceResolver().delete(cache);
            return;
        }

        final Iterator<Resource> iterator = cache.listChildren();
        while (iterator.hasNext()) {
            final Resource child = iterator.next();
            if (!isCacheNode(child)) {
                cleanRemovedChildren(child, rootPage.getChild(child.getName()));
            }
        }
    }

    protected abstract void rebuildImpl(final String rootPagePath);

    public final void build(final String path) {
        try (final ResourceResolver resourceResolver = createResourceResolver()) {
            cleanRemovedChildren(resourceResolver, path);
            final Resource resource = resourceResolver.getResource(path);
            build(resourceResolver, resource);
            resourceResolver.commit();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

    @Override
    public void rebuildAll() {
        try (final ResourceResolver resourceResolver = createResourceResolver()) {
            Optional.ofNullable(getCacheContainerPath(SLASH))
                    .map(resourceResolver::getResource)
                    .ifPresent(this::rebuildInTree);
            resourceResolver.commit();
            rebuildMandatoryContent();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

    protected void rebuildMandatoryContent() { }

    private boolean rebuildInTree(final Resource cache) {
        final Optional<String> rootPage = Optional.of(cache)
                .filter(this::containsCacheAlready)
                .map(this::getOriginalPath)
                .filter(StringUtils::isNotBlank);
        rootPage.ifPresent(this::rebuildImpl);
        boolean result = rootPage.isPresent();
        final Iterator<Resource> iterator = cache.listChildren();
        while (iterator.hasNext()) {
            result = rebuildInTree(iterator.next()) && result;
        }

        if (!result) {
            try {
                cache.getResourceResolver().delete(cache);
            } catch (final PersistenceException e) {
                logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
            }
        }

        return result;
    }

    @Override
    public void addRefreshListener(final L listener) {
        synchronized (refreshListeners) {
            refreshListeners.add(listener);
        }
    }

    @Override
    public void removeRefreshListener(final L listener) {
        synchronized (refreshListeners) {
            refreshListeners.remove(listener);
        }
    }

    protected void notifyCacheRefreshed(final Resource rootPage, final V value) {
        refreshListeners.stream()
                .forEach(l -> l.onCacheRefreshed(rootPage, value));
    }

}
