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
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;

/**
 * Created by schaefa on 6/4/17.
 */
@Component(
    service = AdapterFactory.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Peregrine: Adapter Factory",
        Constants.SERVICE_VENDOR + "=headwire.com, Inc",
        // The Adapter are the target aka the class that an object can be adapted to (parameter in the adaptTo() method)
        AdapterFactory.ADAPTER_CLASSES + "=com.peregrine.data.PerPage",
        AdapterFactory.ADAPTER_CLASSES + "=com.peregrine.data.PerAsset",
        AdapterFactory.ADAPTER_CLASSES + "=com.peregrine.data.PerPageManager",
        // The Adaptable is the source that can be adapt meaning the object on which adaptTo() is called on
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.ResourceResolver"
    }
)
public class PeregrineAdapterFactory
    implements AdapterFactory
{
    private static final Logger log = LoggerFactory.getLogger(PeregrineAdapterFactory.class);

    public PeregrineAdapterFactory() {
    }

    @Override
    public <AdapterType> AdapterType getAdapter(Object adaptable,
                                                Class<AdapterType> type) {
        if(adaptable instanceof Resource) {
            return getAdapter((Resource) adaptable, type);
        } else if (adaptable instanceof ResourceResolver) {
            return getAdapter((ResourceResolver) adaptable, type);
        } else {
            log.warn("Unable to handle adaptable {}",
                adaptable.getClass().getName());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <AdapterType> AdapterType getAdapter(Resource resource,
                                                 Class<AdapterType> type) {
        if(type.equals(PerPage.class)) {
            String primaryType = resource.getResourceType();
            if(PAGE_PRIMARY_TYPE.equals(primaryType)) {
                return (AdapterType) new PerPageImpl(resource);
            } else {
                log.trace("Given Resource: '{}' is not a Page", resource);
            }
        } else if(type == PerAsset.class) {
            String primaryType = resource.getResourceType();
            if(ASSET_PRIMARY_TYPE.equals(primaryType)) {
                return (AdapterType) new PerAssetImpl(resource);
            } else {
                log.trace("Given Resource: '{}' is not an Asset", resource);
            }
            return (AdapterType) new PerAssetImpl(resource);
        } else {
            log.debug("Unable to adapt unknown resource {} to type {}", resource, type.getName());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <AdapterType> AdapterType getAdapter(ResourceResolver resolver,
                                                 Class<AdapterType> type) {
        if(type.equals(PerPageManager.class)) {
            return (AdapterType) new PerPageManagerImpl(resolver);
        } else {
            log.warn("Unable to adapt resolver to requested type {}",
                type.getName());
            return null;
        }
    }
}
