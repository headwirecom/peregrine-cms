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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_CREATION_SITE;
import static com.peregrine.commons.util.PerConstants.COLOR_PALETTE;
import static com.peregrine.commons.util.PerConstants.CONTENT_ROOT;
import static com.peregrine.commons.util.PerConstants.CREATED;
import static com.peregrine.commons.util.PerConstants.FROM_SITE_NAME;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SITE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.STATUS;
import static com.peregrine.commons.util.PerConstants.TEMPLATES_ROOT;
import static com.peregrine.commons.util.PerConstants.TENANT;
import static com.peregrine.commons.util.PerConstants.TO_SITE_NAME;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getResource;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates a Peregrine Site by copying another existing Site
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Create Site servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_CREATION_SITE
    }
)
@SuppressWarnings("serial")
public class CreateSiteServlet extends AbstractBaseServlet {

    private static final String FAILED_TO_CREATE_SITE = "Failed to create site";

    @Reference
    ModelFactory modelFactory;

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String fromSite = request.getParameter(FROM_SITE_NAME);
        String toSite = request.getParameter(TO_SITE_NAME);
        try {
            logger.trace("Copy Site form: '{}' to: '{}'", fromSite, toSite);
            Resource site = resourceManagement
                .copySite(request.getResourceResolver(), CONTENT_ROOT, fromSite, toSite);
            request.getResourceResolver().commit();
            if (fromSite.equals("themecleanflex")) {
                setColorPalette(request, fromSite, toSite);
            }
            return new JsonResponse()
                .writeAttribute(TYPE, SITE)
                .writeAttribute(STATUS, CREATED)
                .writeAttribute(NAME, toSite)
                .writeAttribute(PATH, site.getPath())
                .writeAttribute(SOURCE_PATH, CONTENT_ROOT + SLASH + fromSite);
        } catch(ManagementException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_CREATE_SITE)
                .setException(e);
        }
    }

    private void setColorPalette(Request request, String fromSite, String toSite) throws PersistenceException {
        final Resource templateContent = getResource(
            request.getResourceResolver(),
            TEMPLATES_ROOT.replace(TENANT, toSite)).getChild(JCR_CONTENT);
        final String colorPalette = request.getParameter(COLOR_PALETTE);

        if(templateContent == null) {
            logger.error("No jcr:content resource for copied template");
            return;
        }

        ValueMap templateContentProperties = templateContent.getValueMap();
        String[] siteCssProperty = templateContentProperties.get("siteCSS", String[].class);

        if(siteCssProperty != null && siteCssProperty.length > 0) {
            String[] cssReplacements = Arrays.stream(siteCssProperty)
                .map(css -> {
                    if (css.contains("/palettes/")) {
                        return colorPalette.replace(fromSite, toSite);
                    } else {
                        return css;
                    }
                }).toArray(String[]::new);
            ModifiableValueMap modifiableProperties = getModifiableProperties(templateContent);
            modifiableProperties.put("siteCSS", cssReplacements);
            request.getResourceResolver().commit();
        } else {
            logger.error("No siteCSS property found for copied template");
        }
    }
}

