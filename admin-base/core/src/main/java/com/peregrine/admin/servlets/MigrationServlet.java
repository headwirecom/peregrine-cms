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

import com.peregrine.admin.migration.Migration;
import com.peregrine.admin.migration.MigrationDescriptor;
import com.peregrine.admin.migration.MigrationResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static com.peregrine.admin.util.AdminConstants.MIGRATION_PATH;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Servlet to provide Update Resources support
 *
 * These are the rules:
 * 1) The properties / node structure is provided as JSon on the parameter: 'content'
 * 2) The node structure is a JSon Map where each property is a property of the node
 *    and each Map is a sub node
 * 3) Sub nodes can be deleted by:
 *    a) providing a property on the node called 'delete'
 *       and either be null or 'true'
 *    b) providing a property on the parent called 'delete' and providing the name of the child
 *       node. If found it will be deleted
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definintions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Migration servlet",
        SERVICE_VENDOR + EQUAL + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUAL + GET,
        SLING_SERVLET_METHODS + EQUAL + POST,
        SLING_SERVLET_PATHS + EQUAL + MIGRATION_PATH
    }
)
@SuppressWarnings("serial")
public class MigrationServlet extends AbstractBaseServlet {

    public static final String FAILED_TO_UPDATE_PAGE = "Failed to Update Page";

    @Reference
    private transient Migration migrationService;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        JsonResponse answer;
        if(request.isGet()) {
            List<MigrationDescriptor> migrationDescriptorList = migrationService.getMigrationActions();
            answer = new JsonResponse()
                .writeArray("migrations");
            for(MigrationDescriptor descriptor: migrationDescriptorList) {
                answer
                    .writeObject()
                    .writeAttribute("name", descriptor.getName())
                    .writeAttribute("description", descriptor.getDescription())
                    .writeAttribute("lastUpdated", descriptor.getLastUpdated())
                    .writeClose();
            }
            answer.writeCloseAll();
        } else if(request.isPost()) {
            String migrationName = request.getParameter("migrationName");
            MigrationResponse migrationResponse = migrationService.execute(migrationName, request.getResourceResolver());
            if(migrationResponse.getCode() >= 0) {
                answer = new JsonResponse()
                    .writeAttribute("name", migrationResponse.getMigrationDescriptor().getName())
                    .writeAttribute("code", migrationResponse.getCode())
                    .writeAttribute("message", migrationResponse.getMessage());
            } else {
                answer = new JsonResponse()
                    .writeAttribute("code", migrationResponse.getCode())
                    .writeAttribute("message", migrationResponse.getMessage());
                Throwable exception = migrationResponse.getException();
                if(exception != null) {
                    StringWriter writer = new StringWriter();
                    PrintWriter pw = new PrintWriter(writer);
                    exception.printStackTrace(pw);
                    answer.writeAttribute("exception", writer.toString());
                }
            }
        } else {
            answer = new ErrorResponse().setErrorCode(SC_BAD_REQUEST).setErrorMessage("Request Method: '" + request.getMethod() + "' is not supported");
        }
        return answer;
    }
}

