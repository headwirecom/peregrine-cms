package com.peregrine.assets.impl;

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

import com.peregrine.assets.ResourceResolverFactoryProxy;
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

import java.util.Set;

@Component(service = JobConsumer.class, immediate = true, property = {
        JobConsumer.PROPERTY_TOPICS + "=" + AssetsToFSResourceChangeJobConsumer.TOPIC })
@Designate(ocd = AssetsToFSResourceChangeJobConsumerConfig.class)
public final class AssetsToFSResourceChangeJobConsumer implements JobConsumer {

    public static final String TOPIC = "com/peregrine/assets/REFRESH_FS_FILES";
    public static final String PN_PATHS = "paths";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactoryProxy resourceResolverFactory;

    private String rootPath;

    @Activate
    public void activate(final AssetsToFSResourceChangeJobConsumerConfig config) {
        this.rootPath = config.path();
    }

    @Override
    public JobResult process(final Job job) {
        logger.error(rootPath);
        final Set<String> initialPaths = job.getProperty(PN_PATHS, Set.class);
        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver()) {
            for (final String path : initialPaths) {
                logger.error(path);
                final Resource resource = resourceResolver.getResource(path);
                logger.error(String.valueOf(resource));
            }
        } catch (final LoginException e) {
            return JobResult.CANCEL;
        }

        return JobResult.OK;
    }

}