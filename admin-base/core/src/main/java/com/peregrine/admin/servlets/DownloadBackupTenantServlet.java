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
import com.peregrine.intra.IntraSlingCaller;
import com.peregrine.intra.IntraSlingCaller.CallException;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.OutputStream;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_DOWNLOAD_BACKUP_TENANT;
import static com.peregrine.admin.util.AdminConstants.DOWNLOAD_SELECTOR;
import static com.peregrine.admin.util.AdminConstants.PACKAGE_FORMAT;
import static com.peregrine.admin.util.AdminConstants.PACKAGE_PATH;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.ZIP;
import static com.peregrine.commons.util.PerConstants.ZIP_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.extractName;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
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
public class DownloadBackupTenantServlet extends AbstractBaseServlet {


    @Reference
    @SuppressWarnings("unused")
    private IntraSlingCaller intraSlingCaller;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        try {
            ResourceResolver resourceResolver = request.getResourceResolver();
            String tenantPath = request.getParameter(PATH);
            String tenantName = extractName(tenantPath);
            if(request.isGet()) {
                //TODO: shall we check against the Audit Job to verify the requested download ?
                byte[] response = intraSlingCaller.call(
                    intraSlingCaller.createContext()
                        .setResourceResolver(resourceResolver)
                        .setMethod(METHOD_GET)
                        .setPath(PACKAGE_PATH)
                        .setSelectors(DOWNLOAD_SELECTOR)
                        .setExtension(ZIP)
                        .setSuffix(SLASH + String.format(PACKAGE_FORMAT, tenantName))
                );
                if (response.length > 0) {
                    return new ZipResponse(response);
                } else {
                    return new ErrorResponse().setErrorMessage("No ZIP Content returned for tenant: " + tenantName);
                }
            } else {
                return new ErrorResponse().setErrorMessage("Unsupported Request for: " + tenantName);
            }
        } catch (CallException e) {
            logger.warn("Download Failure", e);
            return new ErrorResponse().setErrorMessage("Download Failed").setException(e);
        }
    }

    private class ZipResponse extends Response {

        private byte[] data;

        public ZipResponse(byte[] data) {
            super("ZIP");
            this.data = data;
        }

        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(data);
        }

        @Override
        public String getMimeType() {
            return ZIP_MIME_TYPE;
        }
    }
}
