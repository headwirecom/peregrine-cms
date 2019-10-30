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
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.admin.util.AdminPathConstants.RESOURCE_TYPE_CREATION_PAGE;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PAGE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TEMPLATE_PATH;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Creates a Peregrine Page
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definintions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Create Page servlet",
        SERVICE_VENDOR + EQUAL + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUAL + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUAL + RESOURCE_TYPE_CREATION_PAGE
    },
    reference = {
        @Reference(name = "ModelFactory", bind = "setModelFactory", service = ModelFactory.class),
        @Reference(name = "AdminResourceHandler", bind = "setResourceManagement", service = AdminResourceHandler.class)
    }
)
@SuppressWarnings("serial")
public class CreatePageServlet extends AbstractCreateServlet {

    public static final String FAILED_TO_CREATE_PAGE = "Failed to create page";

    @Override
    protected String getType() { return PAGE; }

    @Override
    protected String getFailureMessage() { return FAILED_TO_CREATE_PAGE; }

    @Override
    protected Resource doAction(Request request) throws ManagementException {
        String parentPath = request.getParameter(PATH);
        String name = request.getParameter(NAME);
        String templatePath = request.getParameter(TEMPLATE_PATH);
        return resourceManagement.createPage(request.getResourceResolver(), parentPath, name, templatePath, name);
    }

    @Override
    protected void enhanceResponse(JsonResponse response, Request request) throws IOException {
        response.writeAttribute(TEMPLATE_PATH, request.getParameter(TEMPLATE_PATH));
    }
}

