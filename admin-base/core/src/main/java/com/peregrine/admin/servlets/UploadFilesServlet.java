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

import com.peregrine.admin.resource.ResourceManagement;
import com.peregrine.admin.resource.ResourceManagement.ManagementException;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Upload Files servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=api/admin/uploadFiles",
    }
)
@SuppressWarnings("serial")
public class UploadFilesServlet extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;
    @Reference
    ResourceManagement resourceManagement;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String path = request.getParameter("path");
        try {
            Resource resource = request.getResourceByPath(path);
            logger.debug("Upload files to resource: '{}'", resource);
            List<Resource> assets = new ArrayList<>();
            for (Part part : request.getParts()) {
                String assetName = part.getName();
                String contentType = part.getContentType();
                logger.debug("part type {}",contentType);
                logger.debug("part name {}",assetName);
                Resource asset = resourceManagement.createAssetFromStream(resource, assetName, contentType, part.getInputStream());
                assets.add(asset);
            }
            resource.getResourceResolver().commit();
            logger.debug("Upload Done successfully and saved");
            JsonResponse answer = new JsonResponse()
                .writeAttribute("resourceName", resource.getName())
                .writeAttribute("resourcePath", resource.getPath())
                .writeArray("assets");
            for(Resource asset: assets) {
                answer.writeObject();
                answer.writeAttribute("assetName", asset.getName());
                answer.writeAttribute("assetPath", asset.getPath());
                answer.writeClose();
            }
            return answer;
        } catch(ManagementException e) {
            logger.debug("Upload Failed", e);
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(e.getMessage()).setRequestPath(path).setException(e);
        } catch(ServletException e) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Upload Failed because of Servlet Parts Problem").setRequestPath(path).setException(e);
        }
    }

}

