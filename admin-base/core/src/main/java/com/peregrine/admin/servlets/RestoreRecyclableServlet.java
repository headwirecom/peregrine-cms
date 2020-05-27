package com.peregrine.admin.servlets;

/*-
 * #%L
 * admin base - Core
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
 * Contributed by Cris Rockwell, University of Michigan
 */


import com.peregrine.admin.models.Recyclable;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_RECYCLABLES;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_RESTORE_RECYCLABLE;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;

/**
 * Restore an items given a path to a @link Recyclable
 *
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Restore Recyclable Item",
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_RESTORE_RECYCLABLE
    }
)
@SuppressWarnings("serial")
public class RestoreRecyclableServlet extends AbstractBaseServlet {

    public static final String FAILED_TO_RESTORE_RECYCLABLE = "Failed restore recyclable item :-/ ";
    public static final String FAILED_TO_RESTORE_RECYCLABLE_2 = "Cannot restore an item that already exists.";
    public static final String FAILED_TO_RESTORE_RECYCLABLE_3 = "Could write an update to the recyclebin. ACL error.";
    public static final String FAILED_TO_RESTORE_RECYCLABLE_4 = "Path above this item does not exist. Restore or create the parent folders or pages first.";
    public static final int SC_CONFLICT = 409;
    public static final int SC_FORBIDDEN = 403;

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final String path = request.getParameter(PATH);
        try {
            final Recyclable recyclable = request.getResourceResolver().getResource(path).adaptTo(Recyclable.class);
            resourceManagement.recycleDeleted(request.getResourceResolver(), recyclable, true);
        } catch (PathNotFoundException pe) {
            return new ErrorResponse().setHttpErrorCode(SC_CONFLICT)
                    .setErrorMessage(FAILED_TO_RESTORE_RECYCLABLE_4)
                    .setRequestPath(path)
                    .setException(pe);
        } catch (RepositoryException e) {
            return new ErrorResponse().setHttpErrorCode(SC_CONFLICT)
                    .setErrorMessage(FAILED_TO_RESTORE_RECYCLABLE_2)
                    .setRequestPath(path)
                    .setException(e);
        } catch (PersistenceException e) {
            return new ErrorResponse().setHttpErrorCode(SC_FORBIDDEN)
                .setErrorMessage(FAILED_TO_RESTORE_RECYCLABLE_3 )
                .setRequestPath(path)
                .setException(e);
        } catch (AdminResourceHandler.ManagementException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(FAILED_TO_RESTORE_RECYCLABLE )
                .setRequestPath(path)
                .setException(e);
        }

        JsonResponse answer = new JsonResponse();

        return answer;
    }
}

