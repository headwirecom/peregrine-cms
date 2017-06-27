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

import com.peregrine.util.PerUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_TITLE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.util.PerUtil.EQUALS;
import static com.peregrine.util.PerUtil.PER_PREFIX;
import static com.peregrine.util.PerUtil.PER_VENDOR;
import static com.peregrine.util.PerUtil.TEMPLATE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Create Page servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/createPage"
    }
)
@SuppressWarnings("serial")
public class CreatePageServlet extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Override
    Response handleRequest(Request request) throws IOException {
        String parentPath = request.getParameter("path");
        Resource parent = PerUtil.getResource(request.getResourceResolver(), parentPath);
        if(parent == null) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Parent Path not found").setRequestPath(parentPath);
        }
        String name = request.getParameter("name");
        if(name == null || name.isEmpty()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Page Name must be provided").setRequestPath(parentPath);
        }
        String templatePath = request.getParameter("templatePath");
        try {
            Resource templateResource = PerUtil.getResource(request.getResourceResolver(), templatePath + "/" + JCR_CONTENT);
            if(templateResource == null) {
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Could not find template with path: " + templatePath).setRequestPath(parentPath);
            }
            String templateComponent = templateResource.getValueMap().get(SLING_RESOURCE_TYPE, String.class);
            Node parentNode = parent.adaptTo(Node.class);
            Node newPage = parentNode.addNode(name, PAGE_PRIMARY_TYPE);
            Node content = newPage.addNode(JCR_CONTENT);
            content.setPrimaryType(PAGE_CONTENT_TYPE);
            content.setProperty(SLING_RESOURCE_TYPE, templateComponent);
            content.setProperty(JCR_TITLE, name);
            content.setProperty(TEMPLATE, templatePath);
            newPage.getSession().save();
            return new JsonResponse()
                .writeAttribute("type", "page").writeAttribute("status", "created")
                .writeAttribute("name", name).writeAttribute("path", newPage.getPath()).writeAttribute("templatePath", templatePath);
        } catch (RepositoryException e) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to create page").setException(e);
        }
    }

}

