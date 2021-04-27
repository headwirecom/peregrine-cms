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

import com.peregrine.adaption.PerBase;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED_BY;
import static com.peregrine.commons.util.PerConstants.SITE_PRIMARY_TYPE;
import static java.util.Objects.nonNull;

/**
 * Common Base Class for Peregrine Object Wrappers
 *
 * Created by Andreas Schaefer on 6/4/17.
 */
public abstract class PerBaseImpl implements PerBase {

    public static final String RESOURCE_MUST_BE_PROVIDED = "Resource must be provided";
    Logger logger = LoggerFactory.getLogger(getClass());

    /** The Resource this instance wraps **/
    private final Resource resource;
    /** Reference to its JCR Content resource if the resource have on otherwise it is null **/
    private final Resource jcrContent;
    private final ValueMap properties;
    protected final Optional<ValueMap> optionalProperties;
    private final ModifiableValueMap modifiableProperties;

    public PerBaseImpl(final Resource resource) {
        Objects.requireNonNull(resource, "Resource must be provided");
        this.resource = resource;
        jcrContent = resource.getChild(JCR_CONTENT);
        if (hasContent()) {
            properties = jcrContent.getValueMap();
            modifiableProperties = jcrContent.adaptTo(ModifiableValueMap.class);
        } else {
            properties = null;
            modifiableProperties = null;
        }

        optionalProperties = Optional.ofNullable(properties);
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public String getPath() {
        return resource.getPath();
    }

    @Override
    public String getName() {
        return resource.getName();
    }

    @Override
    public Calendar getLastModified() {
        return getContentProperty(JCR_LAST_MODIFIED, Calendar.class);
    }

    @Override
    public String getLastModifiedBy() {
        return getContentProperty(JCR_LAST_MODIFIED_BY, String.class);
    }

    @Override
    public boolean hasContent() {
        return nonNull(jcrContent);
    }

    @Override
    public boolean isValid() {
        return hasContent();
    }

    @Override
    public Resource getContentResource() {
        return jcrContent;
    }

    @Override
    public ValueMap getProperties() {
        return properties;
    }

    @Override
    public ModifiableValueMap getModifiableProperties() {
        return modifiableProperties;
    }

    @Override
    public <T> T getContentProperty(String propertyName, Class<T> type) {
        return optionalProperties
                .map(properties -> properties.get(propertyName, type))
                .orElse(null);
    }

    @Override
    public <T> T getContentProperty(String propertyName, T defaultValue) {
        return Optional.ofNullable(defaultValue)
                .map(Object::getClass)
                .map(t -> (Class<T>)t)
                .map(t -> getContentProperty(propertyName,  t))
                .orElse(defaultValue);
    }

    @Override
    public ValueMap getSiteProperties(){
        if(Objects.nonNull(getSiteResource()) && Objects.nonNull(getSiteResource().getChild(JCR_CONTENT))){
            return getSiteResource().getChild(JCR_CONTENT).getValueMap();
        }
        return null;
    }

    @Override
    public Resource getSiteResource(){
        return this.getSiteResource(this.resource);
    }

    private Resource getSiteResource(Resource resource){
        if (Objects.nonNull(this.resource)){
            try {
                NodeType nt = Objects.requireNonNull(resource.adaptTo(Node.class)).getPrimaryNodeType();
                if( nt.getName().equals(SITE_PRIMARY_TYPE)){
                    return resource;
                } else {
                    return Objects.nonNull(resource.getParent()) ? getSiteResource(resource.getParent()) : null;
                }
            } catch (RepositoryException e) {
                logger.error("Error getting root per:Site resource ", e);
            }
        }
        return null;
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if(Resource.class.equals(type)) {
            return (AdapterType) resource;
        }

        final ResourceResolver resourceResolver = resource.getResourceResolver();
        if(ResourceResolver.class.equals(type)) {
            return (AdapterType) resourceResolver;
        }

        if(Session.class.equals(type)) {
            return (AdapterType) resourceResolver.adaptTo(Session.class);
        }

        return null;
    }
}
