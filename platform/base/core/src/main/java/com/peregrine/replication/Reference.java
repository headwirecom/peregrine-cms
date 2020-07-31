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

import java.util.Date;

/**
 * This is a wrapper that contains the resource and property name
 * of a reference to a given resource but also contains the reference
 * to the parent of its JCR Content node if found
 *
 * Example with Content:
 * /a
 *   /b
 *     /myResource             <- Resource
 *       /jcr:content
 *         /myReference        <- Property Resource
 *           - myPropertyRef   <- Property (which name is provided here)
 *
 * Example w/o Content
 * /a
 *   /b
 *     /myResource             <- Resource and Property Resource
 *        - myPropertyRef      <- Property (which name is provided here)
 *
 * Created by Andreas Schaefer on 5/25/17.
 */
public class Reference {
    private Resource resource;
    private Resource propertyResource;
    private String propertyName;

    public Reference(Resource resource, String propertyName, Resource propertyResource) {
        this.resource = resource;
        this.propertyName = propertyName;
        this.propertyResource = propertyResource;
    }

    /**
     * This is either the same of the property resource or if found the parent of the JCR content
     * that is parent of the property resource
     **/
    public Resource getResource() {
        return resource;
    }

    /** Name of the Property that contains the reference **/
    public String getPropertyName() {
        return propertyName;
    }

    /** The resource that contains the property with the reference **/
    public Resource getPropertyResource() {
        return propertyResource;
    }
}
