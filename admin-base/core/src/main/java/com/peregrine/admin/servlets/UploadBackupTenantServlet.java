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

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_UPLOAD_BACKUP_TENANT;
import static com.peregrine.commons.util.PerConstants.PATH;
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
public class UploadBackupTenantServlet extends AbstractBaseServlet {

    private static final String PACKAGE_PATH = "/bin/cpm/package";
    private static final String DOWNLOAD_SELECTOR = "download";

    private static final String RESOURCE_NAME = "resourceName";
    private static final String RESOURCE_PATH = "resourcePath";
    private static final String ASSET_NAME = "assetName";
    private static final String ASSET_PATH = "assetPath";
    private static final String UPLOAD_FAILED_BECAUSE_OF_SERVLET_PARTS_PROBLEM = "Upload Failed because of Servlet Parts Problem";

    @Reference
    AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String characterEncoding = request.getRequest().getCharacterEncoding();
        logger.debug("Current Character Encoding: '{}'", characterEncoding);
        String path = request.getParameter(PATH);
        try {
            Resource resource = request.getResourceByPath(path);
            logger.debug("Upload files to resource: '{}'", resource);
            List<Resource> assets = new ArrayList<>();
            for (Part part : request.getParts()) {
                String assetName = part.getName();
                if(!characterEncoding.equalsIgnoreCase(StandardCharsets.UTF_8.toString())) {
                    String originalName = assetName;
                    assetName = new String(originalName.getBytes (characterEncoding), StandardCharsets.UTF_8);
                    logger.debug("Asset Name, original: '{}', converted: '{}'", originalName, assetName);
                }
                String contentType = part.getContentType();
                logger.debug("part type {}",contentType);
                logger.debug("part name {}",assetName);
                //TODO: figure out the data send by the multiparts and then create a method to store the backups
//                Resource asset = resourceManagement.
//                    createAssetFromStream(resource, assetName, contentType, part.getInputStream());
//                assets.add(asset);
            }
            resource.getResourceResolver().commit();
            logger.debug("Upload Done successfully and saved");
            JsonResponse answer = new JsonResponse()
                .writeAttribute(RESOURCE_NAME, resource.getName())
                .writeAttribute(RESOURCE_PATH, resource.getPath())
                .writeArray("assets");
            for(Resource asset : assets) {
                answer.writeObject();
                answer.writeAttribute(ASSET_NAME, asset.getName());
                answer.writeAttribute(ASSET_PATH, asset.getPath());
                answer.writeClose();
            }
            return answer;
//        } catch(ManagementException e) {
//            logger.debug("Upload Failed", e);
//            return new ErrorResponse()
//                .setHttpErrorCode(SC_BAD_REQUEST)
//                .setErrorMessage(e.getMessage())
//                .setRequestPath(path)
//                .setException(e);
        } catch(ServletException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(UPLOAD_FAILED_BECAUSE_OF_SERVLET_PARTS_PROBLEM)
                .setRequestPath(path)
                .setException(e);
        }
    }
}
