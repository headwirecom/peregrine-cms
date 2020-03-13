package com.peregrine.admin.models;

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

import com.peregrine.nodetypes.models.Container;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rr on 12/2/2016.
 */
@Model(adaptables = Resource.class, resourceType = {
        "experiences/components/toolingpage"
}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, adapters = IComponent.class)
@Exporter(name = "jackson", extensions = "json")
public class PageModel extends Container {

    public PageModel(Resource r) {
        super(r);
    }

    public Resource getParentContent(Resource res) {
        Resource page = res.getParent();
        if(page != null) {
            Resource parentPage = page.getParent();
            if(parentPage != null) {
                if("per:Page".equals(parentPage.getResourceType())) {
                    Resource child =  parentPage.getChild("jcr:content");
                    return child;
                }
            }
        }
        return null;
    }

    @Inject
    private String[] siteCSS;

    @Inject
    private String[] siteJS;

    @Inject @Named("jcr:title")
    private String title;

    @Inject @Named("jcr:description")
    private String description;

    @Inject
    private String dataFrom;

    @Inject
    private String dataDefault;

    @Inject
    private String[] loaders;

    @Inject
    private String[] suffixToParameter;

    public String[] getSiteCSS() {
        if(siteCSS == null) {
            String[] value = getInheritedProperty("siteCSS");
            if (value != null) return value;
        }
        return siteCSS;
    }

    private String[] getInheritedProperty(String propertyName) {
        Resource parentContent = getParentContent(getResource());
        while(parentContent != null) {
            ValueMap props = ResourceUtil.getValueMap(parentContent);
            Object value = props.get(propertyName);
            if(value != null) {
                return (String[]) value;
            }
            parentContent = getParentContent(parentContent);
        }
        return new String[]{};
    }

    public String[] getSiteJS() {
        if(siteJS == null) {
            String[] value = getInheritedProperty("siteJS");
            if (value != null) return value;
        }
        return siteJS;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDataFrom() {
        return dataFrom;
    }
    public String getDataDefault() {
        return dataDefault;
    }
    public String[] getLoaders() {
        return loaders;
    }

    public String[] getSuffixToParameter() {
        return suffixToParameter;
    }

    public List<TitlePath> getBreadcrumbs() {
        LinkedList<TitlePath> ret = new LinkedList<TitlePath>();
        Resource res = getResource();
        while(res != null) {

            ret.addFirst(new TitlePath(res));
            res = getParentContent(res);
            // we do not want to stop at level 2 and not include it
            if(res != null && res.getParent().getPath().equals("/content/admin/pages")) {
                break;
            }
        }
        return ret;
    }

    class TitlePath {
        Resource res;
        public TitlePath(Resource res) {
            this.res = res;
        }

        public String getTitle() {
            return res.getValueMap().get("jcr:title", String.class);
        }

        public String getPath() {
            return res.getParent().getPath();
        }
    }
}
