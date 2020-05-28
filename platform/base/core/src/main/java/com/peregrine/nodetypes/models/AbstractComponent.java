package com.peregrine.nodetypes.models;

/*-
 * #%L
 * peregrine default node types - Core
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

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.SLASH;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.nodetypes.merge.PageMerge;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import com.peregrine.nodetypes.merge.RenderContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Optional;

/**
 * Created by rr on 12/2/2016.
 */
public class AbstractComponent implements IComponent {

    private final Resource resource;

    @Inject @Optional
    private List<IComponent> experiences;

    public AbstractComponent(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public Resource getRootResource() {
        return java.util.Optional.ofNullable(PageMerge.getRenderContext())
                .map(RenderContext::getRequest)
                .map(SlingHttpServletRequest::getResource)
                .orElse(resource);
    }

    public String getPath() {
        String path = resource.getPath();
        int jcrContentIndex = path.indexOf(SLASH + JCR_CONTENT);
        if(jcrContentIndex >= 0) {
            return path.substring(jcrContentIndex);
        } else {
            return path;
        }
    }

    public String getComponent() {
        return PerUtil.getComponentNameFromResource(resource);
    }

    @Override
    @JsonIgnore
    public List<IComponent> getChildren() {
        return Collections.emptyList();
    }

    public List<IComponent> getExperiences() {
        return experiences;
    }

}
