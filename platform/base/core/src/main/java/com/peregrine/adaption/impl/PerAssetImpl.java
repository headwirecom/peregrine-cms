package com.peregrine.adaption.impl;

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

import com.peregrine.adaption.PerAsset;
import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.Nullable;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.NT_RESOURCE;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerUtil.METADATA;
import static com.peregrine.commons.util.PerUtil.RENDITIONS;

/**
 * Peregrine Asset Wrapper Object
 *
 * Created by Andreas Schaefer on 6/4/17.
 */
public class PerAssetImpl
    extends PerBaseImpl
    implements PerAsset
{

    public static final String JCR = "jcr:";

    public PerAssetImpl(Resource resource) {
        super(resource);
    }

    @Override
    public InputStream getRenditionStream(String name) {
        if(name == null) {
            return getRenditionStream((Resource) null);
        }
        Iterable<Resource> renditions = listRenditions();
        for(Resource rendition: renditions) {
            if(rendition.getName().equals(name)) {
                return getRenditionStream(rendition);
            }
        }
        return null;
    }

    @Override
    public InputStream getRenditionStream(Resource resource) {
        Resource jcrContent = resource != null ?
            resource.getChild(JCR_CONTENT) :
            getContentResource();
        if(jcrContent != null) {
            ValueMap properties = jcrContent.getValueMap();
            return properties.get(JCR_DATA, InputStream.class);
        } else {
            return null;
        }
    }

    @Override
    public Iterable<Resource> listRenditions() {
        Resource renditions = null;
        try {
            renditions = getRenditionsResource(false);
        } catch(PersistenceException e) {
            // Ignore
        }
        if(renditions != null) {
            return renditions.getChildren();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void addRendition(String renditionName, InputStream dataStream, String mimeType)
        throws PersistenceException, RepositoryException
    {
        Session session = adaptTo(Session.class);
        if(session == null) { throw new RepositoryException("Could not adapt asset to session"); }
        Resource renditions = getRenditionsResource(true);
        Node renditionsNode = renditions.adaptTo(Node.class);
        if(renditionsNode == null) { throw new RepositoryException("Renditions Resource could not be adapted to Node"); }
        Node renditionNode = renditionsNode.addNode(renditionName, NT_FILE);
        Node jcrContent = renditionNode.addNode(JCR_CONTENT, NT_RESOURCE);
        Binary data = session.getValueFactory().createBinary(dataStream);
        jcrContent.setProperty(JCR_DATA, data);
        jcrContent.setProperty(JCR_MIME_TYPE, mimeType);
    }

    @Override
    public void addTag(String category, String tag, Object value)
        throws PersistenceException, RepositoryException
    {
        if(value != null) {
            Resource categoryResource = getCategoryResource(category, true);
            ModifiableValueMap properties = categoryResource.adaptTo(ModifiableValueMap.class);
            if(properties != null) {
                properties.put(PerUtil.adjustMetadataName(tag), value);
            }
        }
    }

    @Override
    public Map<String, Map<String, Object>> getTags() {
        Map<String, Map<String, Object>> answer = new HashMap<>();
        Resource metadata = null;
        try {
            metadata = getOrCreateMetaData();
        } catch(PersistenceException e) {
            // Ignore
        }
        if(metadata != null) {
            for(Resource category: metadata.getChildren()) {
                String categoryName = category.getName();
                Map<String, Object> tags = getTags(category);
                answer.put(categoryName, tags);
            }
        }
        return answer;
    }

    @Override
    public Map<String, Object> getTags(String category) {
        Map<String, Object> answer = new HashMap<>();
        Resource categoryResource = null;
        try {
            categoryResource = getCategoryResource(category, false);
        } catch(PersistenceException e) {
            // Ignore
        }
        if(categoryResource != null) {
            answer = getTags(categoryResource);
        }
        return answer;
    }

    /**
     * Obtains all tags of a given category
     * @param category Category resource
     * @return Map of all tags except 'jcr:' of the given category. It is empty if none found or category is null.
     */
    private Map<String, Object> getTags(Resource category) {
        Map<String, Object> answer = new HashMap<>();
        if(category != null) {
            ValueMap properties = category.getValueMap();
            for(String key : properties.keySet()) {
                if(!key.startsWith(JCR)) {
                    answer.put(key, properties.get(key));
                }
            }
        }
        return answer;
    }

    @Override
    public Object getTag(String category, String tag) {
        Object answer = null;
        Resource categoryResource = null;
        try {
            categoryResource = getCategoryResource(category, false);
            if(categoryResource != null) {
                ValueMap properties = categoryResource.getValueMap();
                answer = properties.get(PerUtil.adjustMetadataName(tag));
            }
        } catch(PersistenceException e) {
            // Ignore
        }
        return answer;
    }

    /**
     * Obtains the Renditions from the Image
     * @param create If true the renditions will be created if not found
     * @return The Renditions resource if found or created otherwise null
     * @throws PersistenceException If resource could not be created
     */
    private @Nullable Resource getRenditionsResource(boolean create)
        throws PersistenceException
    {
        ResourceResolver resourceResolver = adaptTo(ResourceResolver.class);
        Resource renditions = getResource().getChild(RENDITIONS);
        if(create && renditions == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            renditions = resourceResolver.create(getResource(), RENDITIONS, properties);
        }
        return renditions;
    }

    /**
     * Obtains the given Category from the Image's Metadata
     * @param category Name of the category
     * @param create If true the category will be created if not found
     * @return The Category resource if found or created otherwise null
     * @throws PersistenceException If resource could not be created
     */
    private Resource getCategoryResource(String category, boolean create)
        throws PersistenceException
    {
        String adjustedCategory = PerUtil.adjustMetadataName(category);
        ResourceResolver resourceResolver = adaptTo(ResourceResolver.class);
        Resource metadata = getOrCreateMetaData();
        Resource answer = metadata.getChild(adjustedCategory);
        if(create && answer == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            answer = resourceResolver.create(metadata, adjustedCategory, properties);
        }
        return answer;
    }

    /** @return Obtains the metadata resource folder from the content and if not found then create it first **/
    private Resource getOrCreateMetaData() throws PersistenceException {
        Resource contentResource = getContentResource();
        Resource metadata = contentResource.getChild(METADATA);
        if(metadata == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            metadata = getResource().getResourceResolver().create(getContentResource(), METADATA, properties);

        }
        return metadata;
    }
}
