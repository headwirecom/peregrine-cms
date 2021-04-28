package com.peregrine.admin.servlets;

/*-
 * #%L
 * admin base - Core
 *
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

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.NodeNameValidation;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.jcr.query.Query;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_USER_HOMEPAGE;
import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.commons.util.PerUtil.*;
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
@Designate(ocd = UserHomepageServlet.Configuration.class)
@SuppressWarnings("serial")
public class UserHomepageServlet extends UserPreferencesServlet {

    @Reference
    private NodeNameValidation nodeNameValidation;
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    @Reference
    private AdminResourceHandler resourceManagement;

    public static final String URI_CANDIDATE_PARAM = "tildaPageUri";
    private static final String SQL2_STATEMENT = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([/home/users]) and s.tildaPageUri='%s'";
    private static final String FAILED_TO_GET_SERVICE_RESOLVER = "Unable to get Peregrine Service Resolver";

    @ObjectClassDefinition(
        name = "Peregrine: User ~ Page Servlet",
        description = "Validates and Initializes User ~Page URI Candidates"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Template for User ~Page",
            description = "Specify the template path to be used when initializing new user ~page's",
            required = true
        )
        String userTildaPageTemplatePath() default "/content/pagerenderserver/templates/empty-container";
    }

    /**
     *
     * @param request
     * @return
     * @throws IOException
     */
    @Override
    protected Response handleRequest(Request request) throws IOException {
        String nameToCheck = request.getParameter(URI_CANDIDATE_PARAM);
        if (Objects.isNull(nameToCheck)) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST);
        }
        JsonResponse answer = new JsonResponse();
        // get uri candidate param, and check whether valid
        boolean candidateNameValid = uriValid(nameToCheck);
        answer.writeAttribute("name", nameToCheck);
        answer.writeAttribute("nameValid", candidateNameValid);
        if (candidateNameValid) {
            // check availability for valid names
            try {
                answer.writeAttribute("nameAvailable", uriAvailable(request, nameToCheck));
            } catch (LoginException e) {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_GET_SERVICE_RESOLVER)
                    .setException(e);
            }
        } else {
            answer.writeAttribute("nameAvailable", false);
        }
        if (request.isPost()) {
            // confirm again uri candidate is available
            // create or update user page node under user's home

        }


        return answer;
    }

    boolean uriValid(String name){
        return nodeNameValidation.isValidUserHomepageName(name);
    }

    boolean uriAvailable(Request request, String name) throws LoginException {
        ResourceResolver serviceResolver = getServiceResolver(request);
        Iterator<Resource> usersWithName = serviceResolver.findResources(String.format(SQL2_STATEMENT, name), Query.JCR_SQL2);
        return !usersWithName.hasNext();
    }

    ResourceResolver getServiceResolver(Request request) throws LoginException {
        return request.isAdmin() ?
            request.getResourceResolver() :
            loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME);
    }
//Resource getUserHome(final Request request)
//UserManager getUserManager(final Request request)

}

