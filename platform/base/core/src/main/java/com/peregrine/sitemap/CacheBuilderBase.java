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

import com.peregrine.concurrent.Callback;
import com.peregrine.concurrent.DeBouncer;
import com.peregrine.sitemap.impl.SiteMapStructureCacheImplConfig;
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

public abstract class CacheBuilderBase implements CacheBuilder {

    protected static final String COULD_NOT_SAVE_SITE_MAP_CACHE = "Could not save Site Map Cache.";
    protected static final String COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER = "Could not get Service Resource Resolver.";
    protected static final String COULD_NOT_SAVE_CHANGES_TO_REPOSITORY = "Could not save changes to repository.";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String location;
    protected String locationWithSlash;

    protected void setLocation(final String location) {
        this.location = location;
        locationWithSlash = location + SLASH;
    }

    protected String getCachePath(final Resource rootPage) {
        return getCachePath(rootPage.getPath());
    }

    protected abstract String getCachePath(String rootPagePath);

    protected String getOriginalPath(final Resource cache) {
        return getOriginalPath(cache.getPath());
    }

    protected abstract String getOriginalPath(String cachePath);

    protected Resource buildCache(final ResourceResolver resourceResolver, final Resource rootPage) {
        try {
            final String cachePath = getCachePath(rootPage);
            final Resource cache = Utils.getOrCreateResource(resourceResolver, cachePath, SLING_ORDERED_FOLDER);
            return buildCache(rootPage, cache);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
            return null;
        }
    }

    protected abstract Resource buildCache(Resource rootPage, Resource cache) throws PersistenceException;

    @Override
    public void rebuild(final String rootPagePath) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            cleanRemovedChildren(resourceResolver, rootPagePath);
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

    protected abstract ResourceResolver getServiceResourceResolver() throws LoginException;

    protected void cleanRemovedChildren(final ResourceResolver resourceResolver, final String rootPagePath)
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

    protected abstract void rebuildImpl(final String rootPagePath);

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

    protected void rebuildMandatoryContent() { }

    private void rebuildExistingCache(final ResourceResolver resourceResolver) {
        final String cacheRoot = getCachePath(StringUtils.EMPTY);
        final Resource root = resourceResolver.getResource(cacheRoot);
        if (nonNull(root)) {
            rebuildCacheInTree(root);
        }
    }

    private void rebuildCacheInTree(final Resource cache) {
        final Iterator<Resource> iterator = cache.listChildren();
        if (containsCacheAlready(cache)) {
            final String rootPagePath = getOriginalPath(cache);
            rebuildImpl(rootPagePath);
        }

        while (iterator.hasNext()) {
            rebuildCacheInTree(iterator.next());
        }
    }

    protected boolean isCached(final ResourceResolver resourceResolver, final String path) {
        final String cachePath = getCachePath(path);
        return Optional.of(resourceResolver)
                .map(rr -> rr.getResource(cachePath))
                .map(r -> containsCacheAlready(r))
                .orElse(false);
    }

    protected boolean containsCacheAlready(final Resource cache) {
        return nonNull(cache);
    }

    protected void buildCache(final String rootPagePath) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            buildCache(resourceResolver, rootPagePath);
            resourceResolver.commit();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

    private void buildCache(final ResourceResolver resourceResolver, final String rootPagePath) {
        buildCache(resourceResolver, resourceResolver.getResource(rootPagePath));
    }

}
