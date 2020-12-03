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

import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.pagerender.vue.models.PageRenderVueConstants.PR_VUE_COMPONENT_PAGE_TYPE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.nodetypes.models.IComponent;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.factory.ModelFactory;

/**
 * Created by rr on 12/2/2016.
 */
@Model(
    adaptables = Resource.class,
    resourceType = {PR_VUE_COMPONENT_PAGE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    adapters = IComponent.class)
@Exporter(
    name = JACKSON,
    extensions = JSON)
public class PageModel extends Container {

    public static final String SITE_CSS = "siteCSS";
    public static final String PREFETCH_DNS = "prefetchDNS";
    public static final String DOMAINS = "domains";
    public static final String SITE_JS = "siteJS";
    public static final String TEMPLATE = "template";

    public PageModel(Resource r) {
        super(r);
    }

    public Resource getParentContent(Resource res) {
        Resource page = res.getParent();
        if(page != null) {
            Resource parentPage = page.getParent();
            if(parentPage != null) {
                if(PAGE_PRIMARY_TYPE.equals(parentPage.getResourceType())) {
                    Resource child = parentPage.getChild(JCR_CONTENT);
                    return child;
                }
            }
        }
        return null;
    }

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

    @Inject
    @Optional
    private String brand;

    @Inject
    @Optional
    private String ogTitle;

    @Inject
    @Optional
    private String ogDescription;

    @Inject
    @Optional
    private String ogImage;

    private String absOgImage;

    @Inject
    @Optional
    private Boolean noIndex = false;

    @Inject
    @Optional
    private Boolean noFollow = false;


    public String getSiteRoot() {
        String path = getPagePath();
        String[] segments = path.split(SLASH);
        return String.join(SLASH, segments[0], segments[1], segments[2], segments[3]);
    }

    public String getPagePath() {
        return getResource().getParent().getPath();
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

    /**
     * Retrieves the primary public-facing domain for the site if it exists. The primary domain is considered the
     * first domain listed.
     *
     * @return The primary domain on success, and an empty string otherwise.
     */
    public String getPrimaryDomain() {

        return getDomains() != null && getDomains().length > 0
                ? getDomains()[0]
                : "";
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

    /**
     * Attempts to determine a fallback domain based on the request. A fallback domain is formatted as:
     * [scheme]://[domain]:[port]. If the remote port is 80, then the port is not included.
     *
     * @return A prefix URL on success, and an empty string otherwise.
     */
    public String getFallbackDomain() {

        // TODO: get SlingHttpServletRequest
        /*
        String domain = "";

        if (request != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(request.getScheme()).append("://").append(request.getServerName());

            if (request.getServerPort() != 80)
            {
                sb.append(":").append(request.getServerPort());
            }
            domain = sb.toString();
        } else {
            System.out.println("request is null");
        }

        return domain;
         */
        return "http://localhost:8080";
    }

    private PageModel getTemplatePageModel() {
        String template = getTemplate();
        if(template == null) return null;
        Resource templateResource = getResource().getResourceResolver().getResource(getTemplate() + SLASH + JCR_CONTENT);
        return (PageModel) modelFactory.getModelFromResource(templateResource);
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
            String value = (String) getInheritedProperty(TEMPLATE);
            if(value != null) {
                this.template = value;
                return value;
            }
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
        List<Tag> answer = new ArrayList<Tag>();
        if(tags != null) {
            for(Resource tag: tags.getChildren()) {
                String tagString = tag.getValueMap().get("value", String.class);
                Resource tagResource = tag.getResourceResolver().getResource(tagString);
                if (tagResource != null) { 
                    answer.add(new Tag(tag));
                }
            }
        }
        return answer;
    }

    public List<String> getRenderedTags() {
        Resource tags = getResource().getChild("tags");
        List<String> answer = new ArrayList<String>();
        if(tags != null) {
            for(Resource tag: tags.getChildren()) {
                String tagString = tag.getValueMap().get("value", String.class);
                Resource tagResource = tag.getResourceResolver().getResource(tagString);
                if (tagResource != null) { 
                    answer.add(new Tag(tag).getName());
                }
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
                if( metaProperty.isProperty()) answer.add(metaProperty);
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
                if( metaProperty.isName()) answer.add(metaProperty);
            }
        }
        return answer;
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        if(brand == null) {
            String value = (String) getInheritedProperty("brand");
            if(value != null && value.trim().length() > 0) return value;
            PageModel templatePageModel = getTemplatePageModel();
            if(templatePageModel != null) {
                return templatePageModel.getBrand();
            }
        }
        return brand;
    }

    public String getOgTitle() {
        return StringUtils.isBlank(ogTitle) ? title : ogTitle;
    }

    public String getOgDescription() {
        return StringUtils.isBlank(ogDescription) ? description: ogDescription;
    }

    /**
     * Constructs an absolute og:image URL, if and only if, a primary domain is set and and og:image is set. The
     * og:image may be set on the page, an ancestor page, or the page template.
     *
     * @return An absolute og:image URL on success and an empty string otherwise.
     */
    public String getAbsOgImage() {

        String absOgImagePath = "";

        // 1. try current page
        String ogImagePath = getResource().getValueMap().get("ogImage", String.class);

        // 2. try to fallback to closest parent or ancestor page
        if (StringUtils.isBlank(ogImagePath)) {
            ogImagePath  = (String) getInheritedProperty("ogImage");

            // 3. try to fallback to template
            if (StringUtils.isBlank(ogImagePath) && getTemplate() != null) {
                PageModel templatePageModel = getTemplatePageModel();
                if (templatePageModel != null) {
                    ogImagePath = templatePageModel.ogImage;
                }
            }
        }

        final String domain = StringUtils.isNotBlank(getPrimaryDomain())
                ? getPrimaryDomain()
                : getFallbackDomain();

        if (StringUtils.isNotBlank(domain) & StringUtils.isNotBlank(ogImagePath)) {
            absOgImagePath = domain + ogImagePath;
        }

        return absOgImagePath;
    }

    public String getCanonicalUrl() {
        final String primaryDomain = getPrimaryDomain();

        return StringUtils.isNotBlank(primaryDomain)
                ? getPrimaryDomain() + getPagePath().replace(getSiteRoot(), "") + ".html"
                : getPagePath() + ".html";
    }

    public String getMetaRobots() {

        StringBuilder metaRobots = new StringBuilder();

        if (noIndex) metaRobots.append("noindex");
        if (noIndex && noFollow) metaRobots.append(",");
        if (noFollow) metaRobots.append("nofollow");

        return metaRobots.toString();
    }

    class Tag {
        private String path;
        private String name;
        private String value;

        public Tag(Resource r) {
            this.path = r.getPath();
            this.path = path.substring(path.indexOf("/jcr:content"));
            this.name = r.getName();
            String tag = r.getValueMap().get("value", String.class);
            Resource tagResource = r.getResourceResolver().getResource(tag);
            if (tagResource != null) {
                this.value = tagResource.getValueMap().get("value", String.class);
            }
        }

        public String getName() { return name; }
        public String getValue() { return value; }
        public String getPath() { return path; }
        @Override
        public String toString() { return name; }
    }

    public class MetaProperty {
        public String path;
        public String metatype;
        public String key;
        public String value;

        public MetaProperty(Resource r) {
            this.path = r.getPath();
            this.path = path.substring(path.indexOf("/jcr:content"));
            this.metatype = r.getValueMap().get("metatype", String.class);
            this.key = r.getValueMap().get("key", String.class);
            this.value = r.getValueMap().get("value", String.class);
        }

        public String getPath() { return path; }
        public String getMetaType() { return metatype; }
        public String getKey() { return key; }
        public String getValue() { return value; }
        public Boolean isProperty() { return "property".equalsIgnoreCase(metatype); }
        public Boolean isName() { return "name".equalsIgnoreCase(metatype); }
    }
}
