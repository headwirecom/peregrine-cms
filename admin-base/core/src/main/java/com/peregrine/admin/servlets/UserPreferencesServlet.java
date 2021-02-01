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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_USER_PREFERENCES;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.loginService;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;

/**
 * set user preferences
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "User Preferences Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_USER_PREFERENCES
    }
)
@SuppressWarnings("serial")
public class UserPreferencesServlet extends AbstractBaseServlet {

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        Resource home = getUserHome(request);
        if(home != null) {
            try {
                Node preferences = null;
                if(home.getChild("preferences") != null) {
                    preferences = home.getChild("preferences").adaptTo(Node.class);
                } else {
                    preferences = home.adaptTo(Node.class).addNode("preferences");
                }
                preferences.setProperty("firstLogin", "false");
                request.getResourceResolver().adaptTo(Session.class).save();
            } catch( RepositoryException re) {
                logger.error("Error updating user preferences", re);
                // TODO: handle this
            }
        }
        JsonResponse answer = new JsonResponse();
        // TODO: should have a better return :-) 
        return answer;
    }

    private Resource getUserHome(final Request request) {

        final ResourceResolver resolver = request.getResourceResolver();
        Resource resource = null;

        try {
            Authorizable authorizable = getUserManager(request).getAuthorizable(request.getRequest().getUserPrincipal());
            resource = resolver.getResource(authorizable.getPath());
        } catch (Exception e) {
            logger.error("Error getting user's home", e);
        }
        return resource;
    }

    private UserManager getUserManager(final Request request) {

        ResourceResolver resourceResolver = null;
        UserManager userManager = null;

        try {
            resourceResolver = request.isAdmin() ?
                    request.getResourceResolver() :
                    loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME);
            Session adminSession = resourceResolver.adaptTo(Session.class);
            userManager = AccessControlUtil.getUserManager(adminSession);

        } catch (Exception e) {
            logger.error("Error getting UserManager", e);
        }

        return userManager;
    }

}

