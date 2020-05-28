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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_COPY;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.commons.util.PerConstants.ORDER_CHILD_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.util.PerUtil;
import java.io.IOException;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Copy Resource Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_COPY,
        SLING_SERVLET_SELECTORS + EQUALS + JSON
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides the ability to copy a resource
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definintions/admin.yaml
 */
public class CopyServlet extends AbstractBaseServlet {

    public static final String TAGET_NAME = "tagetName";
    public static final String TARGET_PATH = "targetPath";
    public static final String TO = "to";
    public static final String NEW_NAME = "newName";
    public static final String NEW_TITLE = "newTitle";
    public static final String DEEP = "deep";

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String fromPath = request.getParameter(PATH);
        Resource from = PerUtil.getResource(request.getResourceResolver(), fromPath);
        String toPath = request.getParameter(TO);
        Resource newParent = null;
        Resource nextSibling = null;
        String type = request.getParameter(TYPE);
        String newName = request.getParameter(NEW_NAME);
        String newTitle = request.getParameter(NEW_TITLE);
        String deepParam = request.getParameter(DEEP);
        boolean deep = false;
        deep = "true".equalsIgnoreCase(deepParam);
        if(ORDER_CHILD_TYPE.equals(type)) {
            newParent = PerUtil.getResource(request.getResourceResolver(), toPath);
        }
        else if(ORDER_BEFORE_TYPE.equals(type)) {
            nextSibling = PerUtil.getResource(request.getResourceResolver(), toPath);
            if(nextSibling != null) {
                newParent = nextSibling.getParent();
            }
        }
        else {
            return new ErrorResponse().
                setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage("Unknown type parameter: " + type)
                .setRequestPath(fromPath);
        }

        Resource copiedResource = null;
        ResourceResolver resourceResolver = request.getResourceResolver();
        try {
            copiedResource = resourceManagement
                .copyResource(resourceResolver, from, newParent, newName, newTitle, nextSibling, deep);
        } catch (ManagementException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(e.getMessage())
                .setRequestPath(fromPath)
                .setException(e);
        }

        resourceResolver.commit();
        JsonResponse answer = new JsonResponse();
        answer.writeAttribute(SOURCE_NAME, from.getName());
        answer.writeAttribute(SOURCE_PATH, from.getPath());
        answer.writeAttribute(TAGET_NAME, copiedResource.getName());
        answer.writeAttribute(TARGET_PATH, copiedResource.getPath());
        return answer;
    }
}
