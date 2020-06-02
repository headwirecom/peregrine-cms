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

import com.peregrine.commons.concurrent.Callback;
import com.peregrine.commons.concurrent.DeBouncer;
import com.peregrine.sitemap.*;
import org.apache.sling.api.resource.*;
import org.apache.sling.serviceusermapping.ServiceUserMapped;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import java.util.*;

import static com.peregrine.commons.ResourceUtils.fileNameToJcrName;
import static com.peregrine.commons.ResourceUtils.jcrNameToFileName;
import static com.peregrine.commons.util.PerConstants.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

@Component(service = SiteMapStructureCache.class, immediate = true)
@Designate(ocd = SiteMapStructureCacheImplConfig.class)
public final class SiteMapStructureCacheImpl extends CacheBuilderBase
        implements SiteMapStructureCache, Callback<String>, SiteMapEntry.Visitor<Resource> {

    private static final String SLASH_JCR_CONTENT = SLASH + JCR_CONTENT;

    private final Set<RefreshListener> refreshListeners = new HashSet<>();

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Reference
    private SiteMapExtractorsContainer siteMapExtractorsContainer;

    @Reference
    private SiteMapConfigurationsContainer siteMapConfigurationsContainer;

    // This reference makes sure that the Service User is available when this service activates
    @Reference(target = "(subServiceName=sitemaps)")
    private ServiceUserMapped wfxServices;

    private DeBouncer<String> deBouncer;

    @Activate
    public void activate(final SiteMapStructureCacheImplConfig config) {
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
        final Map<String, Object> properties = transformToMap(resource);
        final String path = String.valueOf(properties.remove(PATH));
        final SiteMapEntry entry = new SiteMapEntry(path);
        for (final Map.Entry<String, Object> e : properties.entrySet()) {
            final String key = e.getKey();
            final Object value = e.getValue();
            if (value instanceof String) {
                entry.putProperty(key, (String)value);
            } else {
                entry.putProperty(key, value);
            }
        }

        return entry;
    }

    private Map<String, Object> transformToMap(final Resource resource) {
        final ValueMap properties = resource.getValueMap();
        final Map<String, Object> result = new HashMap<>();
        for (final Map.Entry<String, Object> e : properties.entrySet()) {
            final String key = e.getKey();
            if (!key.startsWith("jcr:")) {
                final Object value = e.getValue();
                result.put(fileNameToJcrName(key), value);
            }
        }

        for (final Resource child : resource.getChildren()) {
            result.put(fileNameToJcrName(child.getName()), transformToMap(child));
        }

        return result;
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
        final String cachePath = super.getCachePath(rootPagePath);
        return isRepositoryRoot(rootPagePath) ? cachePath : cachePath + SLASH_JCR_CONTENT;
    }

    @Override
    protected String getOriginalPath(final String cachePath) {
        final String superOriginalPath = super.getOriginalPath(cachePath);
        if (!endsWith(superOriginalPath, SLASH_JCR_CONTENT)) {
            return null;
        }

        return substringBeforeLast(superOriginalPath, SLASH_JCR_CONTENT);
    }

    @Override
    protected Resource buildCache(final Resource rootPage, final Resource cache) throws PersistenceException {
        final SiteMapExtractor extractor = siteMapExtractorsContainer.findFirstFor(rootPage);
        if (isNull(extractor)) {
            removeCachedItemsStartingAtIndex(cache, 0);
            // TODO remove ancestors on path up the tree?
            notifyCacheRefreshed(rootPage, null);
            return null;
        }

        final List<SiteMapEntry> entries = extractor.extract(rootPage);
        putSiteMapsInCache(entries, cache);
        notifyCacheRefreshed(rootPage, entries);

        return cache;
    }

    private void putSiteMapsInCache(final List<SiteMapEntry> source, final Resource target) throws PersistenceException {
        final int siteMapsSize = source.size();
        final Iterator<SiteMapEntry> iterator = source.iterator();
        final ResourceResolver resourceResolver = target.getResourceResolver();
        for (int i = 0; i < siteMapsSize; i++) {
            final String childName = Integer.toString(i);
            final Resource child = target.getChild(childName);
            if (nonNull(child)) {
                resourceResolver.delete(child);
            }

            final SiteMapEntry entry = iterator.next();
            Optional.ofNullable(entry.walk(this, target, childName))
                    .map(r -> r.getChild(childName))
                    .map(r -> r.adaptTo(ModifiableValueMap.class))
                    .ifPresent(map -> map.put(PATH, entry.getPath()));
        }

        removeCachedItemsStartingAtIndex(target, siteMapsSize);
    }

    @Override
    public Resource visit(final String childName, final Map<String, String> properties, final Resource resource) {
        try {
            final Map<String, Object> props = transformToJcrNames(properties);
            props.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            final ResourceResolver resourceResolver = resource.getResourceResolver();
            return resourceResolver.create(resource, jcrNameToFileName(childName), props);
        } catch (final PersistenceException e) {
            logger.error(COULD_NOT_SAVE_SITE_MAP_CACHE, e);
        }

        return null;
    }

    private Map<String, Object> transformToJcrNames(final Map<String, ?> map) {
        final Map<String, Object> result = new HashMap<>();
        for (final Map.Entry<String, ?> e : map.entrySet()) {
            result.put(jcrNameToFileName(e.getKey()), e.getValue());
        }

        return result;
    }

    @Override
    public Resource visit(final String propertyName, final String propertyValue, final Resource resource) {
        return resource;
    }

    @Override
    public Resource endVisit(final String mapName, final Resource resource) {
        return resource.getParent();
    }

    private void notifyCacheRefreshed(final Resource rootPage, final List<SiteMapEntry> entries) {
        new Thread(() -> {
            for (final RefreshListener listener : refreshListeners) {
                listener.onCacheRefreshed(rootPage, entries);
            }
        }).start();
    }

    private void removeCachedItemsStartingAtIndex(final Resource target, final int startItemIndex) throws PersistenceException {
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
        for (final SiteMapConfiguration config : siteMapConfigurationsContainer.getAll()) {
            for (final String path : config.getMandatoryCachedPaths()) {
                rebuildImpl(path);
            }
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
