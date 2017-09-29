package com.peregrine.admin.models;

/*-
 * #%L
 * example site - Core
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
import com.fasterxml.jackson.annotation.JsonValue;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * AS TODO: Move this to the IT tests (Taken from PodsModel
 * Created by rr on 4/18/2017.
 */
@Model(
    adaptables = Resource.class,
    resourceType = "example/components/page",
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    adapters = IComponent.class
)
@Exporter(
    name = "jackson",
    extensions = "json",
    selector = "export"
)
public class ExporterTestModel extends AbstractComponent {

//    private final Resource resource;
//
    public ExporterTestModel(Resource resource) {
        super(resource);
    }

    @Override
    @JsonIgnore
    public String getPath() {
        return super.getPath();
    }

    @Override
    @JsonIgnore
    public String getComponent() {
        return super.getComponent();
    }

    @Inject
    @Named(".")
    public List<IComponent> children;

    @JsonValue
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<IComponent> getChildren() {
//        ArrayList<IComponent> answer = new ArrayList<>();
//        addChildren(answer, children);
        return children;
    }

    private void addChildren(ArrayList<IComponent> answer, List<IComponent> children) {
        for (IComponent node: children) {
            answer.add(node);
            addChildren(answer, node.getChildren());
        }

    }
}
