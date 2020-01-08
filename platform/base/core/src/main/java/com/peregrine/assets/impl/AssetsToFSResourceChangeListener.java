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

import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.event.jobs.JobManager;

import java.util.*;

public final class AssetsToFSResourceChangeListener implements ResourceChangeListener {

    private final JobManager jobManager;

    public AssetsToFSResourceChangeListener(final JobManager jobManager) {
        this.jobManager = jobManager;
    }

    @Override
    public void onChange(final List<ResourceChange> changes) {
        final Set<String> paths = new HashSet<>();
        for (final ResourceChange change: changes) {
            paths.add(change.getPath());
        }

        final Map<String, Object> props = new HashMap<>();
        props.put(AssetsToFSResourceChangeJobConsumer.PN_PATHS, paths);
        jobManager.addJob(AssetsToFSResourceChangeJobConsumer.TOPIC, props);
    }

}