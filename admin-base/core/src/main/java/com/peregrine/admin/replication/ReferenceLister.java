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
 * Defines the Reference Lister Service which provides a list
 * of resources that references the given resource.
 *
 * Created by Andreas Schaefer on 5/25/17.
 */
public interface ReferenceLister {

    /**
     * Provides a list of referenced that have a reference to the given resource
     *
     * @param resource Resource that is referenced by the result list
     * @return List of references which can be empty if none found
     */
    List<Reference> getReferencedByList(Resource resource);

    /**
     * Provides a list of resources referenced directly or indirectly by
     * the given resource
     *
     * @param resource Resource that starts the references
     * @param deep If true it will also look for references in the resource's children
     * @return List of resources referenced which might be empty
     */
    List<Resource> getReferenceList(Resource resource, boolean deep);

    /**
     * Provides a list of resources referenced directly or indirectly by
     * the given resource and also any parents that are not available on
     * the target side
     *
     * @param resource Resource that starts the references
     * @param deep If true it will also look for references in the resource's children
     * @param source The root reference
     * @param target The target reference
     * @return List of resources referenced including all parents of the resource that are not
     *         available on the target side
     */
    List<Resource> getReferenceList(Resource resource, boolean deep, Resource source, Resource target);
}
