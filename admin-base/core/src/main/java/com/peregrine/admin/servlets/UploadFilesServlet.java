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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_UPLOAD_FILES;
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

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Uploads one or more files as assets to Peregrine
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 *
 * To Upload a File with CURL you have to do the following:
 *
 * curl -i -u admin:admin \
 *    -F"test3.jpg=@./testme.jpg;type=image/jpeg" \
 *    "http://localhost:8080/perapi/admin/uploadFiles.json/content/test/assets"
 *
 * 'test3.jpg' is the name of the asset under the given asset path in the URL,
 * './testme.jpg' is the relative or absolute path to the file to be uploaded
 * 'type=image/jpeg' defines the image content type which must be provided
 * '/content/test/assets' is the path to the resource that will contain the resource
 *
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Upload Files servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_UPLOAD_FILES,
    }
)
@SuppressWarnings("serial")
public class UploadFilesServlet extends AbstractBaseServlet {

    private static final String RESOURCE_NAME = "resourceName";
    private static final String RESOURCE_PATH = "resourcePath";
    private static final String ASSET_NAME = "assetName";
    private static final String ASSET_PATH = "assetPath";
    private static final String UPLOAD_FAILED_BECAUSE_OF_SERVLET_PARTS_PROBLEM = "Upload Failed because of Servlet Parts Problem";

    @Reference
    ModelFactory modelFactory;

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
                Resource asset = resourceManagement.
                    createAssetFromStream(resource, assetName, contentType, part.getInputStream());
                assets.add(asset);
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
        } catch(ManagementException e) {
            logger.debug("Upload Failed", e);
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(e.getMessage())
                .setRequestPath(path)
                .setException(e);
        } catch(ServletException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(UPLOAD_FAILED_BECAUSE_OF_SERVLET_PARTS_PROBLEM)
                .setRequestPath(path)
                .setException(e);
        }
    }
}

