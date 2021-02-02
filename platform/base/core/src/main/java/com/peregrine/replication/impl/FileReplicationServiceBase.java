package com.peregrine.replication.impl;

/*-
 * #%L
 * admin base - Core
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

import com.peregrine.replication.ReplicationServiceBase;
import com.peregrine.commons.ResourceUtils;
import com.peregrine.replication.ReferenceLister;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.commons.util.PerUtil.ResourceChecker;
import com.peregrine.render.RenderService;
import com.peregrine.render.RenderService.RenderException;
import com.peregrine.versions.VersioningResourceResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.peregrine.commons.util.PerUtil.*;
import static com.peregrine.replication.ReplicationUtil.markAsActivated;
import static com.peregrine.commons.Chars.DOT;
import static com.peregrine.commons.util.PerConstants.PUBLISHED_LABEL;
import static com.peregrine.commons.util.PerConstants.RENDITION_ACTION;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.NT_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Base Class for External File System / Storage Replications
 *
 * Created by Andreas Schaefer on 5/25/17.
 */
public abstract class FileReplicationServiceBase extends ReplicationServiceBase {

    private static final List<Pattern> NAME_PATTERNS = Collections.singletonList(
            Pattern.compile(".*(\\.data\\.json|\\.html)")
    );
    private static final ResourceChecker EXCLUDED_RESOURCES_RESOURCE_CHECKER = new ResourceChecker() {

        // List of all resources that are excluded from handling
        private final List<String> excludedResources = Arrays.asList(JCR_CONTENT, RENDITIONS);

        @Override
        public boolean doAdd(final Resource resource) {
            return !excludedResources.contains(resource.getName());
        }

        @Override
        public boolean doAddChildren(final Resource resource) {
            return doAdd(resource);
        }

    };

    private static final String EXTENSION_NAME_MUST_BE_PROVIDED = "Extension Name must be provided";
    private static final String EXTENSION_TYPES_MUST_BE_PROVIDED = "Extension Types must be provided";
    public static final String RENDERING_OF_ASSET_FAILED = "Rendering of Asset failed";

    private final RenditionConsumer assetRenditionCreator = (resource, renditionName) -> {
        if (isNotBlank(renditionName)) {
            getRenderService().renderRawInternally(resource, RENDITION_ACTION + SLASH + renditionName);
        }

        return null;
    };
    private final RenditionConsumer assetRenditionReplicator = (resource, renditionName) -> {
        final var initialResolver = resource.getResourceResolver();
        final var targetResolver = new VersioningResourceResolver(initialResolver, PUBLISHED_LABEL);
        final var wrappedResource = targetResolver.wrap(resource);
        if (isNull(wrappedResource)) {
            return null;
        }

        final String extension = isBlank(renditionName) ? EMPTY : RENDITION_ACTION + SLASH + renditionName;
        final byte[] content = getRenderService().renderRawInternally(wrappedResource, extension);
        return storeRendering(resource, renditionName, content);
    };

    @Override
    public List<Resource> filterReferences(final List<Resource> resources) {
        return filterReferences((Collection<Resource>)resources);
    }

