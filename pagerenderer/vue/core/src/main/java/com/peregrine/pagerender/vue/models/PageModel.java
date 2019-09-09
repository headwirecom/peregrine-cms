package com.peregrine.pagerender.vue.models;

/*-
 * #%L
 * peregrine vuejs page renderer - Core
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

import com.peregrine.commons.util.PerConstants;
import com.peregrine.nodetypes.models.IComponent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.factory.ModelFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.DOMAINS;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.pagerender.vue.models.PageRenderVueConstants.PR_VUE_COMPONENT_PAGE_TYPE;

/**
 * Created by rr on 12/2/2016.
 */
@Model(adaptables = Resource.class,
       resourceType = {PR_VUE_COMPONENT_PAGE_TYPE},
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
       adapters = IComponent.class)
@Exporter(name = JACKSON,
          extensions = JSON)
public class PageModel extends Container {

    public static final String SITE_CSS = "siteCSS";
    public static final String PREFETCH_DNS = "prefetchDNS";
    public static final String SITE_JS = "siteJS";

    @Inject
    private ModelFactory modelFactory;

    @Inject
    @Optional
    private String[] prefetchDNS;

    @Inject
    @Optional
    private String[] siteCSS;

    @Inject
    @Optional
    private String[] siteJS;

    @Inject
    @Optional
    private String[] domains;

    @Inject
    @Named(TEMPLATE)
    @Optional
    private String template;

    @Inject
    @Named(JCR_TITLE)
    @Optional
    private String title;

    @Inject
    private String dataFrom;

    @Inject
    private String dataDefault;

    @Inject
    private String[] loaders;

    @Inject
    private String[] suffixToParameter;

    @Inject
    private String description;

    public PageModel(final Resource resource) {
        super(resource);
    }

    public Resource getParentContent(final Resource resource) {
        return java.util.Optional.ofNullable(resource)
                .map(Resource::getParent)
                .map(Resource::getParent)
                .filter(r -> PAGE_PRIMARY_TYPE.equals(r.getResourceType()))
                .map(r -> r.getChild(JCR_CONTENT))
                .orElse(null);
    }

    public String getSiteRoot() {
        String path = getPagePath();
        String[] segments = path.split(SLASH);
        return String.join(SLASH, segments[0], segments[1], segments[2], segments[3]);
    }

    public String getPagePath() {
        return java.util.Optional.ofNullable(getResource())
                .map(Resource::getParent)
                .map(Resource::getPath)
                .orElse(null);
    }

    public String[] getPrefetchDNS() {
        if(prefetchDNS == null) {
            String[] value = (String[]) getInheritedProperty(PREFETCH_DNS);
            if(value != null && value.length != 0) return value;
            if(getTemplate() != null) {
                PageModel templatePageModel = getTemplatePageModel();
                if(templatePageModel != null) {
                    return templatePageModel.getPrefetchDNS();
                }
            }
        }
        return prefetchDNS;
    }

    public String[] getSiteCSS() {
        if(siteCSS == null) {
            String[] value = (String[]) getInheritedProperty(SITE_CSS);
            if(value != null && value.length != 0) return value;
            if(getTemplate() != null) {
                PageModel templatePageModel = getTemplatePageModel();
                if(templatePageModel != null) {
                    return templatePageModel.getSiteCSS();
                }
            }
        }
        return siteCSS;
    }

    public String[] getDomains() {
        if(domains == null) {
            String[] value = (String[]) getInheritedProperty(DOMAINS);
            if(value != null && value.length != 0) return value;
            if(getTemplate() != null) {
                PageModel templatePageModel = getTemplatePageModel();
                if(templatePageModel != null) {
                    return templatePageModel.getDomains();
                }
            }
        }
        return domains;
    }

