package com.peregrine.assets.impl;

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

import com.peregrine.assets.ResourceResolverFactoryProxy;
import com.peregrine.commons.ResourceUtils;
import com.peregrine.commons.util.PerUtil;
import org.apache.commons.io.FileUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.peregrine.commons.Chars.EQ;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.jackrabbit.JcrConstants.NT_FILE;

@Component(service = JobConsumer.class, immediate = true, property = {
        JobConsumer.PROPERTY_TOPICS + EQ + AssetsToFSResourceChangeJobConsumer.TOPIC })
@Designate(ocd = AssetsToFSResourceChangeJobConsumerConfig.class)
public final class AssetsToFSResourceChangeJobConsumer implements JobConsumer {

    public static final String TOPIC = "com/peregrine/assets/REFRESH_FS_FILES";
    public static final String PN_PATHS = "paths";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private JobManager jobManager;

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    private AssetsToFSResourceChangeJobConsumerConfig config;

    private String rootPath;
    private boolean disabled = true;
    private ServiceRegistration<ResourceChangeListener> resourceChangeListener;

    @Activate
    public void activate(final BundleContext context, final AssetsToFSResourceChangeJobConsumerConfig config) {
        this.config = config;
        rootPath = config.targetFolderRootPath();
        if (isBlank(rootPath)) {
            return;
        }

        if (!prepareRootFolder()) {
            return;
        }

        disabled = !registerResourceChangeListener(context);
        if (!disabled) {
            updateExistingFiles();
        }
    }

    private boolean prepareRootFolder() {
        final File root = new File(rootPath);
        if (!root.exists()) {
            try {
                FileUtils.forceMkdir(root);
            } catch (final IOException e) {
                logger.error("Root Folder could not be created.", e);
                return false;
            }
        }

        try {
            FileUtils.cleanDirectory(root);
        } catch (final IOException e) {
            logger.error("Root Folder could not be cleaned.", e);
            return false;
        }

        return true;
    }

    private boolean registerResourceChangeListener(final BundleContext context) {
        final Hashtable<String, Object> properties = new Hashtable<>();
        final String[] rootPaths = config.sourceAssetsRootPaths();
        if (rootPaths == null || rootPaths.length == 0) {
            return false;
        }

        properties.put(ResourceChangeListener.PATHS, rootPaths);
        properties.put(ResourceChangeListener.CHANGES, new String[] { "ADDED", "CHANGED", "REMOVED" });
        final AssetsToFSResourceChangeListener listener = new AssetsToFSResourceChangeListener(jobManager);
        resourceChangeListener = context.registerService(ResourceChangeListener.class, listener, properties);
        return true;
    }

    private void updateExistingFiles() {
        final Set<String> paths = new HashSet<>();
        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver()) {
            for (final String path : config.sourceAssetsRootPaths()) {
                final Resource resource = resourceResolver.getResource(path);
                paths.addAll(
                        findAllFiles(resource).stream()
                                .map(Resource::getPath)
                                .collect(Collectors.toSet())
                );
            }
        } catch (final LoginException e) {
            return;
        }

        final Map<String, Object> props = new HashMap<>();
        props.put(AssetsToFSResourceChangeJobConsumer.PN_PATHS, paths);
        jobManager.addJob(AssetsToFSResourceChangeJobConsumer.TOPIC, props);
    }

    private Collection<Resource> findAllFiles(final Resource resource) {
        return findAllFiles(resource, new LinkedList<>());
    }

    private Collection<Resource> findAllFiles(final Resource resource, final LinkedList<Resource> target) {
        if (isFile(resource)) {
            target.add(resource);
        }

        if (nonNull(resource)) {
            final Iterator<Resource> children = resource.listChildren();
            while (children.hasNext()) {
                findAllFiles(children.next(), target);
            }
        }

        return target;
    }

    private boolean isFile(Resource resource) {
        return PerUtil.isPrimaryType(resource, NT_FILE);
    }

    @Deactivate
    public void deactivate() {
        if (nonNull(resourceChangeListener)) {
            resourceChangeListener.unregister();
        }
    }

    @Override
    public JobResult process(final Job job) {
        if (disabled) {
            return JobResult.CANCEL;
        }

        final Set<String> initialPaths = job.getProperty(PN_PATHS, Set.class);
        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver()) {
            updateFiles(resourceResolver, initialPaths);
        } catch (final LoginException e) {
            return JobResult.CANCEL;
        }

        return JobResult.OK;
    }

    private void updateFiles(final ResourceResolver resourceResolver, final Set<String> paths) {
        for (final String path : paths) {
            try {
                updateFiles(resourceResolver, path);
            } catch (final IOException e) {
                logger.warn("File System operation did not succeed.", e);
            }
        }
    }

    private void updateFiles(final ResourceResolver resourceResolver, final String path) throws IOException {
        final Resource resource = resourceResolver.getResource(path);
        if (isNull(resource)) {
            deleteMissingAncestorFolder(resourceResolver, path);
        } else {
            updateFiles(resource);
        }
    }

    private void updateFiles(final Resource resource) throws IOException {
        if (isFile(resource)) {
            updateResource(resource);
        }

        for (final Resource child : resource.getChildren()) {
            updateFiles(child);
        }
    }

    private void deleteMissingAncestorFolder(final ResourceResolver resourceResolver, final String path) throws IOException {
        final Resource ancestor = ResourceUtils.getFirstExistingAncestorOnPath(resourceResolver, path);
        if (isNull(ancestor)) {
            return;
        }

        final String ancestorPath = ancestor.getPath();
        final String subPath = substringAfter(path, ancestorPath);
        if (isBlank(subPath)) {
            return;
        }

        final String name = substringBefore(subPath.substring(1), SLASH);
        final String folderPath = localPath(ancestorPath, name);
        final File folder = new File(folderPath);
        if (folder.exists()) {
            if (folder.isFile()) {
                folder.delete();
            } else {
                FileUtils.deleteDirectory(folder);
            }
        }
    }

    private String localPath(final String... parts) {
        final StringBuilder result = new StringBuilder(rootPath);
        for (final String part : parts) {
            if (!startsWith(part, SLASH)) {
                result.append(SLASH);
            }

            result.append(ResourceUtils.jcrPathToFilePath(part));
        }

        return result.toString();
    }

    private void updateResource(final Resource resource) throws IOException {
        final String filePath = localPath(resource.getPath());
        final File file = new File(filePath);
        FileUtils.forceMkdirParent(file);
        FileUtils.copyInputStreamToFile(resource.adaptTo(InputStream.class), file);
    }

}
