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

import com.peregrine.commons.ResourceUtils;
import com.peregrine.sitemap.ResourceResolverFactoryProxy;
import com.peregrine.sitemap.SiteMapStructureCache;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;

@Component(service = JobConsumer.class, immediate = true, property = {
        JobConsumer.PROPERTY_TOPICS + EQUALS + SiteMapResourceChangeJobConsumer.TOPIC })
@Designate(ocd = SiteMapResourceChangeJobConsumerConfig.class)
public final class SiteMapResourceChangeJobConsumer implements JobConsumer {

    public static final String TOPIC = "com/peregrine/sitemap/REFRESH_CACHE";
    public static final String PN_PATHS = "paths";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<String> primaryTypes = new HashSet<>();

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    @Reference
    private SiteMapStructureCache cache;

    @Activate
    public void activate(final SiteMapResourceChangeJobConsumerConfig config) {
        if(config == null || config.primaryTypes() == null) {
            logger.debug("Configuration for Site Map Resource Change Job Consumer config is not valid -> ignored");
        } else {
            primaryTypes.clear();
            for (final String type : config.primaryTypes()) {
                primaryTypes.add(type);
            }
        }
    }

    @Override
    public JobResult process(final Job job) {
        @SuppressWarnings("unchecked")
		final Set<String> initialPaths = job.getProperty(PN_PATHS, Set.class);
        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver()) {
            for (final String path : initialPaths) {
                final Resource resource = ResourceUtils.getFirstExistingAncestorOnPath(resourceResolver, path);
                if (isAllowed(resource)) {
                    cache.rebuild(resource.getPath());
                }
            }
        } catch (final LoginException e) {
            logger.error("Unable to process job.", e);
            return JobResult.CANCEL;
        }

        return JobResult.OK;
    }

    private boolean isAllowed(final Resource resource) {
        return Optional.ofNullable(resource)
                .map(Resource::getValueMap)
                .map(map -> map.get(JCR_PRIMARY_TYPE))
                .map(type -> primaryTypes.contains(type))
                .orElse(false);
    }

}
