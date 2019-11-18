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

import java.util.*;

import static com.peregrine.commons.util.PerConstants.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component(service = SiteMapStructureCache.class, immediate = true)
@Designate(ocd = SiteMapStructureCacheImplConfig.class)
public final class SiteMapStructureCacheImpl extends CacheBuilderBase
        implements SiteMapStructureCache, Callback<String>, SiteMapEntry.MapPropertiesVisitor<Resource> {

    public static final String SLASH_JCR_CONTENT = SLASH + JCR_CONTENT;

    private final Set<RefreshListener> refreshListeners = new HashSet<>();

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    private SiteMapStructureCacheImplConfig config;

    private DeBouncer<String> deBouncer;

    @Activate
    public void activate(final SiteMapStructureCacheImplConfig config) {
        this.config = config;
        setLocation(config.location());
        deBouncer = new DeBouncer<>(this, config.debounceInterval());
        rebuildAll();
    }

    @Deactivate
    public void deactivate() {
        deBouncer.terminate();
    }

    @Override
    public List<SiteMapEntry> get(final Resource rootPage) {
        try (final ResourceResolver resourceResolver = getServiceResourceResolver()) {
            final Resource cache = getCache(resourceResolver, rootPage);
            if (isNull(cache)) {
                return null;
            }

            return extractEntriesFromChildren(cache);
        } catch (final LoginException e) {
            logger.error(COULD_NOT_GET_SERVICE_RESOURCE_RESOLVER, e);
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

    @Override
    public void call(final String rootPagePath) {
        buildCache(rootPagePath);
    }

    @Override
    protected ResourceResolver getServiceResourceResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver();
    }

    @Override
    protected String getCachePath(final String rootPagePath) {
        return location + rootPagePath + SLASH_JCR_CONTENT;
    }

    @Override
    protected String getOriginalPath(final String cachePath) {
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

    @Override
    protected Resource buildCache(final Resource rootPage, final Resource cache) throws PersistenceException {
        final SiteMapExtractor extractor = siteMapExtractorsContainer.findFirstFor(rootPage);
        if (isNull(extractor)) {
            putSiteMapsInCache(null, cache);
            notifyCacheRefreshed(rootPage, null);
            return null;
        }

        final List<SiteMapEntry> entries = extractor.extract(rootPage);
        putSiteMapsInCache(entries, cache);
        notifyCacheRefreshed(rootPage, entries);

        return cache;
    }

    private void putSiteMapsInCache(final List<SiteMapEntry> source, final Resource target) throws PersistenceException {
        final int siteMapsSize = nonNull(source) ? source.size() : 0;
        final Iterator<SiteMapEntry> iterator = source.iterator();
        final ResourceResolver resourceResolver = target.getResourceResolver();
        for (int i = 0; i < siteMapsSize; i++) {
            final String childName = Integer.toString(i);
            Resource child = target.getChild(childName);
            if (nonNull(child)) {
                resourceResolver.delete(child);
            }

            iterator.next().walk(this, createNode(target, childName));
        }

        removeCachedItemsAboveIndex(target, siteMapsSize);
    }

    private Resource createNode(final Resource target, final String name)
            throws PersistenceException {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
        final ResourceResolver resourceResolver = target.getResourceResolver();
        return resourceResolver.create(target, name, properties);
    }

    @Override
    public Resource visit(final String childName, final Resource resource) {
        try {
            return createNode(resource, childName);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
        }

        return null;
    }

    @Override
    public void visit(final Map<String, Object> properties, final Resource resource) {
        resource.adaptTo(ModifiableValueMap.class).putAll(properties);
    }

    private void notifyCacheRefreshed(final Resource rootPage, final List<SiteMapEntry> entries) {
        new Thread(() -> {
            for (final RefreshListener listener : refreshListeners) {
                listener.onCacheRefreshed(rootPage, entries);
            }
        }).start();
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
    protected void rebuildImpl(final String rootPagePath) {
        deBouncer.call(rootPagePath);
    }

    @Override
    protected void rebuildMandatoryContent() {
        if (isNull(config) || isNull(config.mandatoryCachedRootPaths())) {
            return;
        }

        for (final String path : config.mandatoryCachedRootPaths()) {
            rebuildImpl(path);
        }
    }

    @Override
    public void addRefreshListener(final RefreshListener listener) {
        synchronized (refreshListeners) {
            refreshListeners.add(listener);
        }
    }

    @Override
    public void removeRefreshListener(final RefreshListener listener) {
        synchronized (refreshListeners) {
            refreshListeners.remove(listener);
        }
    }
}
