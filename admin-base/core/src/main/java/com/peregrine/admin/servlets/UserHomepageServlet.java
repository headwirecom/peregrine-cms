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

import com.peregrine.admin.resource.NodeNameValidation;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Objects;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_USER_HOMEPAGE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.POST;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;

/**
 *  GET requests validates whether a homepage URI is available
 *  POST requests creates a homepage for the current user
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "User Homepage Servlet",
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_USER_HOMEPAGE
    }
)
@SuppressWarnings("serial")
public class UserHomepageServlet extends UserPreferencesServlet {

    @Reference
    private NodeNameValidation nodeNameValidation;

    private static final String URI_CANDIDATE_PARAM = "tildaPageUri";
    private static final String xpathStatement = "/jcr:root/home/users//*/profile[@jcr:primaryType='per:Page' and @tildaUri='%s']";

    /**
     *
     * @param request
     * @return
     * @throws IOException
     */
    @Override
    protected Response handleRequest(Request request) throws IOException {
        Resource home = getUserHome(request);
        String nameToCheck = request.getParameter(URI_CANDIDATE_PARAM);
        if (Objects.isNull(nameToCheck)) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST);
        }
        JsonResponse answer = new JsonResponse();
        if (request.isGet() ) {
            // get uri candidate param, and check whether that is available

            answer.writeAttribute("name", nameToCheck);
            answer.writeAttribute("nameValid", uriAvailable(nameToCheck));
        } else if (request.isPost()) {
            // confirm again uri candidate is available
            // create or update user page node under user's home
        }


        return answer;
    }

    boolean uriAvailable(String name){
        return nodeNameValidation.isValidUserHomepageName(name);
    }
//Resource getUserHome(final Request request)
//UserManager getUserManager(final Request request)

}

