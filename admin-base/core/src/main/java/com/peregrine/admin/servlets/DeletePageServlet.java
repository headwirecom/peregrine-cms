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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_DELETE_PAGE;
import static com.peregrine.commons.util.PerConstants.DELETED;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.NODE_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PARENT_PATH;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.STATUS;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.DeletionResponse;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import javax.servlet.Servlet;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Deletes a Peregrine Page
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Delete Page Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_DELETE_PAGE
    }
)
@SuppressWarnings("serial")
public class DeletePageServlet extends AbstractBaseServlet {

    public static final String FAILED_TO_DELETE_NODE = "Failed to delete node: ";

    @Reference
    ModelFactory modelFactory;

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String path = request.getParameter(PATH);
        try {
            DeletionResponse response = resourceManagement
                .deleteResource(request.getResourceResolver(), path, PAGE_PRIMARY_TYPE);
            request.getResourceResolver().commit();
            return new JsonResponse()
                .writeAttribute(TYPE, PAGE)
                .writeAttribute(STATUS, DELETED)
                .writeAttribute(NAME, response.getName())
                .writeAttribute(NODE_TYPE, response.getType())
                .writeAttribute(PARENT_PATH, response.getParentPath());
        } catch (ManagementException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_DELETE_NODE + path)
                .setRequestPath(path).setException(e);
        }
    }
}

