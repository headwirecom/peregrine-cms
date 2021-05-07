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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_REF_BY;
import static com.peregrine.admin.servlets.ReferenceServletUtils.addBasicProps;
import static com.peregrine.admin.servlets.ReferenceServletUtils.addBasicSourceProps;
import static com.peregrine.admin.servlets.ReferenceServletUtils.addReplicationProps;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.reference.ReferenceLister;
import java.io.IOException;
import java.util.List;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides a list of references that have a reference to the
 * given resource (which resources point to the given resources)
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Referenced By Lister Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_REF_BY,
        SLING_SERVLET_SELECTORS + EQUALS + JSON
    }
)
@SuppressWarnings("serial")
public class ReferencedByListerServlet extends AbstractBaseServlet {

    public static final String REFERENCED_BY = "referencedBy";
    public static final String PROPERTY_NAME = "propertyName";
    public static final String PROPERTY_PATH = "propertyPath";
    public static final String GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE = "Given Path does not yield a resource";

    @Reference
    private ReferenceLister referenceLister;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String sourcePath = request.getParameter(PATH);
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            List<com.peregrine.reference.Reference> references = referenceLister.getReferencedByList(source);
            JsonResponse answer = new JsonResponse();
            addBasicSourceProps(source, answer);
            addReplicationProps(source, answer);
            answer.writeArray(REFERENCED_BY);
            for(com.peregrine.reference.Reference reference : references) {
                answer.writeObject();
                final Resource refResource = reference.getResource();
                addBasicProps(refResource, answer);
                addReplicationProps(refResource, answer);
                answer.writeAttribute(PROPERTY_NAME, reference.getPropertyName());
                answer.writeAttribute(PROPERTY_PATH, reference.getPropertyResource().getPath());
                answer.writeClose();
            }

            return answer;
        } else {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE)
                .setRequestPath(sourcePath);
        }
    }
}
