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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by schaefa on 5/25/17.
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

    public Resource getResource() {
        return resource;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Resource getPropertyResource() {
        return propertyResource;
    }
}
