package com.peregrine.replication.impl;

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

import com.peregrine.replication.DefaultReplicationMapper;
import com.peregrine.replication.Replication;
import com.peregrine.replication.ReplicationsContainer;
import com.peregrine.replication.ReplicationsContainerWithDefault;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.*;

import java.util.*;

import static com.peregrine.commons.util.PerUtil.*;
import static java.util.Objects.nonNull;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = ReplicationsContainerWithDefault.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Replications Container",
        SERVICE_VENDOR + EQUALS + PER_VENDOR
    }
)
public final class ReplicationsContainerWithDefaultImpl implements ReplicationsContainerWithDefault {

    @Reference
    private ReplicationsContainer replications;

    @Reference
    private DefaultReplicationMapper defaultReplicationMapper;

    @Override
    public Replication get(final String name) {
        if (StringUtils.equals(name, defaultReplicationMapper.getName())) {
            return defaultReplicationMapper;
        }

        return replications.get(name);
    }

    @Override
    public Replication getDefault() {
        return Optional.ofNullable(get("defaultRepl"))
                .orElse(defaultReplicationMapper);
    }

    @Override
    public Replication getOrDefault(final String name) {
        return Optional.ofNullable(name)
                .map(this::get)
                .orElseGet(this::getDefault);
    }

    @Override
    public Collection<Replication> getAll() {
        final Collection<Replication> all = replications.getAll();
        if (nonNull(replications.get(defaultReplicationMapper.getName()))) {
            return all;
        }

        final List<Replication> result = new LinkedList<>(all);
        result.add(defaultReplicationMapper);
        return result;
    }

}