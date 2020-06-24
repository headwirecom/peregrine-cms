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
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_ACCESS;
import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.commons.util.PerUtil.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Access Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_ACCESS
    }
)
@SuppressWarnings("serial")
public class AccessServlet extends AbstractBaseServlet {

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {

        JsonResponse jsonResponse = new JsonResponse();
        return jsonResponse.writeMap(getInfo(request));
    }

    private Map<String, String> getInfo(final Request request) {

        final Map<String, String> info = new HashMap<>();
        final ResourceResolver resolver = request.getResourceResolver();

        info.put("userID", resolver.getUserID());

        Authorizable authorizable = null;
        try
        {
            authorizable = getUserManager(request).getAuthorizable(request.getRequest().getUserPrincipal());
            Resource resource = resolver.getResource(authorizable.getPath());

            // TODO: Refactor - serialize node to JSON
            // TODO: Read white list from OSGi configuration admin
            if (resource != null) {
                info.put("firstLogin", resource.getValueMap().get("preferences/firstLogin", String.class));
                info.put("profileEmail", resource.getValueMap().get("profile/email", String.class));
                info.put("profileAvatar", resource.getValueMap().get("profile/avatar", String.class));
            }
        } catch (Exception e)
        {
            logger.warn("Error getting user's profile", e);
        }

        return info;
    }

    private UserManager getUserManager(final Request request) {

        ResourceResolver resourceResolver = null;
        UserManager userManager = null;

        try
        {
            resourceResolver = request.isAdmin() ?
                    request.getResourceResolver() :
                    loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME);
            Session adminSession = resourceResolver.adaptTo(Session.class);
            userManager = AccessControlUtil.getUserManager(adminSession);

        } catch (Exception e)
        {
            logger.warn("Error getting UserManager", e);
        }

        return userManager;
    }
}

