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
import com.peregrine.replication.ReferenceLister;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.List;

import static com.peregrine.admin.servlets.AdminPaths.JSON_EXTENSION;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_REF;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.GET;
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
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Reference Lister Servlet",
        SERVICE_VENDOR + EQUAL + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUAL + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUAL + RESOURCE_TYPE_REF,
        SLING_SERVLET_SELECTORS + EQUAL + JSON_EXTENSION
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides a list of that are referenced by the given
 * resource (to which resources does the given resources points to)
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definintions/admin.yaml
 */
public class ReferenceListerServlet extends AbstractBaseServlet {

    public static final String GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE = "Given Path does not yield a resource";
    public static final String REFERENCES = "references";

    @Reference
    private ReferenceLister referenceLister;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String sourcePath = request.getParameter("path");
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            List<Resource> references = referenceLister.getReferenceList(true, source, true);
            JsonResponse answer = new JsonResponse();
            answer.writeAttribute(SOURCE_NAME, source.getName());
            answer.writeAttribute(SOURCE_PATH, source.getPath());
            answer.writeArray(REFERENCES);
            for(Resource child : references) {
                answer.writeObject();
                answer.writeAttribute(NAME, child.getName());
                answer.writeAttribute(PATH, child.getPath());
                answer.writeClose();
            }
            answer.writeClose();
            return answer;
        } else {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE).setRequestPath(sourcePath);
        }
    }
}

