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
import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.commons.util.PerConstants.ALL_TENANTS_GROUP_NAME;
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
import static com.peregrine.commons.util.PerConstants.TENANT_GROUP_HOME;
import static com.peregrine.commons.util.PerConstants.TENANT_USER_HOME;
import static com.peregrine.commons.util.PerConstants.TENANT_USER_PWD;
import static com.peregrine.commons.util.PerConstants.TO_SITE_NAME;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.loginService;
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
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.servlet.Servlet;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.base.util.AccessControlUtil;
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
    private static final String FAILED_TO_GET_SERVICE_RESOLVER = "Unable to get Peregrine Service Resolver";
    private static final String FAILED_TO_CREATE_TENANT_SECURITY = "Unable to create Tenant Permissions";

    @Reference
    AdminResourceHandler resourceManagement;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String fromSite = request.getParameter(FROM_SITE_NAME);
        String toSite = request.getParameter(TO_SITE_NAME);
        try {
            logger.trace("Copy Site form: '{}' to: '{}'", fromSite, toSite);
            ResourceResolver resourceResolver = loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME);
            Session adminSession = resourceResolver.adaptTo(Session.class);
            UserManager userManager = AccessControlUtil.getUserManager(adminSession);
            Resource site = resourceManagement
                .copySite(resourceResolver, CONTENT_ROOT, fromSite, toSite);
            // Check if Admin user and if not get password
            String userName = request.getRequest().getUserPrincipal().getName();
            boolean isAdmin = "admin".equals(userName);
            // Get User Password
            String userPwd = isAdmin ? "" : request.getParameter(TENANT_USER_PWD);
            boolean isPwdProvided = isNotEmpty(userPwd);
            // Create Tenant Group
            String tenantGroupId = toSite + "_group";
            String tenantUserId = toSite + "_user";
            Group tenantGroup;
            try {
                tenantGroup = userManager.createGroup(
                    () -> tenantGroupId,
                    TENANT_GROUP_HOME
                );
            } catch (AuthorizableExistsException e) {
                tenantGroup = (Group) userManager.getAuthorizable(tenantGroupId);
            }
            if(tenantGroup == null) {
                // Maybe make a better message as we cannot create / find the Tenant Group
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_CREATE_TENANT_SECURITY);
            }
            // Add Tenant Group to All Tenants Group
            Group allTenantsGroup = (Group) userManager.getAuthorizable(ALL_TENANTS_GROUP_NAME);
            if(allTenantsGroup == null) {
                // Maybe make a better message as we cannot create / find the All Tenants Group
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_CREATE_TENANT_SECURITY);
            }
            // Make Tenant Group member of the All Tenants Group
            allTenantsGroup.addMember(tenantGroup);
            if(!isAdmin) {
                // We also need to add the current non-admin user to the group so that new site is visible for them
                Authorizable authorizable = userManager.getAuthorizable(userName);
                if(authorizable != null) {
                    tenantGroup.addMember(authorizable);
                }
                // Create Tenant User as this the creator is not Admin
                User tenantUser;
                try {
                    tenantUser = userManager.createUser(
                        tenantUserId,
                        userPwd,
                        () -> tenantUserId,
                        TENANT_USER_HOME
                    );
                    if(tenantUser != null && !isPwdProvided) {
                        tenantUser.disable("Need a password first");
                    }
                } catch (AuthorizableExistsException e) {
                    tenantUser = (User) userManager.getAuthorizable(tenantUserId);
                }
                if(tenantUser == null) {
                    // Maybe make a better message as we cannot create / find the Tenant User
                    return new ErrorResponse()
                        .setHttpErrorCode(SC_BAD_REQUEST)
                        .setErrorMessage(FAILED_TO_CREATE_TENANT_SECURITY);
                }
                tenantGroup.addMember(tenantUser);
            }
            // Finally set permissions on site
            try {
                AccessControlManager accessControlManager = adminSession.getAccessControlManager();
                JackrabbitAccessControlList policies = AccessControlUtils.getAccessControlList(accessControlManager, site.getPath());
                Privilege[] privileges = AccessControlUtils.privilegesFromNames(accessControlManager, "jcr:all");
                policies.addEntry(allTenantsGroup.getPrincipal(), privileges, false, null);
                policies.addEntry(tenantGroup.getPrincipal(), privileges, true, null);
                accessControlManager.setPolicy(site.getPath(), policies);
                // Done settings permissions
            } catch(RuntimeException e) {
                logger.warn("Setting Site Permissions failed", e);
            }
            request.getResourceResolver().commit();
            if (fromSite.equals("themecleanflex")) {
                setColorPalette(resourceResolver, request.getParameter(COLOR_PALETTE), fromSite, toSite);
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
        } catch (LoginException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_GET_SERVICE_RESOLVER)
                .setException(e);
        } catch (RepositoryException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_CREATE_TENANT_SECURITY)
                .setException(e);
        }
    }

    private void setColorPalette(ResourceResolver resourceResolver, String colorPalette, String fromSite, String toSite) throws PersistenceException {
        final Resource templateContent = getResource(
            resourceResolver,
            TEMPLATES_ROOT.replace(TENANT, toSite)
        ).getChild(JCR_CONTENT);

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
            resourceResolver.commit();
        } else {
            logger.error("No siteCSS property found for copied template");
        }
    }
}

