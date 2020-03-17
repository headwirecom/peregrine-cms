package com.peregrine.pagerender.server.models;

/*-
 * #%L
 * peregrine server page renderer - Core
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

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.pagerender.server.models.PageRenderServerConstants.PR_SERVER_COMPONENT_CONTAINER_TYPE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/**
 * Created by rr on 12/2/2016.
 */
@Model(
    adaptables = Resource.class,
    resourceType = {PR_SERVER_COMPONENT_CONTAINER_TYPE},
    adapters = IComponent.class)
@Exporter(
    name = JACKSON,
    extensions = JSON)
public class Container extends AbstractComponent {

    @Inject
    @Named(".")
    private List<IComponent> children;

    public Container(Resource r) {
        super(r);
    }

    @Override
    @JsonIgnore(value = false)
    public List<IComponent> getChildren() {
        return children;
    }
}
