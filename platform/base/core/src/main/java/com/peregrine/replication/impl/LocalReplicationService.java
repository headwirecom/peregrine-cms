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

import com.google.common.collect.Iterables;
import com.peregrine.commons.ResourceUtils;
import com.peregrine.commons.util.PerUtil.*;
import com.peregrine.replication.ReferenceLister;
import com.peregrine.replication.Replication;
import com.peregrine.replication.ReplicationServiceBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.*;

import static com.peregrine.commons.ResourceUtils.performFlatSafeCopy;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.*;
import static com.peregrine.replication.ReplicationUtil.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * This service replicates resources within the same Peregrine
 * instance but a different folder node.
 *
 * Created by Andreas Schaefer on 5/25/17.
 */
@Component(
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service = Replication.class,
    immediate = true
)
@Designate(ocd = LocalReplicationService.Configuration.class, factory = true)
public class LocalReplicationService
    extends ReplicationServiceBase
{

    public static final String LOCAL_MAPPING_HAS_THE_WRONG_FORMAT = "Local Mapping has the wrong format: '%s'";
    public static final String LOCAL_MAPPING_SOURCE_MUST_BE_ABSOLUTE = "For local Replication local mapping needs to provide a local source (before =) that starts with a '/'. Mapping was: '%s'";
    public static final String LOCAL_MAPPING_TARGET_MUST_BE_ABSOLUTE = "For local Replication local mapping needs to provide a local target (after =) that starts with a '/'. Mapping was: '%s'";
    public static final String LOCAL_SOURCE_NOT_FOUND = "Local Source: '%s' not found. Please fix the local mapping.";
    public static final String LOCAL_TARGET_NOT_FOUND = "Local Target: '%s' not found. Please fix the local mapping or create the local target.";
    public static final String FAILED_TO_DELETE_A_TARGET_RESOURCE = "Failed to delete a target resource: '%s'";

    private String localSource;
    private String localTarget;
    private String localSourceWithSlash;

    @ObjectClassDefinition(
        name = "Peregrine: Local Replication Service",
        description = "Each instance provides the configuration for a Local Replication"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Name",
            description = "Name of the Replication Service"
        )
        String name();
        @AttributeDefinition(
            name = "Description",
            description = "Description of this Replication Service"
        )
        String description();
        @AttributeDefinition(
            name = "Local Mapping",
            description = "JCR Root Path Mapping: <source path>=<target path> (only used if this is local). Anything outside will not be copied."
        )
        String localMapping();
    }
    @Activate
    @SuppressWarnings("unused")
    void activate(Configuration configuration) { setup(configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(Configuration configuration) { setup(configuration); }

    private void setup(Configuration configuration) {
        init(configuration.name(), configuration.description());
        localSource = localTarget = localSourceWithSlash = null;
        final String mapping = configuration.localMapping();
        final String[] tokens = mapping.split(EQUALS);
        if(tokens.length < 2) {
            throw new IllegalArgumentException(String.format(LOCAL_MAPPING_HAS_THE_WRONG_FORMAT, mapping));
        }

        localSource = StringUtils.stripEnd(tokens[0], SLASH);
        localSourceWithSlash = localSource + SLASH;
        localTarget = StringUtils.stripEnd(tokens[1], SLASH);
        if(!StringUtils.startsWith(localSource, SLASH)) {
            throw new IllegalArgumentException(String.format(LOCAL_MAPPING_SOURCE_MUST_BE_ABSOLUTE, mapping));
        }

        if(!StringUtils.startsWith(localTarget, SLASH)) {
            throw new IllegalArgumentException(String.format(LOCAL_MAPPING_TARGET_MUST_BE_ABSOLUTE, mapping));
        }

        log.trace("Local Replication Service Name: '{}' created", getName());
        log.trace("Local Source: '{}', Target: '{}'", localSource, localTarget);
    }

    @Reference
    @SuppressWarnings("unused")
    ResourceResolverFactory resourceResolverFactory;
    @Reference
    @SuppressWarnings("unused")
    private ReferenceLister referenceLister;

    @Override
    public List<Resource> findReferences(final Resource startingResource, final boolean deep) throws ReplicationException {
        ResourceResolver resourceResolver = startingResource.getResourceResolver();
        final Resource source = resolveLocalSource(resourceResolver);
        final Resource target = resolveLocalTarget(resourceResolver);
        final List<Resource> referenceList = referenceLister.getReferenceList(true, startingResource, deep, source, target);
        final List<Resource> replicationList = new ArrayList<>();
        final ResourceChecker resourceChecker = new MissingOrOutdatedResourceChecker(source, target);
        // Need to check this list of they need to be replicated first
        for(final Resource resource: referenceList) {
            if(resourceChecker.doAdd(resource) && !containsResource(replicationList, resource)) {
                replicationList.add(resource);
            }
        }

        // This only returns the referenced resources. Now we need to check if there are any JCR Content nodes to be added as well
        for(final Resource reference: new LinkedList<>(replicationList)) {
            listMissingResources(reference, resourceChecker, false, replicationList);
        }

        listMissingParents(startingResource, replicationList, source, resourceChecker);
        return listMissingResources(startingResource, resourceChecker, deep, replicationList);
    }

    private Resource resolveLocalSource(final ResourceResolver resolver) throws ReplicationException {
        return Optional.ofNullable(localSource)
                .map(resolver::getResource)
                .orElseThrow(() -> new ReplicationException(String.format(LOCAL_SOURCE_NOT_FOUND, localSource)));
    }

    private Resource resolveLocalTarget(final ResourceResolver resolver) throws ReplicationException {
        return Optional.ofNullable(localTarget)
                .map(resolver::getResource)
                .orElseThrow(() -> new ReplicationException(String.format(LOCAL_TARGET_NOT_FOUND, localTarget)));
    }

    @Override
    public List<Resource> deactivate(Resource startingResource)
        throws ReplicationException
    {
        final ResourceResolver resourceResolver = startingResource.getResourceResolver();
        final Resource source = resolveLocalSource(resourceResolver);
        final Resource target = resolveLocalTarget(resourceResolver);
        final List<Resource> replicationList = new ArrayList<>(Collections.singletonList(startingResource));
        final ResourceChecker resourceChecker = new MatchingResourceChecker(source, target);
        listMissingResources(startingResource, resourceChecker, true, replicationList);
        return deactivate(startingResource, replicationList);
    }

    /**
     * This method deactivates the given resource to deactivate it and then updates
     * the given list of source resources with the replication properties
     *
     * @param toBeDeleted The staring resource to be removed which removes all its children
     * @param resourceList The list of the source dependencies to be updated
     * @return List of all updated source dependencies
     * @throws ReplicationException
     */
    private List<Resource> deactivate(Resource toBeDeleted, List<Resource> resourceList) throws ReplicationException {
        final ResourceResolver resourceResolver = ResourceUtils.findResolver(resourceList);
        if (isNull(resourceResolver)) {
            return Collections.emptyList();
        }

        List<Resource> answer = new ArrayList<>();
        // Replicate the resources
        Resource source = resolveLocalSource(resourceResolver);
        resolveLocalTarget(resourceResolver);
        // Update all replication targets by setting the new Replication Date, User and remove the Ref to indicate the deactivation
        for (Resource item : resourceList) {
            markAsDeactivated(item);
        }
        // Delete the replicated target resource
        String relativePath = relativePath(source, toBeDeleted);
        if (relativePath != null) {
            String targetPath = localTarget + '/' + relativePath;
            Resource targetResource = getResource(resourceResolver, targetPath);
            if (targetResource != null) {
                try {
                    resourceResolver.delete(targetResource);
                } catch (PersistenceException e) {
                    throw new ReplicationException(String.format(FAILED_TO_DELETE_A_TARGET_RESOURCE, targetPath), e);
                }
            }
        } else {
            log.warn("Given Resource: '{}' path does not start with local source path: '{}' -> ignore", toBeDeleted, localSource);
        }

        refreshAndCommit(resourceResolver);
        return answer;
    }

    @Override
    public List<Resource> replicate(final Collection<Resource> resourceList) throws ReplicationException {
        final ResourceResolver resourceResolver = ResourceUtils.findResolver(resourceList);
        if (isNull(resourceResolver)) {
            return Collections.emptyList();
        }

        final Collection<Resource> safeResourceList = new LinkedList<>(resourceList);
        Iterables.removeIf(safeResourceList, Objects::isNull);
        final Resource source = resolveLocalSource(resourceResolver);
        final Resource target = resolveLocalTarget(resourceResolver);
        // Prepare the Mappings for the Properties mapping
        final Map<String, String> pathMapping = new HashMap<>();
        for (final Resource item : safeResourceList) {
            String relativePath = relativePath(source, item);
            if (isNotBlank(relativePath)) {
                String targetPath = localTarget + '/' + relativePath;
                log.trace("Add to Path mappings Source Path: '{}', Target Path: '{}'", item.getPath(), targetPath);
                pathMapping.put(item.getPath(), targetPath);
                // References need to be updated through the Path Mappings therefore we revisit them here
                List<Resource> referenceList = referenceLister.getReferenceList(true, item, true, source, target);
                for (Resource reference : referenceList) {
                    relativePath = relativePath(source, reference);
                    if (isNotBlank(relativePath)) {
                        targetPath = localTarget + '/' + relativePath;
                        log.trace("Add to Path mappings Reference Source Path: '{}', Target Path: '{}'", reference.getPath(), targetPath);
                        pathMapping.put(reference.getPath(), targetPath);
                    }
                }
            } else {
                log.warn("Given Resource: '{}' path does not start with local source path: '{}' -> ignore", item, localSource);
            }
        }

        final List<Resource> handledSources = new LinkedList<>();
        final List<Resource> answer = new LinkedList<>();
        for (final Resource item : safeResourceList) {
            handleParents(handledSources, item, answer, pathMapping, resourceResolver);
        }

        refreshAndCommit(resourceResolver);
        return answer;
    }

    private boolean handleParents(List<Resource> handledSources, Resource resource, List<Resource> resourceList, Map<String, String> pathMapping, ResourceResolver resourceResolver) {
        if (containsResource(handledSources, resource)) {
            return true;
        }

        final String targetPath = getTargetPath(resource);
        log.trace("Handle Parents, Resource: '{}', Target Path: '{}'", resource.getPath(), targetPath);
        if (isBlank(targetPath)) {
            return true;
        }

        final String targetParentPath = getParent(targetPath);
        log.trace("Target Parent: '{}'", targetParentPath);
        if (isBlank(targetParentPath)) {
            // No more parent -> handling parents failed
            return false;
        }

        Resource targetParent = resourceResolver.getResource(targetParentPath);
        if (isNull(targetParent)) {
            // Parent does not exist so try with its parent
            final Resource parent = resource.getParent();
            if (isNull(parent)) {
                // No more parent -> handling parents failed
                return false;
            }

            log.trace("Recursive Handle Parents: '{}'", parent.getPath());
            if (!handleParents(handledSources, parent, resourceList, pathMapping, resourceResolver)) {
                // Handling of parent failed -> leaving as failure
                return false;
            }

            targetParent = resourceResolver.getResource(targetParentPath);
            if (isNull(targetParent)) {
                log.error("Target Parent:'" + targetParentPath + "' is still not found even after all parents were handled");
                return false;
            }
        }

        try {
            log.trace("Copy Resource: '{}' to Target: '{}'", resource.getPath(), targetParent.getPath());
            final Resource copy = performFlatSafeCopy(resourceResolver, resource, targetParent, pathMapping::get);
            markAsActivated(resource, copy);
            resourceList.add(copy);
        } catch (final PersistenceException e) {
            log.error("Failed to replicate resource: '{}' -> ignored", resource, e);
        }

        handledSources.add(resource);
        return true;
    }

    private String getTargetPath(final Resource resource) {
        return getTargetPath(resource.getPath());
    }

    private String getTargetPath(final String path) {
        if (StringUtils.startsWith(path, localSourceWithSlash)) {
            return StringUtils.replaceFirst(path, localSource, localTarget);
        }

        return null;
    }

}
