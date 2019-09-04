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
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_GET_COMPONENT;
import static com.peregrine.commons.util.PerUtil.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Forwards the Request to .model.json to get the exported model json for a component
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definintions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Get Component Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_GET_COMPONENT
    }
)
@SuppressWarnings("serial")
public class GetComponentServlet extends AbstractBaseServlet {

    private static final String MODEL = "=model";

    @Override
    protected Response handleRequest(Request request) throws IOException {

        String suffix = request.getSuffix();
        if(suffix.endsWith(".model.json")) {
            suffix = suffix.substring(0, suffix.indexOf(".model.json"));
        }
        Resource res = request.getResourceByPath(suffix);
        RequestDispatcherOptions rdOtions = new RequestDispatcherOptions(
            RequestDispatcherOptions.OPT_REPLACE_SELECTORS + MODEL
        );
        return new ForwardResponse(res, rdOtions);
    }
}