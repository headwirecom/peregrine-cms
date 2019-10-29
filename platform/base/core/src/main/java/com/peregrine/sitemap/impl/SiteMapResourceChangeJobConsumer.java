package com.peregrine.sitemap.impl;

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

import com.peregrine.sitemap.ResourceResolverFactoryProxy;
import com.peregrine.sitemap.SiteMapCache;
import com.peregrine.sitemap.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashSet;
import java.util.Set;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;

@Component(service = JobConsumer.class, immediate = true, property = {
        JobConsumer.PROPERTY_TOPICS + "=" + SiteMapResourceChangeJobConsumer.TOPIC })
public final class SiteMapResourceChangeJobConsumer implements JobConsumer {

    public static final String TOPIC = "com/peregrine/sitemap/REFRESH_CACHE";
    public static final String PN_PATHS = "paths";
    public static final String PN_PRIMARY_TYPES = "primaryTypes";

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Reference
    private SiteMapCache cache;

    @Override
    public JobResult process(final Job job) {
        final Set<String> affectedPaths = new HashSet<>();
        final Set<String> initialPaths = job.getProperty(PN_PATHS, Set.class);
        final Set<String> allowedPrimaryTypes = job.getProperty(PN_PRIMARY_TYPES, Set.class);
        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver()) {
            for (final String path : initialPaths) {
                final Resource resource = Utils.getFirstExistingAncestorOnPath(resourceResolver, path);
                if (resource != null && allowedPrimaryTypes.contains(resource.getValueMap().get(JCR_PRIMARY_TYPE))) {
                    affectedPaths.add(resource.getPath());
                }
            }
        } catch (final LoginException e) {
            return JobResult.CANCEL;
        }

        for (final String path : affectedPaths) {
            cache.rebuild(path);
        }

        return JobResult.OK;
    }

}