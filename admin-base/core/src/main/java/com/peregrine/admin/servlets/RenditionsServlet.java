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

import com.peregrine.adaption.PerAsset;
import com.peregrine.admin.resource.ResourceManagement;
import com.peregrine.admin.resource.ResourceManagement.ManagementException;
import com.peregrine.admin.transform.ImageContext;
import com.peregrine.admin.transform.ImageTransformationConfigurationProvider;
import com.peregrine.admin.transform.ImageTransformationProvider;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Rendition Provider Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "per/Asset"
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides renditions of Peregrine Assets (per:Asset)
 * and creates them if they are not available yet
 *
 * Drag an image to the asset page: http://localhost:8080/content/admin/assets.html/path///content/assets
 * Create a thumbnail image with: curl -u admin:admin -X POST http://localhost:8080/content/assets/test.png.rendition.json/thunbnail
 */
public class RenditionsServlet extends AbstractBaseServlet {

    public static final String ETC_FELIBS_ADMIN_IMAGES_BROKEN_IMAGE_SVG = "/etc/felibs/admin/images/broken-image.svg";
    
    @Reference
    private ImageTransformationConfigurationProvider imageTransformationConfigurationProvider;
    @Reference
    private ImageTransformationProvider imageTransformationProvider;
    @Reference
    MimeTypeService mimeTypeService;
    @Reference
    ResourceManagement resourceManagement;


    @Override
    protected Response handleRequest(Request request) throws IOException {
        Response answer = null;
        Resource resource = request.getResource();
        PerAsset asset = resource.adaptTo(PerAsset.class);
        if(asset == null) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Given Resource: '" + resource.getPath() + "' is not an Asset");
        }
        if(!asset.isValid()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Given Resource: '" + resource.getPath() + "' is not an valid Asset");
        }
        // Check if there is a suffix
        String suffix = request.getSuffix();
        String sourceMimeType = asset.getContentProperty(JCR_MIME_TYPE, String.class);
        if(sourceMimeType == null || sourceMimeType.isEmpty()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Given Resource has no Mime Type");
        }
        if(suffix != null && suffix.length() > 0) {
            // Get final Rendition Name and Mime Type
            String renditionName = suffix;
            int index = renditionName.indexOf('/');
            if(index >= 0) {
                renditionName = renditionName.substring(index + 1);
            }
            ImageContext imageContext = null;
            try {
                imageContext = resourceManagement.createRendition(resource, renditionName, sourceMimeType);
                request.getResourceResolver().commit();
            } catch(ManagementException e) {
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(e.getMessage()).setException(e);
            }
            if(imageContext != null) {
                // Got a output stream -> send it back
                answer = new StreamResponse(imageContext.getTargetMimeType(), imageContext.getImageStream());
            } else {
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to load or transform the broken image");
            }
        } else {
            // This was not a request for a rendition but just the original asset -> load and send it back
            InputStream sourceStream = asset.getRenditionStream((Resource) null);
            if(sourceStream != null) {
                answer = new StreamResponse(sourceMimeType, sourceStream);
            } else {
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("No Data Stream found for requested resource");
            }
        }
        return answer;
    }

    public static class StreamResponse extends Response {

        private String mimeType;
        private InputStream inputStream;

        public StreamResponse(String mimeType, InputStream stream) {
            super("stream");
            this.mimeType = mimeType;
            this.inputStream = stream;
        }

        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            try {
                IOUtils.copy(inputStream, outputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }
}

