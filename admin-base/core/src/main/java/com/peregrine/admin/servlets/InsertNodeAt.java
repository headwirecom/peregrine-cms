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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_INSERT_NODE;
import static com.peregrine.admin.util.AdminConstants.BEFORE_POSTFIX;
import static com.peregrine.admin.util.AdminConstants.INTO;
import static com.peregrine.admin.util.AdminConstants.MODEL_JSON;
import static com.peregrine.admin.util.AdminConstants.NOT_PROVIDED;
import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.CONTENT;
import static com.peregrine.commons.util.PerConstants.DROP;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.commons.util.PerConstants.ORDER_CHILD_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerConstants.VARIATION;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.getStringOrNull;
import static com.peregrine.commons.util.PerUtil.isPrimaryType;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.admin.resource.ResourceRelocation;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.servlets.ServletHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates a New Node in a given Parent Node either as child or sibling
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "insert node at servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_INSERT_NODE
    }
)
@SuppressWarnings("serial")
public class InsertNodeAt extends AbstractBaseServlet {

    private static final String INNER_JCR_CONTENT = SLASH + JCR_CONTENT + SLASH;
    private static final String APPS_PREFIX = APPS_ROOT + SLASH;
    private static final String FAILED_TO_CREATE_INTERMEDIATE_RESOURCES = "Failed to create intermediate resources";
    private static final String RESOURCE_NOT_FOUND_BY_PATH = "Resource not found by Path";

    private final ObjectMapper mapper = new ObjectMapper();

    @Reference
    private ResourceRelocation resourceRelocation;

    @Reference
    private AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final String path = request.getParameter(PATH);
        final Resource resource;
        try {
            resource = getOrCreateResource(request.getResourceResolver(), path);
        } catch(ManagementException e) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_CREATE_INTERMEDIATE_RESOURCES)
                    .setRequestPath(path);
        }

        //AS End of Patch
        if(isNull(resource)) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(RESOURCE_NOT_FOUND_BY_PATH)
                .setRequestPath(path);
        }

        String type = request.getParameter(TYPE);
        // Next Block is only here to be backwards compatible
        if (isEmpty(type)) {
            type = request.getParameter(DROP, NOT_PROVIDED);
        }

        final boolean addAsChild = ORDER_CHILD_TYPE.equals(type) || type.startsWith(INTO);
        final boolean addBefore = ORDER_BEFORE_TYPE.equals(type) || type.endsWith(BEFORE_POSTFIX);
        String component = request.getParameter(COMPONENT);
        if(startsWith(component, APPS_PREFIX)) {
            component = substringAfter(component, APPS_PREFIX);
        }

        final Map<String, Object> properties = new HashMap<>();
        final String data = request.getParameter(CONTENT);
        if(isNotEmpty(data)) {
            properties.putAll(mapper.readValue(data, Map.class));
        }

        if(isNotEmpty(component)) {
            // Component overrides the Json component if provided
            properties.put(COMPONENT, component);
        } else {
            component = getStringOrNull(properties, COMPONENT);
            if(nonNull(component)) {
                properties.put(COMPONENT, ServletHelper.componentNameToPath(component));
            }
        }

        final String variation = request.getParameter(VARIATION);
        try {
            final Resource newResource = resourceManagement
                .insertNode(resource, properties, addAsChild, addBefore, variation);
            newResource.getResourceResolver().commit();
            return new RedirectResponse((addAsChild ? path : resource.getParent().getPath()) + MODEL_JSON);
        } catch (ManagementException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(e.getMessage())
                .setException(e);
        }
    }

    private Resource getOrCreateResource(final ResourceResolver resourceResolver, final String path) throws ManagementException {
        Resource resource = getResource(resourceResolver, path);
        //AS This is a fix for missing intermediary nodes from templates
        if(nonNull(resource)) {
            return resource;
        }

        if(!contains(path, INNER_JCR_CONTENT)) {
            return null;
        }

        // We found jcr:content. Now we check if that is a page and if so traverse down the path and create all nodes until we reach the parent
        final String pagePath = substringBeforeLast(path, INNER_JCR_CONTENT);
        final Resource page = getResource(resourceResolver, pagePath);
        if(!isPrimaryType(page, PAGE_PRIMARY_TYPE)) {
            return null;
        }

        // Now we can traverse
        final String rest = substringAfter(path, pagePath + SLASH);
        logger.debug("Rest of Page Path: '{}'", rest);
        resource = page;
        for(String nodeName : rest.split(SLASH)) {
            if(isNotEmpty(nodeName)) {
                final Resource temp = resource.getChild(nodeName);
                logger.debug("Node Child Name: '{}', parent resource: '{}', resource found: '{}'", nodeName, resource.getPath(), temp == null ? "null" : temp.getPath());
                if(isNull(temp)) {
                    resource = resourceManagement
                        .createNode(resource, nodeName, NT_UNSTRUCTURED, null);
                } else {
                    resource = temp;
                }
            }
        }

        return resource;
    }
}