    private PageModel getTemplatePageModel() {
        final String path = getTemplate();
        if (path == null) {
            return null;
        }

        return java.util.Optional.ofNullable(getResource())
                .map(Resource::getResourceResolver)
                .map(rr -> rr.getResource(path + SLASH + JCR_CONTENT))
                .map(r -> (PageModel) modelFactory.getModelFromResource(r))
                .orElse(null);
    }

    private Object getInheritedProperty(String propertyName) {
        Resource parentContent = getParentContent(getResource());
        while(parentContent != null) {
            ValueMap props = ResourceUtil.getValueMap(parentContent);
            Object value = props.get(propertyName);
            if(value != null) {
                return value;
            }
            parentContent = getParentContent(parentContent);
        }
        return null;
    }

    public String[] getSiteJS() {
        if(siteJS == null) {
            String[] value = (String[]) getInheritedProperty(SITE_JS);
            if(value != null && value.length != 0) return value;
            PageModel templatePageModel = getTemplatePageModel();
            if(templatePageModel != null) {
                return templatePageModel.getSiteJS();
            }
        }
        return siteJS;
    }

    public String getTemplate() {
        if(template == null) {
            return (String) getInheritedProperty(TEMPLATE);
        }

        return template;
    }

    public String getTitle() {
        return title;
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

    public List<Tag> getTags() {
        Resource tags = getResource().getChild("tags");
        List<Tag> answer = new ArrayList<>();
        if(tags != null) {
            for(Resource tag: tags.getChildren()) {
                answer.add(new Tag(tag));
            }
        }
        return answer;
    }

    public List<String> getRenderedTags() {
        Resource tags = getResource().getChild("tags");
        List<String> answer = new ArrayList<>();
        if(tags != null) {
            for(Resource tag: tags.getChildren()) {
                answer.add(new Tag(tag).getName());
            }
        }
        return answer;
    }

    public List<MetaProperty> getMetaproperties() {
        Resource metaproperties = getResource().getChild(PerConstants.METAPROPERTIES);
        List<MetaProperty> answer = new ArrayList<>();
        if(metaproperties != null) {
            for(Resource metaproperty : metaproperties.getChildren()) {
                MetaProperty metaProperty = new MetaProperty(metaproperty);
                if(metaProperty.isProperty()) {
                    answer.add(metaProperty);
                }
            }
        }
        return answer;
    }

    public List<MetaProperty> getMetanames() {
        Resource metaproperties = getResource().getChild(PerConstants.METAPROPERTIES);
        List<MetaProperty> answer = new ArrayList<>();
        if(metaproperties != null) {
            for(Resource metaproperty : metaproperties.getChildren()) {
                MetaProperty metaProperty = new MetaProperty(metaproperty);
                if(metaProperty.isName()) {
                    answer.add(metaProperty);
                }
            }
        }
        return answer;
    }

    public String getDescription() {
        return description;
    }

    class Tag {
        private String path;
        private String name;
        private String value;

        public Tag(Resource r) {
            this.path = r.getPath();
            this.path = path.substring(path.indexOf("/jcr:content"));
            this.name = r.getName();
            this.value = r.getValueMap().get("value", String.class);
        }

        public String getName() { return name; }
        public String getValue() { return value; }
        public String getPath() { return path; }
        @Override
        public String toString() { return name; }
    }

    public class MetaProperty {
        private String path;
        private String metaType;
        private String key;
        private String value;

        public MetaProperty(Resource r) {
            this.path = r.getPath();
            this.path = path.substring(path.indexOf("/jcr:content"));
            final ValueMap valueMap = r.getValueMap();
            this.metaType = valueMap.get("metatype", String.class);
            this.key = valueMap.get("key", String.class);
            this.value = valueMap.get("value", String.class);
        }

        public String getPath() { return path; }
        public String getMetaType() { return metaType; }
        public String getKey() { return key; }
        public String getValue() { return value; }
        public boolean isProperty() { return "property".equalsIgnoreCase(metaType); }
        public boolean isName() { return "name".equalsIgnoreCase(metaType); }
    }
}
