package com.peregrine.admin.replication;

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

import org.apache.sling.api.resource.Resource;

import java.util.List;

/**
 * Created by schaefa on 5/25/17.
 */
public interface Replication {

    String getName();

    /**
     * Replicates the given resource with its JCR Content and references
     * and if deep also with its children
     *
     * @param source Resource to be replicated
     * @param deep If true the entire sub tree of the resource is replicated
     * @return List of replicated resources (the copy)
     * @throws ReplicationException If the replication failed
     */
    List<Resource> replicate(Resource source, boolean deep)
        throws ReplicationException;

    List<Resource> deactivate(Resource source)
        throws ReplicationException;

    /**
     * Replicates all the given resources and only them. This means JCR Content
     * nodes must be part of the given list
     *
     * @param resourceList List of resources to be replicated
     * @return List of replicated resources (the copy)
     * @throws ReplicationException If the replication failed
     */
    List<Resource> replicate(List<Resource> resourceList)
        throws ReplicationException;

//    List<Resource> deactivate(List<Resource> resourceList)
//        throws ReplicationException;

    class ReplicationException
        extends Exception
    {
        public ReplicationException(String message) {
            super(message);
        }

        public ReplicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
