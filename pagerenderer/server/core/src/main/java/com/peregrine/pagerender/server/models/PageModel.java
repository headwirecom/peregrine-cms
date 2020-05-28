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
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.pagerender.server.models.PageRenderServerConstants.PR_SERVER_COMPONENT_PAGE_TYPE;

import com.peregrine.nodetypes.models.IComponent;
import javax.inject.Inject;
import javax.inject.Named;
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
    resourceType = {PR_SERVER_COMPONENT_PAGE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    adapters = IComponent.class)
@Exporter(
    name = JACKSON,
    extensions = JSON)
public class PageModel extends Container {

    public static final String SITE_CSS = "siteCSS";
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

    @Inject private ModelFactory modelFactory;

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

    @Inject private String dataFrom;

    @Inject private String dataDefault;

    @Inject private String[] loaders;

    @Inject private String[] suffixToParameter;

    public String getSiteRoot() {
        String path = getPagePath();
        String[] segments = path.split(SLASH);
        return String.join(SLASH, segments[0], segments[1], segments[2], segments[3]);
    }

    public String getPagePath() {
        return getResource().getParent().getPath();
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
                this.template = template;
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

    public boolean getServerSide() {
        return true;
    }
}
