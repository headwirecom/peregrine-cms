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
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.List;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Referenced By Lister Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "GET",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/refBy",
        SLING_SERVLET_SELECTORS + EQUALS + "json"
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides a list of references of a given page
 */
public class ReferencedByListerServlet extends AbstractBaseServlet {

    @Reference
    private ReferenceLister referenceLister;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String sourcePath = request.getParameter ("path");
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            List<com.peregrine.admin.replication.Reference> references = referenceLister.getReferencedByList(source);
            JsonResponse answer = new JsonResponse();
            answer.writeAttribute("sourceName", source.getName());
            answer.writeAttribute("sourcePath", source.getPath());
            answer.writeArray("referencedBy");
            for(com.peregrine.admin.replication.Reference child : references) {
                answer.writeObject();
                answer.writeAttribute("name", child.getResource().getName());
                answer.writeAttribute("path", child.getResource().getPath());
                answer.writeAttribute("propertyName", child.getPropertyName());
                answer.writeAttribute("propertyPath", child.getPropertyResource().getPath());
                answer.writeClose();
            }
            return answer;
        } else {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Given Path does not yield a resource").setRequestPath(sourcePath);
        }
    }
}
