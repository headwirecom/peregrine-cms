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

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.DeletionResponse;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.admin.util.AdminPathConstants.RESOURCE_TYPE_DELETE_SITE;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.SITE;
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Delete a Peregrine Site
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Delete Site servlet",
        SERVICE_VENDOR + EQUAL + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUAL + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUAL + RESOURCE_TYPE_DELETE_SITE
    },
    reference = {
        @Reference(name = "ModelFactory", bind = "setModelFactory", service = ModelFactory.class),
        @Reference(name = "AdminResourceHandler", bind = "setResourceManagement", service = AdminResourceHandler.class)
    }
)
@SuppressWarnings("serial")
public class DeleteSiteServlet extends AbstractDeleteServlet {

    public static final String FAILED_TO_DELETE_SITE = "Failed to delete site: ";

    @Override
    protected String getType() { return SITE; }

    @Override
    protected String getFailureMessage() { return FAILED_TO_DELETE_SITE; }

    @Override
    protected DeletionResponse doAction(Request request) throws ManagementException {
        String fromSite = request.getParameter(NAME);
        resourceManagement.deleteSite(request.getResourceResolver(), SITES_ROOT, fromSite);
        return null;
    }

    @Override
    protected void enhanceResponse(JsonResponse response, Request request) throws IOException {
        String fromSite = request.getParameter(NAME);
        response.writeAttribute(SOURCE_PATH, SITES_ROOT + SLASH + fromSite);
    }
}

