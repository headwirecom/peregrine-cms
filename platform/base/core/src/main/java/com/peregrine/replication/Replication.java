package com.peregrine.replication;

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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Replication Interface to replicate or deactivate
 * a resource
 *
 * Created by Andreas Schaefer on 5/25/17.
 */
public interface Replication {

    /** @return Name of the Service which is used to discover the Replication Service by the User **/
    String getName();

    /** @return Description of the Service which is given to the Users when they list the Replication Services **/
    String getDescription();

    default List<Resource> filterReferences(final List<Resource> resources) {
        return resources;
    }

    List<Resource> findReferences(Resource source, boolean deep)
            throws ReplicationException;

    /**
     * Replicates the given resource with its JCR Content and references
     * and if deep also with its children as well as referenced and missing
     * parent resources.
     *
     * @param source Resource to be replicated
     * @param deep If true the entire sub tree of the resource is replicated
     * @return List of replicated resources (the copy)
     *
     * @throws ReplicationException If the replication failed
     */
    default List<Resource> replicate(final Resource source, final boolean deep)
        throws ReplicationException {
        return replicate(findReferences(source, deep));
    }

    default void prepare(final Resource source, final boolean deep)
            throws ReplicationException {
        prepare(findReferences(source, deep));
    }

    /**
     * Removes the replicated resources (and with it all child resources)
     *
     * @param source Starting Resource to be removed from the Replication Target
     * @return List of removed resources (most likely just the given resource)
     *
     * @throws ReplicationException If there was an error preventing the deactivation
     */
    List<Resource> deactivate(Resource source)
        throws ReplicationException;

    default List<Resource> prepare(Collection<Resource> resourceList) throws ReplicationException {
        return Collections.emptyList();
    }

    /**
     * Replicates all the given resources and only them. This means
     * that missing parents as well as child resources must be provided
     * as well and they must be listed in order so that the parent resources
     * are created ahead of their children.
     *
     * @param resourceList List of resources to be replicated
     * @return List of replicated resources (the copy)
     *
     * @throws ReplicationException If the replication failed
     */
    List<Resource> replicate(Collection<Resource> resourceList)
        throws ReplicationException;

    default String storeFile(Resource parent, String name, String content)
            throws ReplicationException {
        return null;
    }

    default String storeFile(Resource parent, String name, byte[] content)
            throws ReplicationException {
        return null;
    }

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
