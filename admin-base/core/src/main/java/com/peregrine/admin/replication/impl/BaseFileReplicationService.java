package com.peregrine.admin.replication.impl;

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

import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.admin.replication.Replication;
import com.peregrine.admin.replication.ReplicationUtil;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.commons.util.PerUtil.MatchingResourceChecker;
import com.peregrine.commons.util.PerUtil.MissingOrOutdatedResourceChecker;
import com.peregrine.commons.util.PerUtil.ResourceChecker;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.admin.replication.ReplicationUtil.updateReplicationProperties;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED_BY;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATION_REF;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getPrimaryType;
import static com.peregrine.commons.util.PerUtil.getResource;

/**
 * Created by schaefa on 5/25/17.
 */
public abstract class BaseFileReplicationService
    implements Replication
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Resource> replicate(Resource startingResource, boolean deep)
        throws ReplicationException
    {
        ResourceResolver resourceResolver = startingResource.getResourceResolver();
        List<Resource> referenceList = getReferenceLister().getReferenceList(startingResource, true);
        List<Resource> replicationList = new ArrayList<>();
        replicationList.add(startingResource);
        ResourceChecker resourceChecker = new ResourceChecker() {
            @Override
            public boolean doAdd(Resource resource) {
                return !resource.getName().equals(PerConstants.JCR_CONTENT);
            }

            @Override
            public boolean doAddChildren(Resource resource) {
                return !resource.getName().equals(PerConstants.JCR_CONTENT);
            }
        };
        // Need to check this list of they need to be replicated first
        for(Resource resource: referenceList) {
            if(resourceChecker.doAdd(resource)) {
                replicationList.add(resource);
            }
        }
        // This only returns the referenced resources. Now we need to check if there are any JCR Content nodes to be added as well
        for(Resource reference: new ArrayList<Resource>(replicationList)) {
            PerUtil.listMissingResources(reference, replicationList, resourceChecker, false);
        }
        PerUtil.listMissingResources(startingResource, replicationList, resourceChecker, deep);
        return replicate(replicationList);
    }

    @Override
    public List<Resource> deactivate(Resource startingResource)
        throws ReplicationException
    {
//        ResourceResolver resourceResolver = startingResource.getResourceResolver();
//        Resource source = resourceResolver.getResource(localSource);
//        if(source == null) {
//            throw new ReplicationException("Local Source: '" + localSource + "' not found. Please fix the local mapping.");
//        }
//        Resource target = resourceResolver.getResource(localTarget);
//        if(target == null) {
//            throw new ReplicationException("Local Target: '" + localTarget + "' not found. Please fix the local mapping or create the local target.");
//        }
//
//        List<Resource> replicationList = new ArrayList<>(Arrays.asList(startingResource));
//        ResourceChecker resourceChecker = new MatchingResourceChecker(source, target);
//        PerUtil.listMatchingResources(startingResource, replicationList, resourceChecker, true);
//        return deactivate(startingResource, replicationList);
        return null;
    }

    @Override
    public List<Resource> replicate(List<Resource> resourceList) throws ReplicationException {
        List<Resource> answer = new ArrayList<>();
        // Replicate the resources
        ResourceResolver resourceResolver = null;
        for(Resource item: resourceList) {
            if(item != null) {
                resourceResolver = item.getResourceResolver();
                break;
            }
        }
        if(resourceResolver != null) {
            Session session = resourceResolver.adaptTo(Session.class);
            for(Resource item: resourceList) {
                if(item != null) {
                    handleParents(item.getParent(), resourceResolver);
                    replicateResource(item);
                    answer.add(item);
                }
            }
            try {
                session.save();
            } catch(RepositoryException e) {
                log.warn("Failed to save changes replicate parents", e);
            }
        }
        return answer;
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
    public List<Resource> deactivate(Resource toBeDeleted, List<Resource> resourceList) throws ReplicationException {
        List<Resource> answer = new ArrayList<>();
        return answer;
    }

    abstract SlingRequestProcessor getRequestProcessor();
    abstract ReferenceLister getReferenceLister();

    abstract boolean isFolderOnTarget(String path);

    abstract void createTargetFolder(String path) throws ReplicationException;

    private void handleParents(Resource resource, ResourceResolver resourceResolver) throws ReplicationException {
        // Go through all its parents and make sure the folder does exist
        if(!isFolderOnTarget(resource.getPath())) {
            Resource parent = resource.getParent();
            if(parent != null) {
                handleParents(parent, resourceResolver);
            }
            createTargetFolder(resource.getPath());
        }
    }

    private void replicateResource(Resource resource) throws ReplicationException {
        // Need to figure out the type and replicate accordingly
        String primaryType = getPrimaryType(resource);
        if(ASSET_PRIMARY_TYPE.equals(primaryType)) {
            replicateAsset(resource);
        } else if(primaryType.startsWith("per:")) {
            replicatePerResource(resource);
        } else {
            // Ignore
            log.warn("Unsupported Primary Type: '{}' for Resource: '{}'", primaryType, resource.getPath());
        }
    }

    private void replicateAsset(Resource resource) {

    }

    abstract String storeRendering(Resource resource, String extension, String content) throws ReplicationException;

    private void replicatePerResource(Resource resource) throws ReplicationException {
        // Render the resource as .data.json and then write the content to the
        String extension = ".data.json";
        String renderingContent = renderResource(resource, extension);
        log.trace("Rendered Resource: {}", renderingContent);
        String path = storeRendering(resource, extension, renderingContent);
        Resource contentResource = resource.getChild(JCR_CONTENT);
        if(contentResource != null) {
            updateReplicationProperties(contentResource, path, null);
        }
    }

    private String renderResource(Resource resource, String extension) throws ReplicationException {
        try {
            final HttpRequest req = new HttpRequest(resource.getPath() + extension);
            final HttpResponse resp = new HttpResponse();
            getRequestProcessor().processRequest(req, resp, resource.getResourceResolver());
            return resp.getContent();
        } catch(UnsupportedEncodingException e) {
            throw new ReplicationException("Unsuported Encoding while creating the Render Response", e);
        } catch(ServletException | IOException e) {
            throw new ReplicationException("Failed to render resource: " + resource.getPath(), e);
        }
    }
}
