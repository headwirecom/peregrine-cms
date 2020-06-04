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

import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_UPLOAD_BACKUP_TENANT;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet uploads a backup a Tenant Site
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 *
 * It is invoked like this:
 *      curl -X GET "http://localhost:8080/perapi/admin/uploadBackupTenant.json/<tenant name>"
 */

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Upload Tenant Backup",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_UPLOAD_BACKUP_TENANT
    }
)
@SuppressWarnings("serial")
public class UploadBackupTenantServlet extends AbstractPackageServlet {

    private static final String RESOURCE_NAME = "resourceName";
    private static final String RESOURCE_PATH = "resourcePath";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PACKAGE_PATH = "packagePath";

    public static final String PARAM_FILE = "file";
    public static final String PARAM_FORCE = "force";

    @Reference
    private Packaging packaging;

    @Override
    Packaging getPackaging() { return packaging; }

    JobManager getJobManager() { return null; }

    @Override
    protected Response handleRequest(Request request) throws IOException {
        try {
            RequestParameterMap parameters = request.getRequest().getRequestParameterMap();

            RequestParameter file = parameters.getValue(PARAM_FILE);
            if (file != null) {
                InputStream input = file.getInputStream();
                boolean force = request.getBooleanParameter(PARAM_FORCE, false);

                JcrPackageManager manager = getPackageManager(request);
                JcrPackage jcrPackage = manager.upload(input, force);

                logger.info("Upload Done successfully and saved");
                JsonResponse answer = new JsonResponse()
                    .writeAttribute(RESOURCE_NAME, request.getResource().getName())
                    .writeAttribute(RESOURCE_PATH, request.getResource().getPath())
                    .writeArray("packages");
                answer.writeObject();
                answer.writeAttribute(PACKAGE_NAME, jcrPackage.getPackage().getId().toString());
                answer.writeAttribute(PACKAGE_PATH, jcrPackage.getNode().getPath());
                answer.writeClose();
                return answer;
            } else {
                logger.info("No Package File provided");
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage("No Package File provided")
                    .setRequestPath(request.getRequestPath());
            }
        } catch(RepositoryException e) {
            logger.info("Upload Failed", e);
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(e.getMessage())
                .setRequestPath(request.getRequestPath())
                .setException(e);
        }
    }
}
