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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.admin.resource.ResourceManagement;
import com.peregrine.admin.resource.ResourceManagement.ManagementException;
import com.peregrine.admin.resource.ResourceRelocation;
import com.peregrine.util.PerUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.peregrine.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.util.PerConstants.ORDER_CHILD_TYPE;
import static com.peregrine.util.PerUtil.EQUALS;
import static com.peregrine.util.PerUtil.PER_PREFIX;
import static com.peregrine.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "insert node at servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/insertNodeAt"
    }
)
@SuppressWarnings("serial")
public class InsertNodeAt extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Reference
    ResourceRelocation resourceRelocation;

    @Reference
    ResourceManagement resourceManagement;

    @Override
    Response handleRequest(Request request) throws IOException {
        String path = request.getParameter("path");
        Resource resource = PerUtil.getResource(request.getResourceResolver(), path);
        if(resource == null) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Resource not found by Path").setRequestPath(path);
        }
        String type = request.getParameter("type");
        // Next Block is only here to be backwards compatible
        if(type == null || type.isEmpty()) {
            type = request.getParameter("drop", "not provided");
        }
        boolean addAsChild = ORDER_CHILD_TYPE.equals(type) || type.startsWith("into");
        boolean addBefore = ORDER_BEFORE_TYPE.equals(type) || type.endsWith("-before");
        String component = request.getParameter("component");
        if(component != null && component.startsWith("/apps")) {
            component = component.substring(component.indexOf('/', 1) + 1);
        }
        Map<String, Object> properties = new HashMap<>();
        String data = request.getParameter("content");
        if(data != null && !data.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            properties.putAll(mapper.readValue(data, Map.class));
        }
        if(component != null && !component.isEmpty()) {
            // Component overrides the JSon component if provided
            properties.put("component", component);
        } else {
            component = properties.get("component") + "";
            if(component != null) {
                properties.put("component", ServletHelper.componentNameToPath(component));
            }
        }

        try {
            Resource newResource = resourceManagement.insertNode(resource, properties, addAsChild, addBefore);
            newResource.getResourceResolver().commit();
            return new RedirectResponse((addAsChild ? path : resource.getParent().getPath()) + ".model.json");
        } catch (ManagementException e) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(e.getMessage()).setException(e);
        }

    }
}

