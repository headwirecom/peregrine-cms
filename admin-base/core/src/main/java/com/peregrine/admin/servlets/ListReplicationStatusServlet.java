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
 * Contributed by Cris Rockwell, University of Michigan
 */

import com.peregrine.adaption.PerReplicable;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_GET_OBJECT;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_REPLICATION_STATUS;
import static com.peregrine.admin.servlets.NodesServlet.ACTIVATED;
import static com.peregrine.admin.servlets.NodesServlet.DATE_FORMATTER;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Provides the Replicable Status in a JSON representation
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Replicable Status Servlet",
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_LIST_REPLICATION_STATUS
    }
)
@SuppressWarnings("serial")
public class ListReplicationStatusServlet extends AbstractBaseServlet {

    private static final String RESOURCE_NOT_FOUND = "Resource not found";



    @Override
    protected Response handleRequest(Request request) throws IOException {

        JsonResponse answer = new JsonResponse();
        final String path = request.getSuffix();
        Resource resource = request.getResourceByPath(path);

        PerReplicable replicable = resource.adaptTo(PerReplicable.class);
        if(resource == null || replicable == null) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(RESOURCE_NOT_FOUND)
                .setRequestPath(path);
        }

        answer.writeAttribute(SOURCE_NAME, replicable.getName());
        answer.writeAttribute(SOURCE_PATH, replicable.getPath());
        answer.writeAttribute(ACTIVATED, replicable.isReplicated());
        answer.writeAttribute(PER_REPLICATION_REF, replicable.getReplicationRef());
        answer.writeAttribute(PER_REPLICATED, DATE_FORMATTER.format(replicable.getReplicated().getTime().getTime()));
        answer.writeAttribute(PER_REPLICATION_LASTACTION, replicable.getLastReplicationAction());
        answer.writeClose();
        return answer;
    }
}

