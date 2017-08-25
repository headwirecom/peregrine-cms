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
import com.peregrine.commons.util.PerUtil;
import com.peregrine.commons.util.PerUtil.ResourceChecker;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.servlethelpers.MockRequestPathInfo;
import org.apache.sling.servlethelpers.MockSlingHttpServletRequest;
import org.apache.sling.servlethelpers.MockSlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static com.peregrine.admin.replication.ReplicationUtil.updateReplicationProperties;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.NT_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static com.peregrine.commons.util.PerUtil.RENDITIONS;
import static com.peregrine.commons.util.PerUtil.getPrimaryType;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;

/**
 * Created by schaefa on 5/25/17.
 */
public abstract class BaseFileReplicationService
    implements Replication
{
    public static final String DATA_JSON = "data.json";
    private static final List<Pattern> NAME_PATTERNS = new ArrayList<>();

    static {
        NAME_PATTERNS.add(Pattern.compile(".*\\.data\\.json"));
    }

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
        List<Resource> referenceList = getReferenceLister().getReferenceList(startingResource, true);
        List<Resource> replicationList = new ArrayList<>();
        ResourceChecker resourceChecker = new ResourceChecker() {
            @Override
            public boolean doAdd(Resource resource) {
                return !resource.getName().equals(JCR_CONTENT)
                    && !resource.getName().equals(RENDITIONS);
            }

            @Override
            public boolean doAddChildren(Resource resource) {
                return !resource.getName().equals(JCR_CONTENT)
                    && !resource.getName().equals(RENDITIONS);
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
        List<Resource> replicationList = new ArrayList<>(Arrays.asList(startingResource));
        return deactivate(startingResource, replicationList);
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
                    handleParents(item.getParent());
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
        String primaryType = getPrimaryType(toBeDeleted);
        if(ASSET_PRIMARY_TYPE.equals(primaryType)) {
            removeReplica(toBeDeleted, null, false);
            answer.add(toBeDeleted);
        } else if(primaryType.startsWith("per:")) {
            removeReplica(toBeDeleted, NAME_PATTERNS, false);
            answer.add(toBeDeleted);
        } else if(primaryType.equals(NT_FOLDER) || primaryType.equals(SLING_FOLDER) || primaryType.equals(SLING_ORDERED_FOLDER)) {
            removeReplica(toBeDeleted, null, true);
            answer.add(toBeDeleted);
        }
        return answer;
    }

    abstract SlingRequestProcessor getRequestProcessor();
    abstract ReferenceLister getReferenceLister();

    abstract boolean isFolderOnTarget(String path);

    abstract void createTargetFolder(String path) throws ReplicationException;

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

    private void replicateResource(Resource resource) throws ReplicationException {
        // Need to figure out the type and replicate accordingly
        String primaryType = getPrimaryType(resource);
        if(ASSET_PRIMARY_TYPE.equals(primaryType)) {
            replicateAsset(resource);
        } else {
            replicatePerResource(resource, false);
        }
    }

    abstract Map<String, List<String>> getExportExtensions();
    abstract List<String> getMandatoryRenditions();

    private void replicateAsset(Resource resource) throws ReplicationException {
        // Get the image data of the resource and write to the target
        byte[] imageContent = renderRawResource(resource, "", false);
        storeRendering(resource, "", imageContent);
        // Loop over all existing renditions and write the image data to the target
        List<String> checkRenditions = new ArrayList<>(getMandatoryRenditions());
        Resource renditions = resource.getChild(RENDITIONS);
        if(renditions != null) {
            for(Resource rendition: renditions.getChildren()) {
                if(NT_FILE.equals(getPrimaryType(rendition))) {
                    try {
                        imageContent = renderRawResource(resource, "rendition.json/" + rendition.getName(), true);
                        storeRendering(resource, rendition.getName(), imageContent);
                        checkRenditions.remove(rendition.getName());
                    } catch(ReplicationException e) {
                        log.warn("Rendition: '{}' failed with message: '{}'", rendition.getPath(), e.getMessage());
                        log.warn("Rendition Failure", e);
                    }
                }
            }
        }
        // Loop over all remaining mandatory renditions and write the image data to the target
        for(String renditionName: checkRenditions) {
            try {
                imageContent = renderRawResource(resource, "rendition.json/" + renditionName, true);
                // Get rendition
                if(renditions == null) { renditions = resource.getChild(RENDITIONS); }
                if(renditions != null) {
                    Resource rendition = renditions.getChild(renditionName);
                    if(rendition != null) {
                        storeRendering(resource, rendition.getName(), imageContent);
                    }
                }
            } catch(ReplicationException e) {
                log.warn("Rendition: '{}' failed with message: '{}'", renditionName, e.getMessage());
                log.warn("Rendition Failure", e);
            }
        }
    }

    /**
     * Store the given Content Rendering on the target
     * @param resource Resource that is exported
     * @param extension File Extension (without a leading dot)
     * @param content String content of the rendering
     * @return Path to the Stored Rendition used for the Rendition Ref property
     * @throws ReplicationException if the writing of the content failed
     */
    abstract String storeRendering(Resource resource, String extension, String content) throws ReplicationException;
    /**
     * Store the given Asset Rendering on the target
     * @param resource Resource that is exported
     * @param extension File Extension (without a leading dot)
     * @param content Asset content of the rendering
     * @return Path to the Stored Rendition used for the Rendition Ref property
     * @throws ReplicationException if the writing of the content failed
     */
    abstract String storeRendering(Resource resource, String extension, byte[] content) throws ReplicationException;
    abstract void removeReplica(Resource resource, final List<Pattern> namePattern, boolean isFolder) throws ReplicationException;

    private void replicatePerResource(Resource resource, boolean post) throws ReplicationException {
        // Render the resource as .data.json and then write the content to the
        String primaryType = getPrimaryType(resource);
        for(Entry<String, List<String>> entry: getExportExtensions().entrySet()) {
            String extension = entry.getKey();
            boolean raw = extension.endsWith("~raw");
            if(raw) { extension = extension.substring(0, extension.length() - "~raw".length()); }
            if("*".equals(extension)) { extension = ""; }
            if(entry.getValue().contains(primaryType)) {
                Object renderingContent = null;
                try {
                    if(raw) {
                        renderingContent = renderRawResource(resource, extension, post);
                    } else {
                        renderingContent = renderResource(resource, extension, post);
                    }
                } catch(ReplicationException e) {
                    log.warn("Rendering of '{}' failed -> ignore it", resource.getPath());
                }
                if(renderingContent != null) {
                    log.trace("Rendered Resource: {}", renderingContent);
                    String path;
                    if(raw) {
                        path = storeRendering(resource, extension, (byte[]) renderingContent);
                    } else {
                        path = storeRendering(resource, extension, (String) renderingContent);
                    }
                    Resource contentResource = resource.getChild(JCR_CONTENT);
                    if(contentResource != null) {
                        updateReplicationProperties(contentResource, path, null);
                    }
                }
            }
        }
    }

    private byte[] renderRawResource(Resource resource, String extension, boolean post) throws ReplicationException {
        return renderResource0(resource, extension, post).getOutput();
    }

    private String renderResource(Resource resource, String extension, boolean post) throws ReplicationException {
        return renderResource0(resource, extension, post).getOutputAsString();
    }

    private MockSlingHttpServletResponse renderResource0(Resource resource, String extension, boolean post) throws ReplicationException {
        try {
            MockSlingHttpServletRequest req = new MockSlingHttpServletRequest(resource.getResourceResolver());
            MockRequestPathInfo mrpi = (MockRequestPathInfo) req.getRequestPathInfo();
            mrpi.setResourcePath(resource.getPath());
            mrpi.setExtension((isNotEmpty(extension) ? "." + extension : ""));
            String requestPath = resource.getPath() + (isNotEmpty(extension) ? "." + extension : "");
            log.trace("Render Resource Request Path: '{}'", mrpi);
            MockSlingHttpServletResponse resp = new MockSlingHttpServletResponse();
            getRequestProcessor().processRequest(req, resp, resource.getResourceResolver());
            log.trace("Response Status: '{}'", resp.getStatus());
            //AS TODO: do we need to support redirects (301 / 302)
            if(resp.getStatus() != 200) {
                String content = resp.getOutputAsString();
                log.error("Request of: '{}' failed (status: {}). Output : '{}'", requestPath, resp.getStatus(), content);
                throw new ReplicationException("Request: '" + requestPath + "' failed with status: '" + resp.getStatus() + "'");
            } else {
                return resp;
            }
        } catch(UnsupportedEncodingException e) {
            throw new ReplicationException("Unsupported Encoding while creating the Render Response", e);
        } catch(ServletException | IOException e) {
            throw new ReplicationException("Failed to render resource: " + resource.getPath(), e);
        }
    }
}
