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

import com.peregrine.admin.replication.ReferenceLister;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.convertSuffixToParams;
import static com.peregrine.admin.util.JcrUtil.EQUALS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + "Peregrine: Referenced By Lister Servlet",
        SERVICE_VENDOR + EQUALS + "headwire.com, Inc",
        SLING_SERVLET_METHODS + EQUALS + "GET",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/refBy",
        SLING_SERVLET_SELECTORS + EQUALS + "json"
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides a list of references of a given page
 */
public class ReferencedByListerServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ReferencedByListerServlet.class);

    @Reference
    private ReferenceLister referenceLister;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
        IOException
    {
        Map<String, String> params = convertSuffixToParams(request);
        log.debug("Parameters from Suffix: '{}'", params);
        String sourcePath = params.get("path");
        response.setContentType("application/json");
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            List<com.peregrine.admin.replication.Reference> references = referenceLister.getReferencedByList(source);
            StringBuffer answer = new StringBuffer();
            answer.append("{");
            answer.append("\"sourceName\":\"" + source.getName() + "\", ");
            answer.append("\"sourcePath\":\"" + source.getPath() + "\", ");
            answer.append("\"referencedBy\":[");
            boolean first = true;
            for(com.peregrine.admin.replication.Reference child : references) {
                if(first) {
                    first = false;
                } else {
                    answer.append(", ");
                }
                answer.append("{\"name\":\"");
                answer.append(child.getResource().getName());
                answer.append("\", \"path\":\"");
                answer.append(child.getResource().getPath());
                answer.append("\", \"propertyName\":\"");
                answer.append(child.getPropertyName());
                answer.append("\", \"propertyPath\":\"");
                answer.append(child.getPropertyResource().getPath());
                answer.append("\"}");
            }
            answer.append("]}");
            String temp = answer.toString();
            log.debug("Answer: '{}'", temp);
            response.getWriter().write(temp);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Given Path does not yield a resource\", \"path\":\"" + sourcePath + "\"}");
        }
    }
}
