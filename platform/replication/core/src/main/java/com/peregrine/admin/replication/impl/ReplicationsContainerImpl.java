package com.peregrine.admin.replication.impl;

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

import com.peregrine.admin.replication.DefaultReplicationMapper;
import com.peregrine.replication.Replication;
import com.peregrine.replication.ReplicationsContainer;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = ReplicationsContainer.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Replications Container",
        SERVICE_VENDOR + EQUALS + PER_VENDOR
    }
)
@SuppressWarnings("serial")
public final class ReplicationsContainerImpl implements ReplicationsContainer {

    public static final String DEFAULT_REPL = "defaultRepl";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Replication> replications = new HashMap<>();

    @Reference
    private DefaultReplicationMapper defaultReplicationMapper;

    @Reference(
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    public void bindReplication(final Replication replication) {
        final String name = replication.getName();
        logger.trace("Register replication with name: '{}': {}", name, replication);
        if (isNotBlank(name)) {
            replications.put(name, replication);
        } else {
            logger.error("Replication: '{}' does not provide an operation name -> binding is ignored", replication);
        }
    }

    @SuppressWarnings("unused")
    public void unbindReplication(final Replication replication) {
        final String name = replication.getName();
        if (replications.containsKey(name)) {
            replications.remove(name);
        } else {
            logger.error("Replication: '{}' is not register with operation name: '{}' -> unbinding is ignored", replication, name);
        }
    }

    @Reference(
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    public void bindDefaultReplicationMapper(final DefaultReplicationMapper mapper) {
        logger.trace("Register Default Replication Mapper: '{}'", mapper.getName());
        bindReplication(mapper);
    }

    @SuppressWarnings("unused")
    public void unbindDefaultReplicationMapper(final DefaultReplicationMapper mapper) {
        logger.trace("UnRegister Default Replication Mapper: '{}'", mapper.getName());
        unbindReplication(mapper);
    }

    @Override
    public Replication get(final String name) {
        return replications.get(name);
    }

    @Override
    public Replication getDefault() {
        return Optional.of(DEFAULT_REPL)
                .map(this::get)
                .orElse(defaultReplicationMapper);
    }

    @Override
    public Replication getOrDefault(final String name) {
        return Optional.ofNullable(name)
                .map(this::get)
                .orElseGet(this::getDefault);
    }

}