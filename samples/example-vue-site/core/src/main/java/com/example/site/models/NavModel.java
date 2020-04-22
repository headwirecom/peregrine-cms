package com.example.site.models;

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

import com.peregrine.nodetypes.merge.PageMerge;
import com.peregrine.nodetypes.merge.RenderContext;
import com.peregrine.nodetypes.models.AbstractComponent;
import com.peregrine.nodetypes.models.IComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/**
 * Created by rr on 4/18/2017.
 */
@Model(adaptables = Resource.class, resourceType = "example/components/nav", defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class NavModel extends AbstractComponent {

    public NavModel(Resource resource) {
        super(resource);
    }

    public String getBrand() {
        RenderContext rx = PageMerge.getRenderContext();
        SlingHttpServletRequest request = rx.getRequest();
        Resource homePage = getResourceAt(request.getResource(), 3);
        Resource content = homePage.getChild("jcr:content");
        return content.adaptTo(ValueMap.class).get("brand", String.class);
    }

    public List<NavItem> getNavigation() {
        List<NavItem> ret = new ArrayList<>();

        RenderContext rx = PageMerge.getRenderContext();
        SlingHttpServletRequest request = rx.getRequest();
        Resource homePage = getResourceAt(request.getResource(), 3);

        Iterator<Resource> children = homePage.listChildren();

        for (Iterator<Resource> it = children; it.hasNext(); ) {
            Resource child = it.next();
            if("per:Page".equals(child.getResourceType())) {

                Resource content = child.getChild("jcr:content");
                ValueMap map = content.adaptTo(ValueMap.class);
                ret.add(new NavItem(child.getPath(), ""+map.get("jcr:title")));
            }
        }
        return ret;
    }

    private Resource getResourceAt(Resource res, int level) {
        LinkedList<Resource> parents = new LinkedList<Resource>();
        parents.addFirst(res);
        Resource resource = res.getParent();
        while(resource != null) {
            parents.addFirst(resource);
            resource = resource.getParent();
        }
        if(parents.size() >= 4) {
            return parents.get(3);
        } else {
            return res;
        }
    }

    static class NavItem {

        private String path;
        private String title;

        public NavItem(String path, String title) {
            this.path = path;
            this.title = title;
        }

        public String getPath() {
            return path;
        }

        public String getTitle() {
            return title;
        }
    }
}
