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
 */


import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import javax.servlet.Servlet;
import java.io.IOException;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_RECYCLABLES;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_RESTORE_RECYCLABLE;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
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


    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final String path = request.getParameter(PATH);
        JsonResponse answer = new JsonResponse();

        return answer;
    }
}

