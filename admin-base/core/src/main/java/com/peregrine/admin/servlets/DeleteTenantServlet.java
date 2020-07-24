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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_DELETE_TENANT;
import static com.peregrine.admin.util.AdminConstants.GROUP_NAME_SUFFIX;
import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.admin.util.AdminConstants.USER_NAME_SUFFIX;
import static com.peregrine.commons.util.PerConstants.CONTENT_ROOT;
import static com.peregrine.commons.util.PerConstants.DELETED;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.SITE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.STATUS;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
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
import java.util.Iterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Deletes a Peregrine Site
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Delete Site servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_DELETE_TENANT
    }
)
@SuppressWarnings("serial")
public class DeleteTenantServlet extends AbstractBaseServlet {

    private static final String FAILED_TO_DELETE_SITE = "Failed to delete site";
    private static final String FAILED_TO_REMOVE_TENANT_SECURITY = "Unable to remove Tenant Permissions";

    @Reference
    ModelFactory modelFactory;

    @Reference
    AdminResourceHandler resourceManagement;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String fromTenant = request.getParameter(NAME);
        boolean isAdmin = request.isAdmin();
        ResourceResolver resourceResolver = null;
        try {
            logger.debug("Delete Site form: '{}'", fromTenant);
            resourceResolver = isAdmin ?
                request.getResourceResolver() :
                loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME);
            resourceManagement.deleteTenant(resourceResolver, CONTENT_ROOT, fromTenant);
            // Remove the Tenant Group with is assigned to that tenant
            String tenantGroupId = fromTenant + GROUP_NAME_SUFFIX;
            String tenantUserId = fromTenant + USER_NAME_SUFFIX;
            Session adminSession = resourceResolver.adaptTo(Session.class);
            UserManager userManager = AccessControlUtil.getUserManager(adminSession);
            Group tenantGroup = (Group) userManager.getAuthorizable(tenantGroupId);
            if(tenantGroup != null) {
                Iterator<Authorizable> i = tenantGroup.getDeclaredMembers();
                boolean removeGroup = true;
                while(i.hasNext()) {
                    Authorizable member = i.next();
                    // Tenant User could still be around if it is part of another group that it's tenant (see above)
                    if(!member.getID().equals(tenantUserId)) {
                        removeGroup = false;
                        break;
                    }
                }
                if(removeGroup) {
                    try {
                        tenantGroup.remove();
                    } catch (RepositoryException e) {
                        // Ignore for now
                    }
                }
            }
            resourceResolver.commit();
            return new JsonResponse()
                .writeAttribute(TYPE, SITE)
                .writeAttribute(STATUS, DELETED)
                .writeAttribute(SOURCE_PATH, CONTENT_ROOT + SLASH + fromTenant);
        } catch(ManagementException | LoginException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_DELETE_SITE)
                .setException(e);
        } catch (RepositoryException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_REMOVE_TENANT_SECURITY)
                .setException(e);
        } finally {
            if(!isAdmin && resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }
}

