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

import com.peregrine.replication.PerReplicable;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_REPLICATION_STATUS;
import static com.peregrine.admin.servlets.NodesServlet.ACTIVATED;
import static com.peregrine.admin.servlets.NodesServlet.DATE_FORMATTER;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static java.util.Objects.isNull;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;

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
        final JsonResponse answer = new JsonResponse();
        final String suffix = request.getSuffix();
        final PerReplicable replicable = Optional.ofNullable(suffix)
                .map(request::getResourceByPath)
                .map(r ->  r.adaptTo(PerReplicable.class))
                .orElse(null);
        if(isNull(replicable)) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(RESOURCE_NOT_FOUND)
                .setRequestPath(suffix);
        }

        answer.writeAttribute(SOURCE_NAME, replicable.getName());
        answer.writeAttribute(SOURCE_PATH, replicable.getPath());
        answer.writeAttribute(ACTIVATED, replicable.isReplicated());
        answer.writeAttribute(PER_REPLICATION_REF, replicable.getReplicationRef());
        answer.writeAttribute(PER_REPLICATED, Optional.ofNullable(replicable.getReplicated())
                .map(Calendar::getTime)
                .map(Date::getTime)
                .map(DATE_FORMATTER::format)
                .orElse(null)
        );
        answer.writeAttribute(PER_REPLICATION_LAST_ACTION, replicable.getLastReplicationAction());
        answer.writeClose();
        return answer;
    }
}

