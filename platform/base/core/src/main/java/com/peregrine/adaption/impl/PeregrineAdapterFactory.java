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
import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import com.peregrine.replication.PerReplicable;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.replication.impl.PerReplicableImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static java.util.Objects.nonNull;
import static org.apache.sling.api.adapter.AdapterFactory.ADAPTABLE_CLASSES;
import static org.apache.sling.api.adapter.AdapterFactory.ADAPTER_CLASSES;

/**
 * This Adapter Factory allows to adapt a resource to a Wrapped Peregrine Object
 * or a Resource Resolver to a Page Manager
 *
 * Created by Andreas Schaefer on 6/4/17.
 */
@Component(
    service = AdapterFactory.class,
    property = {
        Constants.SERVICE_DESCRIPTION + EQUALS + "Peregrine: Adapter Factory",
        Constants.SERVICE_VENDOR + EQUALS + "headwire.com, Inc",
        // The Adapter are the target aka the class that an object can be adapted to (parameter in the adaptTo() method)
        ADAPTER_CLASSES + EQUALS + "com.peregrine.adaption.PerPage",
        ADAPTER_CLASSES + EQUALS + "com.peregrine.adaption.PerAsset",
        ADAPTER_CLASSES + EQUALS + "com.peregrine.adaption.PerPageManager",
        ADAPTER_CLASSES + EQUALS + "com.peregrine.replication.PerReplicable",
        // The Adaptable is the source that can be adapt meaning the object on which adaptTo() is called on
        ADAPTABLE_CLASSES + EQUALS + "org.apache.sling.api.resource.Resource",
        ADAPTABLE_CLASSES + EQUALS + "org.apache.sling.api.resource.ResourceResolver"
    }
)
public class PeregrineAdapterFactory
    implements AdapterFactory
{
    private static final Logger log = LoggerFactory.getLogger(PeregrineAdapterFactory.class);

    @Override
    public <AdapterType> AdapterType getAdapter(Object adaptable,
                                                Class<AdapterType> type) {
        if(adaptable instanceof Resource) {
            return getAdapter((Resource) adaptable, type);
        }

        if (adaptable instanceof ResourceResolver) {
            return getAdapter((ResourceResolver) adaptable, type);
        }

        log.warn("Unable to handle adaptable {}",
            adaptable.getClass().getName());
        return null;
    }

    /**
     * Adapts the given resource to the desired type if supported here (page or asset)
     * @param resource Resource to be adapted. If the desired type is a Peregrine Page
     *                 then it will go up the resource tree until it finds a JCR Content
     *                 node or the root. This way a page can be found by starting from any
     *                 of its components
     * @param type Desired type which can only be one of the Peregrine Wrapped Objects
     * @param <AdapterType> Desired type to be adapted to
     * @return The requested Peregrine Wrapped Object if the resource matches the type and if the requested type is supported
     */
    @SuppressWarnings("unchecked")
    private <AdapterType> AdapterType getAdapter(Resource resource,
                                                 Class<AdapterType> type) {
        log.trace("Get Adapter for Type: '{}' and Resource: '{}', ", type.getName(), resource);
        final String primaryType = PerUtil.getPrimaryType(resource);
        if(PerPage.class.equals(type)) {
            if(PAGE_PRIMARY_TYPE.equals(primaryType)) {
                return (AdapterType) new PerPageImpl(resource);
            }

            // Traverse up the tree. If we find a jcr:content of type per:PageContent and its parent is per:Page
            // then return that one instead
            PerPage answer = findPage(resource);
            if(nonNull(answer)) {
                return (AdapterType) answer;
            }

            log.trace("Given Resource: '{}' is not a Page", resource);
            return null;
        }

        if(PerAsset.class.equals(type)) {
            if(ASSET_PRIMARY_TYPE.equals(primaryType)) {
                return (AdapterType) new PerAssetImpl(resource);
            }

            log.trace("Given Resource: '{}' is not an Asset", resource);
            return (AdapterType) new PerAssetImpl(resource);
        }

        if(PerReplicable.class.equals(type)) {
            return (AdapterType) new PerReplicableImpl(resource);
        }

        log.warn("Unable to adapt unknown resource {} to type {}", resource, type.getName());
        return null;
    }

    /**
     * Adapts a Resource Resolver to the Peregrine Page Manager
     * @param resolver Resource resolver
     * @param type Desired type which must be the Peregrine Page Manager
     * @param <AdapterType> Desired typ
     * @return Page Manager if the resource resolver is not null and the type is the Peregrine Page Manager
     */
    @SuppressWarnings("unchecked")
    private <AdapterType> AdapterType getAdapter(ResourceResolver resolver,
                                                 Class<AdapterType> type) {
        if(PerPageManager.class.equals(type)) {
            return (AdapterType) new PerPageManagerImpl(resolver);
        }

        log.warn("Unable to adapt resolver to requested type {}", type.getName());
        return null;
    }

    /**
     * Tries to find a page of the given resource. This is either that resource
     * or one of its parent until we find a JCR Content Node.
     * This method does return a page from one of its child nodes (components).
     * The method will end as soon as we hit a JCR Content node or the root node
     * @param resource Starting resource
     * @return A Wrapped Page Object if found otherwise null
     */
    private PerPage findPage(Resource resource) {
        log.trace("path: {}", resource.getPath());
        final String primaryType = PerUtil.getPrimaryType(resource);
        final Resource parent = resource.getParent();
        log.trace("primaryType: {}", primaryType);
        if (!isJcrContent(resource)) {
            return Optional.ofNullable(parent)
                    .map(this::findPage)
                    .orElse(null);
        }

        if (isPrimaryType(resource, PAGE_CONTENT_TYPE) && isPrimaryType(parent, PAGE_PRIMARY_TYPE)) {
            return new PerPageImpl(parent);
        }

        // Found jcr:content but either wrong type or no parent -> done
        // JCR Content found not of the correct type -> done
        return null;
    }

}
