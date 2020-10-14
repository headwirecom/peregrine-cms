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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_MOVE;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_RENAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.*;
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

import com.peregrine.admin.replication.ReplicationUtil;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.util.PerUtil;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Move Resource Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_MOVE,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_RENAME,
        SLING_SERVLET_SELECTORS + EQUALS + JSON
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides the ability to move a resource
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
public class MoveServlet extends AbstractBaseServlet {

    public static final String TARGET_NAME = "targetName";
    public static final String TARGET_PATH = "targetPath";
    public static final String RENAME = "rename";
    public static final String MOVE = "move";
    public static final String TO = "to";

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final ResourceResolver resourceResolver = request.getResourceResolver();
        final String fromPath = request.getParameter(PATH);
        if (Optional.ofNullable(fromPath)
                .map(resourceResolver::getResource)
                .map(ReplicationUtil::isSelfOrAnyDescendantReplicated)
                .orElse(false)
        ) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage("Failed to update a replicated node.")
                    .setRequestPath(fromPath);
        }

        final String newTitle = request.getParameter(TITLE);
        final String toPath = request.getParameter(TO);
        Resource from = PerUtil.getResource(resourceResolver, fromPath);
        Resource newResource;
        final String name = request.getResource().getName();
        if (name.equals(MOVE)) {
            String type = request.getParameter(TYPE);
            Resource to = PerUtil.getResource(resourceResolver, toPath);
            boolean addAsChild = ORDER_CHILD_TYPE.equals(type);
            boolean addBefore = ORDER_BEFORE_TYPE.equals(type);
            try {
                newResource = resourceManagement.moveNode(from, to, addAsChild, addBefore);
            } catch(ManagementException e) {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(e.getMessage())
                    .setRequestPath(fromPath)
                    .setException(e);
            }
        } else if(name.equals(RENAME)) {
            try {
                newResource = resourceManagement.rename(from, toPath);
                if (StringUtils.isNotEmpty(newTitle)) {
                    if (newResource.getResourceType().equals("per:Asset")){
                        resourceManagement.updateOrCreateAssetTitle(newResource.getChild(JCR_CONTENT), newTitle);
                    } else {
                        resourceManagement.updateTitle(newResource.getChild(JCR_CONTENT), newTitle);
                    }
                }
            } catch(ManagementException e) {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(e.getMessage())
                    .setRequestPath(fromPath)
                    .setException(e);
            }
        } else {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage("Unknown request: " + name);
        }

        resourceResolver.commit();
        JsonResponse answer = new JsonResponse();
        answer.writeAttribute(SOURCE_NAME, from.getName());
        answer.writeAttribute(SOURCE_PATH, from.getPath());
        answer.writeAttribute(TARGET_NAME, newResource.getName());
        answer.writeAttribute(TARGET_PATH, newResource.getPath());
        return answer;
    }
}