    private List<Resource> filterReferences(final Collection<Resource> resources) {
        // Ignore jcr:content as they cannot be rendered to the FS (if needed then we need to map the file names)
        // AS TODO: Check if the resource name can be mapped to a file name and if not ignore it.
        // Also make sure we ignore nodes like jcr:content
        return resources.stream()
                .filter(Objects::nonNull)
                .filter(item -> !item.getPath().contains(JCR_CONTENT))
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> findReferences(Resource startingResource, boolean deep) {
        log.trace("Replicate Resource: '{}', deep: '{}'", startingResource, deep);
        List<Resource> referenceList = getReferenceLister().getReferenceList(true, startingResource, true);
        List<Resource> replicationList = new ArrayList<>();
        // Need to check this list of they need to be replicated first
        referenceList.stream()
                .filter(EXCLUDED_RESOURCES_RESOURCE_CHECKER::doAdd)
                .forEach(replicationList::add);
        // This only returns the referenced resources. Now we need to check if there are any JCR Content nodes to be added as well
        for(Resource reference: replicationList) {
            PerUtil.listMissingResources(reference, EXCLUDED_RESOURCES_RESOURCE_CHECKER, false, replicationList);
        }

        return filterReferences(PerUtil.listMissingResources(startingResource, EXCLUDED_RESOURCES_RESOURCE_CHECKER, deep, replicationList));
    }

    @Override
    public List<Resource> deactivate(Resource startingResource)
        throws ReplicationException
    {
        List<Resource> answer = new ArrayList<>();
        String primaryType = PerUtil.getPrimaryType(startingResource);
        if(ASSET_PRIMARY_TYPE.equals(primaryType)) {
            removeReplica(startingResource, null, false);
            answer.add(startingResource);
        } else if(primaryType.startsWith("per:")) {
            removeReplica(startingResource, NAME_PATTERNS, false);
            answer.add(startingResource);
        } else if(primaryType.equals(NT_FOLDER) || primaryType.equals(SLING_FOLDER) || primaryType.equals(SLING_ORDERED_FOLDER)) {
            removeReplica(startingResource, null, true);
            answer.add(startingResource);
        }
        return answer;
    }

    @Override
    public List<Resource> prepare(final Collection<Resource> resources) throws ReplicationException {
        final List<Resource> answer = filterReferences(resources);
        for (final Resource resource: answer) {
            // Need to figure out the type and replicate accordingly
            String primaryType = PerUtil.getPrimaryType(resource);
            if (ASSET_PRIMARY_TYPE.equals(primaryType)) {
                processAssetRenditions(resource, assetRenditionCreator);
            }
        }

        return answer;
    }

    @Override
    public List<Resource> replicate(Collection<Resource> resourceList) throws ReplicationException {
        log.trace("Replicate Resource List: '{}'", resourceList);
        // Replicate the resources
        final ResourceResolver resourceResolver = ResourceUtils.findResolver(resourceList);
        if (isNull(resourceResolver)) {
            return Collections.emptyList();
        }

        final List<Resource> answer = new LinkedList<>();
        Session session = resourceResolver.adaptTo(Session.class);
        for(Resource item: filterReferences(resourceList)) {
            handleParents(item.getParent());
            // Need to figure out the type and replicate accordingly
            String primaryType = PerUtil.getPrimaryType(item);
            final String path;
            if(ASSET_PRIMARY_TYPE.equals(primaryType)) {
                path = processAssetRenditions(item, assetRenditionReplicator);
            } else {
                path = replicatePerResource(item);
            }

            answer.add(item);
            if (isNotBlank(path)) {
                final Resource contentResource = item.getChild(JCR_CONTENT);
                markAsActivated(requireNonNullElse(contentResource, item), path);
            }
        }

        try {
            session.save();
        } catch(RepositoryException e) {
            log.warn("Failed to save changes replicate parents", e);
        }

        return answer;
    }

    /** @return Sling Request Processor to render pages **/
    abstract RenderService getRenderService();
    /** @return Reference Lister to find referencing nodes **/
    abstract ReferenceLister getReferenceLister();

    /** @return True of the folder is already created on the Target **/
    abstract boolean isFolderOnTarget(String path);

    /** @return Create a folder on the target based on the given Path and return it if it exists **/
    abstract File createTargetFolder(String path) throws ReplicationException;

    private void handleParents(Resource resource) throws ReplicationException {
        // Go through all its parents and make sure the folder does exist
        if(!isFolderOnTarget(resource.getPath())) {
            Resource parent = resource.getParent();
            if(parent != null) {
                handleParents(parent);
            }
            createTargetFolder(resource.getPath());
        }
    }

    /** @return Map listing all extensions and the primary types of all nodes that are exported with that extension **/
    abstract List<ExportExtension> getExportExtensions();
    private List<ExportExtension> getExportExtensions(final Resource resource) {
        return getExportExtensions().stream()
                .filter(e -> e.supportsResource(resource))
                .collect(Collectors.toList());
    }

    /** @return A list of all mandatory renditions which are created during the replication if not already there **/
    abstract List<String> getMandatoryRenditions();

    protected String renderingName(final Resource resource, final String extension) {
        return resource.getName() + (isNotEmpty(extension) ? DOT + extension : EMPTY);
    }

    private String processAssetRenditions(Resource resource, RenditionConsumer consumer) throws ReplicationException {
        if (isNull(resource)) {
            return null;
        }

        try {
            // Get the image data of the resource and write to the target
            final String answer = consumer.consume(resource, EMPTY);
            // Loop over all existing renditions and write the image data to the target
            final List<String> checkRenditions = new ArrayList<>(getMandatoryRenditions());
            final Resource renditions = ResourceUtils.tryToCreateChildOrGetNull(resource, RENDITIONS, SLING_FOLDER);
            for (final Resource rendition : Optional.ofNullable(renditions)
                    .map(Resource::getChildren)
                    .map(Iterable::spliterator)
                    .map(i -> StreamSupport.stream(i, false))
                    .orElseGet(Stream::empty)
                    .filter(r -> NT_FILE.equals(PerUtil.getPrimaryType(r)))
                    .collect(Collectors.toList())) {
                try {
                    final String renditionName = rendition.getName();
                    consumer.consume(resource, renditionName);
                    checkRenditions.remove(renditionName);
                } catch (RenderException e) {
                    log.warn("Rendition: '{}' failed with message: '{}'", rendition.getPath(), e.getMessage());
                    log.warn("Rendition Failure", e);
                }
            }
            // Loop over all remaining mandatory renditions and write the image data to the target
            for(String renditionName : checkRenditions) {
                try {
                    consumer.consume(resource, renditionName);
                } catch(RenderException e) {
                    log.warn("Rendition: '{}' failed with message: '{}'", renditionName, e.getMessage());
                    log.warn("Rendition Failure", e);
                }
            }

            return answer;
        } catch(final RenderException e) {
            throw new ReplicationException(RENDERING_OF_ASSET_FAILED, e);
        }
    }

    /**
     * Store the given Asset Rendering on the target
     * @param resource Resource that is exported
     * @param extension File Extension (without a leading dot)
     * @param content Asset content of the rendering
     * @return Path to the Stored Rendition used for the Rendition Ref property
     * @throws ReplicationException if the writing of the content failed
     */
    String storeRendering(Resource resource, String extension, byte[] content) throws ReplicationException {
        return storeFile(resource.getParent(), renderingName(resource, extension), content);
    }

    /**
     * Removes a given resource from the target
     * @param resource Source Resource which replica is to be removed
     * @param namePattern List of Regex Pattern that will find the replica
     * @param isFolder If true this is a folder to be removed
     * @throws ReplicationException If the removal fails
     */
    abstract void removeReplica(Resource resource, final List<Pattern> namePattern, boolean isFolder) throws ReplicationException;

    private String replicatePerResource(Resource resource) throws ReplicationException {
        String result = null;
        log.trace("Replicate Resource: '{}'", resource.getPath());
        final RenderService renderService = getRenderService();
        for (final ExportExtension exportExtension : getExportExtensions(resource)) {
            final String extension = exportExtension.getFileExtension();
            try {
                final String path;
                if (exportExtension.isRaw()) {
                    log.trace("Before Rendering Raw Resource With Extension: '{}'", extension);
                    final byte[] content = renderService.renderRawInternally(resource, extension);
                    path = storeRendering(resource, extension, content);
                } else {
                    log.trace("Before Rendering String Resource With Extension: '{}'", extension);
                    final String content = renderService.renderInternally(resource, extension);
                    path = storeFile(resource.getParent(), renderingName(resource, extension), content);
                }

                result = StringUtils.defaultIfBlank(result, path);
            } catch (RenderException e) {
                log.warn("Rendering of '{}' failed -> ignore it", resource.getPath());
            }
        }

        return result;
    }

    protected static class ExportExtension {

        private final String name;
        private final List<String> types;

        public ExportExtension(String name, List<String> types) {
            if (StringUtils.isEmpty(name)) {
                throw new IllegalArgumentException(EXTENSION_NAME_MUST_BE_PROVIDED);
            }

            if (isNull(types) || types.isEmpty()) {
                throw new IllegalArgumentException(EXTENSION_TYPES_MUST_BE_PROVIDED);
            }
            this.name = name;
            this.types = types;
        }

        public String getName() {
            return name;
        }

        public boolean supportsResource(final Resource resource) {
            if (types.contains(getPrimaryType(resource))) {
                return true;
            }

            return types.contains(getResourceType(resource));
        }

        public boolean isRaw() {
            return StringUtils.endsWith(name, "~raw");
        }

        public String getFileExtension() {
            String result = name;
            if (isRaw()) {
                result = StringUtils.substringAfterLast(result, "~raw");
            }

            if ("*".equals(result)) {
                return EMPTY;
            }

            return result;
        }

    }

    private interface RenditionConsumer {
        String consume(Resource resource, String renditionName) throws RenderException, ReplicationException;
    }

}
