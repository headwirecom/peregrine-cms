package com.peregrine.admin.servlets;

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

import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.servlets.ServletHelper;
import com.peregrine.commons.util.PerConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_COMPONENT_DEFINITION;
import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.MODEL;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_SUPER_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Provides the Component Definition of a Resource
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definintions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Component Definition Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_COMPONENT_DEFINITION
    }
)
@SuppressWarnings("serial")
public class ComponentDefinitionServlet extends AbstractBaseServlet {

    public static final String EXPLORER_DIALOG_JSON = "explorer_dialog.json";
    public static final String DIALOG_JSON = "dialog.json";

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String path = request.getParameter(PATH);
        Resource resource = request.getResourceByPath(path);
        boolean page = false;
        if(resource.getResourceType().equals(PerConstants.PAGE_PRIMARY_TYPE)) {
            page = true;
            resource = resource.getChild(PerConstants.JCR_CONTENT);
        }
        String componentPath = "";
        if(path.startsWith(APPS_ROOT + SLASH)) {
            componentPath = path;
        } else {
            componentPath = APPS_ROOT + SLASH + resource.getValueMap().get(SLING_RESOURCE_TYPE, String.class);
        }

        Resource component = request.getResourceByPath(componentPath);
        logger.debug("Component Path: '{}', Component: '{}'", componentPath, component);
        if("/apps/admin/components/assetview".equals(path)) {
            page = true;
        }
        Resource dialog = component.getChild(page ? EXPLORER_DIALOG_JSON : DIALOG_JSON);
        if(dialog == null) {
            dialog = getDialogFromSuperType(component, page);
        }
        JsonResponse answer = new JsonResponse();
        answer.writeAttribute(PATH, componentPath);
        answer.writeAttribute(NAME, ServletHelper.componentPathToName(componentPath));
        if(dialog != null) {
            answer.writeAttributeRaw(MODEL, ServletHelper.asString(dialog.adaptTo(InputStream.class)).toString());
        }
        return answer;
    }

    private Resource getDialogFromSuperType(Resource resource, boolean page) {
        String componentPath = resource.getValueMap().get(SLING_RESOURCE_SUPER_TYPE, String.class);
        if(componentPath != null) {
            if (!componentPath.startsWith(APPS_ROOT + SLASH)) {
                componentPath = APPS_ROOT + SLASH + componentPath;
            }
            ResourceResolver resourceResolver = resource.getResourceResolver();
            Resource component = resourceResolver.getResource(componentPath);
            Resource dialog = component.getChild(page ? EXPLORER_DIALOG_JSON : DIALOG_JSON);
            if (dialog == null) {
                return getDialogFromSuperType(component, page);
            } else {
                return dialog;
            }
        } else {
            return null;
        }
    }

}

