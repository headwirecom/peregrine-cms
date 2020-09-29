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

import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED_BY;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATION_REF;

/**
 * Created by Andreas Schaefer on 6/2/17.
 */
public class PerPageManagerImpl
    implements PerPageManager
{
    public static final String RESOURCE_RESOLVER_MUST_BE_PROVIDED = "Resource Resolver must be provided";
    public static final String RESOURCE_MUST_BE_PROVIDED = "Resource must be provided";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Resource Resolver that backs that Page Manager **/
    private ResourceResolver resourceResolver;

    /** Creates a Page Manager from a Resource Resolver which cannot be null **/
    public PerPageManagerImpl(ResourceResolver resourceResolver) {
        if(resourceResolver == null) {
            throw new IllegalArgumentException(RESOURCE_RESOLVER_MUST_BE_PROVIDED);
        }
        this.resourceResolver = resourceResolver;
    }

    /** Creates a Page Manager from a Resource which cannot be null **/
    public PerPageManagerImpl(Resource resource) {
        if(resource == null) {
            throw new IllegalArgumentException(RESOURCE_MUST_BE_PROVIDED);
        }
        this.resourceResolver = resource.getResourceResolver();
    }

    @Override
    public PerPage getPage(String pagePath) {
        PerPage page = null;
        Resource resource = resourceResolver.getResource(pagePath);
        if(resource != null) {
            page = resource.adaptTo(PerPage.class);
        }
        return page;
    }

    @Override
    public void touch(PerPage page, boolean shallow, Calendar now, boolean clearReplication) {
        if(page != null && page.isValid() && page.hasContent()) {
            ModifiableValueMap properties = page.getModifiableProperties();
            if(now != null) {
                properties.put(JCR_LAST_MODIFIED, now);
            }
            if(clearReplication) {
                properties.remove(PER_REPLICATED);
                properties.remove(PER_REPLICATED_BY);
                properties.remove(PER_REPLICATION_REF);
            }
            try {
                resourceResolver.commit();
            } catch(PersistenceException e) {
                logger.error("could not clear replication properties", e);
            }
            if(!shallow) {
                for(PerPage child : page.listChildren()) {
                    touch(child, shallow, now, clearReplication);
                }
            }
        }
    }
}
