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
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.jcr.Session;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_ACCESS;
import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.commons.util.PerUtil.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * The AccessServlet returns session information about the current principal.
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    configurationPolicy = ConfigurationPolicy.OPTIONAL,
    immediate = true,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Access Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_ACCESS
    }
)
@Designate(ocd = AccessServlet.Configuration.class)
@SuppressWarnings("serial")
public class AccessServlet extends AbstractBaseServlet {

    @ObjectClassDefinition(
            name = "Peregrine: Access Servlet",
            description = "Provides information about the current principal"
    )
    @interface Configuration {
        @AttributeDefinition(
                name = "Profile Include List",
                description = "A list of allowed JCR property paths relative to a user's home directory that will be included in the response",
                required = true
        )
        String[] profile_include_list() default "preferences/firstLogin";
    }

    public static final String USER_ID = "userID";
    public static final String AUTH_TYPE = "authType";

    private List<String> profileIncludeList = new ArrayList<>();

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {

        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.writeAttribute(USER_ID, request.getResourceResolver().getUserID());

        if (request instanceof SlingHttpServletRequest && ((SlingHttpServletRequest)request).getAuthType() != null) {
            jsonResponse.writeAttribute(AUTH_TYPE, ((SlingHttpServletRequest)request).getAuthType());
        }

        convertResource(jsonResponse, getUserHome(request));

        return jsonResponse;
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

    private void convertResource(JsonResponse json, Resource resource) throws IOException {

        Iterable<Resource> children = resource.getChildren();
        for(Resource child : children) {

            if (isAllowedParentPath(child.getName()))
            {
                json.writeObject(child.getName());
            }

            for (String key: child.getValueMap().keySet()) {
                if (key.indexOf(":") < 0) {
                    StringBuilder curPath = new StringBuilder(child.getName());
                    curPath.append("/").append(key).toString();

                    if (isAllowedPath(curPath.toString()))
                    {
                        json.writeAttribute(key, child.getValueMap().get(key, String.class));
                    }
                }
            }

            convertResource(json, child);

            if (isAllowedParentPath(child.getName()))
            {
                json.writeClose();
            }
        }
    }

    private boolean isAllowedParentPath(final String path) {

        if (StringUtils.isNoneBlank(path)) {
            for (String includeListPath : profileIncludeList) {
                if (includeListPath.startsWith(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAllowedPath(final String path) {

        if (StringUtils.isNoneBlank(path)) {
            for (String includeListPath: profileIncludeList) {
                if (path.equals(includeListPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(AccessServlet.Configuration configuration) { setup(configuration); }

    @Modified
    @SuppressWarnings("unused")
    void modified(AccessServlet.Configuration configuration) { setup(configuration); }

    private void setup(AccessServlet.Configuration configuration) {

        profileIncludeList = new ArrayList<>();

        if (configuration.profile_include_list() != null)
        {
            for (String path : configuration.profile_include_list())
            {
                if (StringUtils.isNoneBlank(path))
                {
                    logger.debug("Adding profile include path: '{}'", path);
                    profileIncludeList.add(path);
                }
            }
        }
    }
}
