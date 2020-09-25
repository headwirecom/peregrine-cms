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
 * Contributed by Cris Rockwell, University of Michigan
 */

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.servlet.Servlet;
import java.io.IOException;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_MANAGE_VERSIONS;
import static com.peregrine.admin.servlets.ListResourceVersionsServlet.RESOURCE_NOT_FOUND;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerUtil.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;


/**
 * Manages resource version actions
 * including; Create, Delete and Checkout
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Manage resource's versions",
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_MANAGE_VERSIONS
    }
)

@SuppressWarnings("serial")
public class ManageVersions extends AbstractBaseServlet {

    private static final String FAILED_TO_CREATE_VERSION = "Failed to create version";
    private static final String FAILED_TO_DELETE_VERSION = "Failed to delete version";
    private static final String FAILED_TO_RESTORE_VERSION = "Failed to delete version";
    private static final String UNKNOWN_ACTION = "Bad request. unsupported or missing action";
    private static final String RESOURCE_FROZEN = "Resource Checked-in & frozen. ";
    private static final String VERSION_MGT_ERROR = "Versions cannot be managed.";
    private static final String ACTION = "action";
    private static final String VERSION = "version";
    private static final String CREATE_VERSION = "createVersion";
    private static final String DELETE_VERSION = "deleteVersion";
    private static final String RESTORE_VERSION = "restoreVersion";


    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {

        final String resourcePath = request.getSuffix();
        final String version = request.getParameter(VERSION);
        final String action = request.getParameter(ACTION);
        JsonResponse answer = new JsonResponse();
        Resource resource = request.getResourceResolver().getResource(resourcePath);
        if (resource == null) {
            return new ErrorResponse().setHttpErrorCode(SC_NOT_FOUND).setErrorMessage(RESOURCE_NOT_FOUND);
        }
        Resource resourceContent = resource.getChild(JCR_CONTENT);
        if (resourceContent == null) {
            resourceContent = resource;
        }

        try {
            if (!resourceManagement.isCheckedOut(request.getResourceResolver(), resourceContent.getPath())){
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(RESOURCE_FROZEN+VERSION_MGT_ERROR)
                    .setRequestPath(resourcePath);
            }
        } catch (ManagementException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(VERSION_MGT_ERROR)
                .setRequestPath(resourcePath)
                .setException(e);
        }
        if (CREATE_VERSION.equals(action)) {
            Version newVersion = null;
            try {
                newVersion = resourceManagement.createVersion(request.getResourceResolver(), resourceContent.getPath());
                answer.writeAttribute("new_version", newVersion.getName());
                answer.writeClose();
                return answer;
            } catch (ManagementException | RepositoryException e) {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_CREATE_VERSION)
                    .setRequestPath(resourcePath)
                    .setException(e);
            }
        } else if (DELETE_VERSION.equals(action)) {
            try {
                resourceManagement.deleteVersion(request.getResourceResolver(), resourceContent.getPath(), version);
                return answer;
            } catch (RepositoryException e) {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_DELETE_VERSION)
                    .setRequestPath(resourcePath)
                    .setException(e);
            }
        } else if (RESTORE_VERSION.equals(action)) {
            try {
                resourceManagement.restoreVersionByName(
                    request.getResourceResolver(),
                    resourceContent.getPath(),
                    version,
                    true);
                return answer;
            } catch (RepositoryException e) {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_RESTORE_VERSION)
                    .setRequestPath(resourcePath)
                    .setException(e);
            }
        }
        return new ErrorResponse()
            .setHttpErrorCode(SC_BAD_REQUEST)
            .setErrorMessage(UNKNOWN_ACTION)
            .setRequestPath(resourcePath);
    }
}

