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

import com.peregrine.commons.ResourceUtils;
import com.peregrine.sitemap.*;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import java.util.*;

import static java.util.Objects.isNull;

@Component(service = SiteMapFilesCache.class)
@Designate(ocd = SiteMapFilesCacheImplConfig.class)
public final class SiteMapFilesCacheImpl extends CacheBuilderBase
        implements SiteMapFilesCache, SiteMapStructureCache.RefreshListener {

    private static final String MAIN_SITE_MAP_KEY = Integer.toString(0);

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Reference
    private SiteMapStructureCache structureCache;

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    @Reference
    private SiteMapFileContentBuilder siteMapBuilder;

    private int maxEntriesCount;
    private int maxFileSize;

    @Activate
    public void activate(final SiteMapFilesCacheImplConfig config) {
        structureCache.addRefreshListener(this);

        setLocation(config.location());

        maxEntriesCount = config.maxEntriesCount();
        if (maxEntriesCount <= 0) {
            maxEntriesCount = Integer.MAX_VALUE;
        }

        maxFileSize = config.maxFileSize();
        if (maxFileSize <= 0) {
            maxFileSize = Integer.MAX_VALUE;
        }

        rebuildAll();
    }

    @Deactivate
    public void deactivate() {
        structureCache.removeRefreshListener(this);
    }

    @Override
    public String get(final Resource rootPage, final int index) {
        final String key = Integer.toString(index);
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            return Optional.ofNullable(rootPage)
                    .filter(ResourceUtils::exists)
                    .map(r -> getCache(resourceResolver, r))
                    .map(Resource::getValueMap)
                    .map(props -> props.get(key, String.class))
                    .orElse(null);
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
            return null;
        }
    }

    @Override
    protected ResourceResolver getServiceResourceResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver();
    }

    @Override
    protected boolean containsCacheAlready(final Resource cache) {
        return Optional.ofNullable(cache)
                .map(Resource::getValueMap)
                .map(vm -> vm.containsKey(MAIN_SITE_MAP_KEY))
                .orElse(false);
    }

    @Override
    protected Resource buildCache(final Resource rootPage, final Resource cache) {
        final List<SiteMapEntry> entries = structureCache.get(rootPage);
        return buildCache(rootPage, entries, cache);
    }

    private Resource buildCache(final Resource rootPage, final List<SiteMapEntry> entries, final Resource cache) {
        final SiteMapExtractor extractor = siteMapExtractorsContainer.findFirstFor(rootPage);
        if (isNull(entries) || isNull(extractor)) {
            final ModifiableValueMap modifiableValueMap = cache.adaptTo(ModifiableValueMap.class);
            removeCachedItemsAboveIndex(modifiableValueMap, 0);
            return null;
        }

        final ArrayList<String> siteMaps = new ArrayList<>();
        final LinkedList<List<SiteMapEntry>> splitEntries = splitEntries(entries);

        if (splitEntries.size() > 1) {
            siteMaps.add(siteMapBuilder.buildSiteMapIndex(rootPage, extractor, splitEntries));
        }

        for (final List<SiteMapEntry> list : splitEntries) {
            final SiteMapConfiguration config = extractor.getConfiguration();
            final String content = siteMapBuilder.buildUrlSet(list, config.getXmlNamespaces());
            siteMaps.add(content);
        }

        putSiteMapsInCache(siteMaps, cache);

        return cache;
    }

    private void putSiteMapsInCache(final ArrayList<String> source, final Resource target) {
        final ModifiableValueMap modifiableValueMap = target.adaptTo(ModifiableValueMap.class);
        final int siteMapsSize = source.size();
        for (int i = 0; i < siteMapsSize; i++) {
            modifiableValueMap.put(Integer.toString(i), source.get(i));
        }

        removeCachedItemsAboveIndex(modifiableValueMap, siteMapsSize);
    }

    private void removeCachedItemsAboveIndex(final ModifiableValueMap modifiableValueMap, final int indexOfStartItem) {
        int i = indexOfStartItem;
        String key = Integer.toString(i);
        while (modifiableValueMap.containsKey(key)) {
            modifiableValueMap.remove(key);
            key = Integer.toString(++i);
        }
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

    @Override
    protected void rebuildImpl(final String rootPagePath) {
        buildCache(rootPagePath);
    }

    @Override
    public void onCacheRefreshed(final Resource rootPage, final List<SiteMapEntry> entries) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            final Resource cache = getOrCreateCacheResource(resourceResolver, rootPage);
            buildCache(rootPage, entries, cache);
            resourceResolver.commit();
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_CHANGES_TO_REPOSITORY, e);
        }
    }

}
