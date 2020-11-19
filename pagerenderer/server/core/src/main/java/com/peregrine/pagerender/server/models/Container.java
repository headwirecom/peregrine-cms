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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.peregrine.adaption.PerPage;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.CompositeValueMap;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import javax.annotation.PostConstruct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.pagerender.server.models.PageRenderServerConstants.PR_SERVER_COMPONENT_CONTAINER_TYPE;

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

    private ResourceResolver resolver;
    private String pagePath;
    private Resource page;
    private PerPage templatePage;
    private PerPage perPage;
    private String relativePath;
    private Resource templateContainer;

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

    public List<Resource> getCombinedResources(){
        if (Objects.isNull(resolver)){
            setup();
        }
        List<Resource> merged = new ArrayList<>();
        // get template container children, add them to the list
            if(Objects.nonNull(templateContainer)){
                templateContainer.getChildren().forEach(resource -> merged.add(resource));
            }
        // get page container children
        this.getResource().getChildren().forEach(resource -> {
            merged.add(resource);
        });
        return merged;
    }

    public ValueMap getCombinedProperties(){
        if (Objects.isNull(resolver)){
            setup();
        }
        ValueMap contentVM = this.getResource().getValueMap();
        if (Objects.nonNull(templateContainer)){
            ValueMap templateVM = templateContainer.getValueMap();
            return new CompositeValueMap(contentVM, templateVM, true);
        } else {
            return contentVM;
        }
    }


    void setup() {
        resolver = this.getResource().getResourceResolver();
        pagePath = this.getResource().getPath().substring(0, this.getResource().getPath().indexOf(JCR_CONTENT));
        page = resolver.getResource(pagePath);
        perPage = page.adaptTo(PerPage.class);
        templatePage = perPage.getTemplate();
        relativePath = this.getPath();
        // find the container under the template
        if (Objects.nonNull(templatePage)) {
            templateContainer = resolver.getResource(templatePage.getPath() + relativePath);
        }
    }
}
