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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_MOVE_NODE;
import static com.peregrine.admin.util.AdminConstants.BEFORE_POSTFIX;
import static com.peregrine.admin.util.AdminConstants.INTO;
import static com.peregrine.admin.util.AdminConstants.MODEL_JSON;
import static com.peregrine.admin.util.AdminConstants.NOT_PROVIDED;
import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.DROP;
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
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Moves an Existing Node in a given Parent Node either as child or sibling
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Move Node To Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_MOVE_NODE
    }
)
@SuppressWarnings("serial")
public class MoveNodeTo extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        Response answer;
        String toPath = request.getParameter(PATH);
        String fromPath = request.getParameter(COMPONENT);
        String type = request.getParameter(TYPE);
        // Next Block is only here to be backwards compatible
        if(type == null || type.isEmpty()) {
            type = request.getParameter(DROP, NOT_PROVIDED);
        }
        boolean addAsChild = ORDER_CHILD_TYPE.equals(type) || type.startsWith(INTO);
        boolean addBefore = ORDER_BEFORE_TYPE.equals(type) || type.endsWith(BEFORE_POSTFIX);
        logger.trace("Add resource: '{}' to {}: '{}' {}",
            fromPath, addAsChild ? "parent" : "sibling", toPath, addBefore ? "before" : "after");
        try {
            Resource toResource = request.getResourceByPath(toPath);
            Resource fromResource = request.getResourceByPath(fromPath);
            resourceManagement.moveNode(fromResource, toResource, addAsChild, addBefore);
            request.getResourceResolver().commit();
            answer = new RedirectResponse((addAsChild ? toPath : toResource.getParent().getPath()) + MODEL_JSON);
        } catch(ManagementException e) {
            logger.error("problems while moving", e);
            answer = new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(e.getMessage())
                .setException(e);
        }
        return answer;
    }
}

