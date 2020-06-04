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

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Binary;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_DOWNLOAD_BACKUP_TENANT;
import static com.peregrine.admin.util.AdminConstants.BACKUP_FOLDER_FORMAT;
import static com.peregrine.admin.util.AdminConstants.BACKUP_FORMAT;
import static com.peregrine.admin.util.AdminConstants.PACKAGE_FORMAT;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.ZIP_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.extractName;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.HttpConstants.HEADER_LAST_MODIFIED;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet downloads a backup a Tenant Site
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 *
 * It is invoked like this:
 *      curl -X GET "http://localhost:8080/perapi/admin/downloadBackupTenant.json/<tenant name>"
 */

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Download Tenant Backup",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_DOWNLOAD_BACKUP_TENANT
    }
)
@SuppressWarnings("serial")
public class DownloadBackupTenantServlet extends AbstractPackageServlet {

    @Reference
    private Packaging packaging;

    @Override
    Packaging getPackaging() {
        return packaging;
    }

    JobManager getJobManager() { return null; }

    @Override
    protected Response handleRequest(Request request) throws IOException {
        try {
            String path = request.getParameter(PATH);
            String tenantName = extractName(path);
            if(tenantName.endsWith(".zip")) {
                tenantName = tenantName.substring(0, tenantName.length() - 4);
            }
            if(request.isGet()) {
                JcrPackageManager manager = getPackageManager(request);
                String tenantPath = String.format(BACKUP_FOLDER_FORMAT, String.format(PACKAGE_FORMAT, tenantName));
                Resource packageResource = request.getResourceByPath(tenantPath);
                JcrPackage jcrPackage = getJcrPackage(manager, packageResource);

                if (jcrPackage != null) {
                    Property data;
                    Binary binary;
                    InputStream stream;
                    if ((data = jcrPackage.getData()) != null &&
                        (binary = data.getBinary()) != null &&
                        (stream = binary.getStream()) != null) {

                        ZipResponse answer = new ZipResponse(stream);
                        answer.addHeader("Content-Disposition", "inline; filename=" + String.format(BACKUP_FORMAT, tenantName));
                        Calendar lastModified = jcrPackage.getDefinition().getLastModified();
                        if (lastModified != null) {
                            answer.addHeader(HEADER_LAST_MODIFIED, lastModified);
                        }
                        answer.addHeader("Cache-Control", "no-cache");
                        answer.addHeader("Cache-Control", "no-store");
                        answer.addHeader("Cache-Control", "must-revalidate");
                        answer.addHeader("Pragma", "no-cache");
                        return answer;
                    } else {
                        logger.info("Could not download package because it is either not a package or has not content: '{}'", tenantPath);
                        return new ErrorResponse()
                            .setHttpErrorCode(SC_BAD_REQUEST)
                            .setErrorMessage("This is either no a package or has not content")
                            .setRequestPath(tenantPath);
                    }

                } else {
                    logger.info("Resource could not be found: '{}'", tenantPath);
                    return new ErrorResponse()
                        .setHttpErrorCode(SC_BAD_REQUEST)
                        .setErrorMessage("Resource could not be found")
                        .setRequestPath(tenantPath);
                }
            } else {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage("Unsupported Request Method: " + request.getRequest().getMethod())
                    .setRequestPath(path);
            }
        } catch(RepositoryException e) {
            logger.info("Download Failed", e);
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(e.getMessage())
                .setRequestPath(request.getRequestPath())
                .setException(e);
        }
    }

    private class ZipResponse extends Response {

        private byte[] data;
        private InputStream stream;

        public ZipResponse(byte[] data) {
            super("ZIP");
            this.data = data;
        }

        public ZipResponse(InputStream stream) {
            super("ZIP");
            this.stream = stream;
        }

        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            if(data != null) {
                outputStream.write(data);
            } else if(stream != null) {
                IOUtils.copy(stream, outputStream);
            }
        }

        @Override
        public String getMimeType() {
            return ZIP_MIME_TYPE;
        }
    }
}
