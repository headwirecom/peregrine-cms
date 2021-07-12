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
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.*;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.*;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_USER_HOMEPAGE;
import static com.peregrine.admin.servlets.UserPreferencesServlet.PREFERENCES;
import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.commons.util.PerConstants.*;
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
public class UserHomepageServlet extends AbstractBaseServlet {

    @Reference
    private NodeNameValidation nodeNameValidation;
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    @Reference
    private AdminResourceHandler resourceManagement;
    private ResourceResolver serviceResolver;
    private Map<String,String> userParamMap;
    private static String templatePath;
    private static String profileComponentPath;
    public static final String PROFILE = "profile";
    public static final String USER_GIVEN = "firstName";
    public static final String USER_FAMILY = "lastName";
    public static final String URI_CANDIDATE_PARAM = "tildaPageUri";
    public static final String USER_ORG = "organization";
    public static final String USER_TITLE = "positionTitle";
    public static final String USER_PRONOUNS = "pronouns";
    public static final String TILDA_PAGE = "tildaPage";
    private static final String SQL2_STATEMENT = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([/home/users]) and s.tildaPageUri='%s'";
    private static final String FAILED_TO_GET_SERVICE_RESOLVER = "Unable to get Peregrine Service Resolver";
    private static final String USER_PAGE_EXISTS = "User profile already exists.";
    private static final String MGT_ERROR = "A data management error occurred. user page was not created";

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
        String userProfileComponentPath() default "pagerenderserver/components/profile";
    }

    /**
     *
     * @param request
     * @return
     * @throws IOException
     */
    @Override
    protected Response handleRequest(Request request) throws IOException {
        String content = request.getParameter(CONTENT);
        userParamMap = convertToMap(content);
        String nameToCheck = request.getParameter(URI_CANDIDATE_PARAM);

        if (Objects.isNull(nameToCheck)) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST);
        }
        JsonResponse answer = new JsonResponse();
        // get uri candidate param, and check whether valid
        boolean candidateNameValid = uriValid(nameToCheck);
        boolean uriAvailable = false;
        answer.writeAttribute("name", nameToCheck);
        answer.writeAttribute("nameValid", candidateNameValid);

        try {
            if (candidateNameValid) {
                // check availability for valid names
                uriAvailable = checkUriAvailability(request, nameToCheck);
                answer.writeAttribute("nameAvailable", uriAvailable);
            } else {
                answer.writeAttribute("nameAvailable", false);
            }
            if (request.isPost() && candidateNameValid && uriAvailable) {
                if (userProfileExists(request)){
                    closeServiceResolver();
                    return new ErrorResponse()
                        .setHttpErrorCode(SC_BAD_REQUEST)
                        .setErrorMessage(USER_PAGE_EXISTS);
                } else if (hasRequiredProps()){
                    // create or update user page node under user's home
                    createUserPage(request, answer);
                }
            }
        } catch (LoginException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_GET_SERVICE_RESOLVER)
                .setException(e);
        } catch (AdminResourceHandler.ManagementException | RepositoryException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(MGT_ERROR)
                .setException(e);
        } finally {
            closeServiceResolver();
        }

        return answer;
    }

    boolean uriValid(String name){
        return nodeNameValidation.isValidUserHomepageName(name);
    }

    boolean checkUriAvailability(Request request, String name) throws LoginException {
        Iterator<Resource> usersWithName = getServiceResolver(request)
            .findResources(String.format(SQL2_STATEMENT, name), Query.JCR_SQL2);
        return !usersWithName.hasNext();
    }

    boolean hasRequiredProps(){
       return Objects.nonNull(userParamMap.get(USER_GIVEN))
           && Objects.nonNull(userParamMap.get(USER_FAMILY))
           && Objects.nonNull(userParamMap.get(URI_CANDIDATE_PARAM));
    }

    ResourceResolver getServiceResolver(Request request) throws LoginException {
        if (Objects.nonNull(serviceResolver) && serviceResolver.isLive()){
            return serviceResolver;
        }
        serviceResolver = request.isAdmin() ?
                request.getResourceResolver() :
                loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME);
        return serviceResolver;
    }

    void closeServiceResolver(){
        if (Objects.nonNull(serviceResolver) && serviceResolver.getUserID().equals(PEREGRINE_SERVICE_NAME)){
            serviceResolver.close();
        }
    }

    Resource createUserPage(Request request, JsonResponse response) throws LoginException, AdminResourceHandler.ManagementException, IOException, RepositoryException {
        final Resource userHome = getUserHome(request);
        Resource userPreferences = userHome.getChild(PREFERENCES);
        String userpageTitle = userParamMap.get(USER_GIVEN)+" "+userParamMap.get(USER_FAMILY);
        final Resource userPage = resourceManagement.createPage(
            request.getResourceResolver(),
            userHome.getPath(),
            PROFILE,
            templatePath,
            userpageTitle);
        response.writeAttribute(USER_GIVEN, request.getParameter(USER_GIVEN));
        response.writeAttribute(USER_FAMILY, request.getParameter(USER_FAMILY));
        response.writeAttribute(URI_CANDIDATE_PARAM, request.getParameter(URI_CANDIDATE_PARAM));
        if (Objects.nonNull(request.getParameter(USER_ORG))){
            response.writeAttribute(USER_ORG, request.getParameter(USER_ORG));
        }
        if (Objects.nonNull(request.getParameter(USER_TITLE))){
            response.writeAttribute(USER_TITLE, request.getParameter(USER_TITLE));
        }
        if (Objects.nonNull(request.getParameter(USER_PRONOUNS))){
            response.writeAttribute(USER_PRONOUNS, request.getParameter(USER_PRONOUNS));
        }

        Resource jcrContent = userPage.getChild(JCR_CONTENT);
        Node content = jcrContent.adaptTo(Node.class).addNode(CONTENT);
        Node profileNode = content.addNode(PROFILE);
        profileNode.setProperty(SLING_RESOURCE_TYPE, profileComponentPath);
        resourceManagement.updateResource(
            request.getResourceResolver(),
            profileNode.getPath(),
            request.getParameter(CONTENT));

        userPreferences.adaptTo(ModifiableValueMap.class).put(TILDA_PAGE, userPage.getPath());
        request.getResourceResolver().commit();
        return userPage;
    }

    boolean userProfileExists(Request request){
        Resource userHome = getUserHome(request);
        return Objects.nonNull(userHome) && Objects.nonNull(userHome.getChild(PROFILE));
    }

    private Resource getUserHome(final Request request) {
        final ResourceResolver resolver = request.getResourceResolver();
        Resource resource = null;
        try {
            Authorizable authorizable = getUserManager(request).getAuthorizable(request.getRequest().getUserPrincipal());
            resource = resolver.getResource(authorizable.getPath());
        } catch (Exception e) {
            logger.error("Error getting user's home", e);
        }
        return resource;
    }

    private UserManager getUserManager(Request request) {
        UserManager userManager = null;
        try {

            Session adminSession = getServiceResolver(request).adaptTo(Session.class);
            userManager = AccessControlUtil.getUserManager(adminSession);

        } catch (Exception e) {
            logger.error("Error getting UserManager", e);
        }
        return userManager;
    }

    @Activate
    @Modified
    void setup(UserHomepageServlet.Configuration config){
        templatePath = config.userTildaPageTemplatePath();
        profileComponentPath = config.userProfileComponentPath();
    }
}

