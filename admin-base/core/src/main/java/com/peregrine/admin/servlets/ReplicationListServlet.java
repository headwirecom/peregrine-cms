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
import com.peregrine.replication.Replication;
import com.peregrine.replication.ReplicationsContainerWithDefault;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_REPLICATION;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerUtil.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet lists all available replication services
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 *
 * It is invoked like this:
 *      curl -X GET "http://localhost:8080/perapi/admin/listRepl.json" -H  "accept: application/json"
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Replication Lister Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_LIST_REPLICATION
    }
)
@SuppressWarnings("serial")
public class ReplicationListServlet extends AbstractBaseServlet {

    public static final String REPLICATION_SERVICES = "replicationServices";
    public static final String DESCRIPTION = "description";

    @Reference
    private ReplicationsContainerWithDefault replications;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        final JsonResponse answer;
        answer = new JsonResponse();
        answer.writeArray(REPLICATION_SERVICES);
        for(final Replication replication: replications.getAll()) {
            answer.writeObject();
            answer.writeAttribute(NAME, replication.getName());
            answer.writeAttribute(DESCRIPTION, replication.getDescription());
            answer.writeClose();
        }

        answer.writeClose();
        return answer;
    }
}

