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
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;

@Component(service = SiteMapCache.class)
@Designate(ocd = SiteMapCacheImplConfig.class)
public final class SiteMapCacheImpl implements SiteMapCache {

    private static final String COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER = "Could not get Service Resource Resolver.";
    private static final String COULD_NOT_SAVE_SITE_MAP_CACHE = "Could not save Site Map Cache.";
    private static final String COULD_NOT_SAVE_CHANGES_TO_REPOSITORY = "Could not save changes to repository.";
    private static final String MAIN_SITE_MAP_KEY = Integer.toString(0);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    @Reference
    private SiteMapBuilder siteMapBuilder;

    private String location;
    private String locationWithSlash;
    private int maxEntriesCount;
    private int maxFileSize;

    @Activate
    public void activate(final SiteMapCacheImplConfig config) {
        location = config.location();
        locationWithSlash = location + SLASH;

        maxEntriesCount = config.maxEntriesCount();
        if (maxEntriesCount <= 0) {
            maxEntriesCount = Integer.MAX_VALUE;
        }

        maxFileSize = config.maxFileSize();
        if (maxFileSize <= 0) {
            maxFileSize = Integer.MAX_VALUE;
        }
    }

    @Override
    public String get(final Resource rootPage, final int index) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            final Resource cache;
            if (isCached(resourceResolver, rootPage.getPath())) {
                final String cachePath = getCachePath(rootPage);
                cache = resourceResolver.getResource(cachePath);
            } else {
                cache = buildCache(resourceResolver, rootPage);
                resourceResolver.commit();
            }

            final ValueMap properties = cache.getValueMap();
            final String key = Integer.toString(index);
            return properties.get(key, String.class);
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
            return null;
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
            return null;
        }
    }

    private ResourceResolver getServiceResourceResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver();
    }

    private boolean isCached(final ResourceResolver resourceResolver, final String path) {
        final String cachePath = getCachePath(path);
        return Optional.of(resourceResolver)
                .map(rr -> rr.getResource(cachePath))
                .map(r -> containsCacheAlready(r))
                .orElse(false);
    }

    private boolean containsCacheAlready(final Resource cache) {
        return Optional.ofNullable(cache)
                .map(Resource::getValueMap)
                .map(vm -> vm.containsKey(MAIN_SITE_MAP_KEY))
                .orElse(false);
    }

    private String getCachePath(final Resource rootPage) {
        return getCachePath(rootPage.getPath());
    }

    private String getCachePath(final String rootPagePath) {
        return location + rootPagePath;
    }

    private String getOriginalPath(final Resource cache) {
        return getOriginalPath(cache.getPath());
    }

    private String getOriginalPath(final String cachePath) {
        if (!StringUtils.startsWith(cachePath, locationWithSlash)) {
            return null;
        }

        return StringUtils.substringAfter(cachePath, location);
    }

    private Resource buildCache(final ResourceResolver resourceResolver, final Resource rootPage) {
        try {
            final String cachePath = getCachePath(rootPage);
            final Resource cache = getOrCreateCacheResource(resourceResolver, cachePath);
            return buildCache(rootPage, cache);
        } catch (final RepositoryException e) {
            logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
            return null;
        }
    }

    private Resource buildCache(final Resource rootPage, final Resource cache) {
        final SiteMapExtractor extractor = siteMapExtractorsContainer.findFirstFor(rootPage);
        if (extractor == null) {
            return null;
        }

        final Collection<SiteMapEntry> entries = extractor.extract(rootPage);
        final LinkedList<List<SiteMapEntry>> splitEntries = splitEntries(entries);
        final ArrayList<String> siteMaps = new ArrayList<>();
        final int numberOfParts = splitEntries.size();
        if (numberOfParts > 1) {
            siteMaps.add(siteMapBuilder.buildSiteMapIndex(rootPage, extractor, numberOfParts));
        }

        for (final List<SiteMapEntry> list : splitEntries) {
            siteMaps.add(siteMapBuilder.buildUrlSet(list));
        }

        putSiteMapsInCache(siteMaps, cache);

        return cache;
    }

    private Resource getOrCreateCacheResource(final ResourceResolver resourceResolver, final String path)
            throws RepositoryException {
        final Resource resource = Utils.getFirstExistingAncestorOnPath(resourceResolver, path);
        final String missingPath;
        if (resource == null) {
            missingPath = path;
        } else {
            missingPath = StringUtils.substringAfter(path, resource.getPath());
        }

        final String[] missing = StringUtils.split(missingPath, SLASH);
        Node node = resource.adaptTo(Node.class);
        for (final String name : missing) {
            node = node.addNode(name, SLING_FOLDER);
        }

        node.getSession().save();
        return resourceResolver.getResource(path);
    }

    private LinkedList<List<SiteMapEntry>> splitEntries(final Collection<SiteMapEntry> entries) {
        final int baseSiteMapLength = siteMapBuilder.getBaseSiteMapLength();
        final LinkedList<List<SiteMapEntry>> result = new LinkedList<>();
        int index = 0;
        int size = baseSiteMapLength;
        List<SiteMapEntry> split = new LinkedList<>();
        result.add(split);
        for (final SiteMapEntry entry : entries) {
            final int entrySize = siteMapBuilder.getSize(entry);
            if (index < maxEntriesCount && size + entrySize <= maxFileSize) {
                index++;
                size += entrySize;
            } else {
                index = 1;
                size = baseSiteMapLength;
                result.add(split = new LinkedList<>());
            }

            split.add(entry);
        }

        return result;
    }

    private void putSiteMapsInCache(ArrayList<String> source, Resource target) {
        final ModifiableValueMap modifiableValueMap = target.adaptTo(ModifiableValueMap.class);
        final int siteMapsSize = source.size();
        for (int i = 0; i < siteMapsSize; i++) {
            modifiableValueMap.put(Integer.toString(i), source.get(i));
        }

        removeCachedItemsAboveIndex(modifiableValueMap, siteMapsSize);
    }

    private void removeCachedItemsAboveIndex(final ModifiableValueMap modifiableValueMap, final int startItemIndex) {
        int i = startItemIndex;
        String key = Integer.toString(i);
        while (modifiableValueMap.containsKey(key)) {
            modifiableValueMap.remove(key);
            key = Integer.toString(++i);
        }
    }

    @Override
    public void rebuild(final String rootPagePath) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            cleanRemovedChildren(resourceResolver, rootPagePath);
            String path = rootPagePath;
            while (StringUtils.isNotBlank(path)) {
                if (isCached(resourceResolver, path)) {
                    buildCache(resourceResolver, path);
                }

                path = StringUtils.substringBeforeLast(path, SLASH);
            }

            resourceResolver.commit();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final RepositoryException | PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

    private void cleanRemovedChildren(final ResourceResolver resourceResolver, final String rootPagePath)
            throws RepositoryException {
        final Resource cache = resourceResolver.getResource(getCachePath(rootPagePath));
        final Resource rootPage = resourceResolver.getResource(rootPagePath);
        cleanRemovedChildren(cache, rootPage);
    }

    private void cleanRemovedChildren(final Resource cache, final Resource rootPage)
            throws RepositoryException {
        if (cache == null) {
            return;
        }

        if (rootPage == null) {
            cache.adaptTo(Node.class).remove();
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
            final String cacheRoot = getCachePath(StringUtils.EMPTY);
            final Resource root = resourceResolver.getResource(cacheRoot);
            if (root != null) {
                rebuildCacheInTree(resourceResolver, root);
            }

            resourceResolver.commit();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final RepositoryException | PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

    private void rebuildCacheInTree(final ResourceResolver resourceResolver, final Resource cache) {
        final Iterator<Resource> iterator = cache.listChildren();
        if (containsCacheAlready(cache)) {
            final String rootPagePath = getOriginalPath(cache);
            final Resource rootPage = resourceResolver.getResource(rootPagePath);
            buildCache(rootPage, cache);
        }

        while (iterator.hasNext()) {
            rebuildCacheInTree(resourceResolver, iterator.next());
        }
    }
}
