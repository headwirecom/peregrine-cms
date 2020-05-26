package com.peregrine.rendition;

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

import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.adaption.PerAsset;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.rendition.BaseResourceHandler.HandlerException;
import com.peregrine.transform.ImageContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Rendition Provider Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "per/Asset"
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides renditions of Peregrine Assets (per:Asset)
 * and creates them if they are not available yet
 *
 * Drag an image to the asset page: http://localhost:8080/content/admin/pages/assets.html/path:/content/test/assets
 * Create a thumbnail image with: curl -u admin:admin http://localhost:8080/content/test/assets/test.png.rendition.json/thumbnail.png
 */
public class RenditionsServlet extends AbstractBaseServlet {

    @Reference
    BaseResourceHandler renditionHandler;

    private Servlet redirectServlet;

    @Reference(
        target = "(component.name=org.apache.sling.servlets.get.impl.RedirectServlet)",
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    void bindServlet(Servlet servlet) {
        logger.info("Bind Servlet: '{}', Name: '{}'", servlet, servlet.getClass().getName());
        logger.trace("Bind Servlet: '{}', Name: '{}'", servlet, servlet.getClass().getName());
        if(servlet.getClass().getName().equals("org.apache.sling.servlets.get.impl.RedirectServlet")) {
            redirectServlet = servlet;
            logger.trace("Redirect Servlet: '{}'", redirectServlet);
        }
    }
    void unbindServlet(Servlet servlet) {
        logger.info("Unbind Servlet: '{}'", servlet);
        logger.trace("Unbind Servlet: '{}'", servlet);
        if(servlet.getClass().getName().equals("org.apache.sling.servlets.get.impl.RedirectServlet")) { redirectServlet = null; }
    }

    @Override
    protected Response handleRequest(Request request) throws IOException, ServletException {
        Response answer = null;
        Resource resource = request.getResource();
        PerAsset asset = resource.adaptTo(PerAsset.class);
        if(asset == null) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Given Resource: '" + resource.getPath() + "' is not an Asset");
        }
        if(!asset.isValid()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Given Resource: '" + resource.getPath() + "' is not an valid Asset");
        }
        String selector = request.getSelector();
        // if we have a selector called 'rendition' or the request path is the same as the resource path then we handle them as images
        // otherwise we delegate to Sling
        // TODO: If the path changes because of a Mapping then this will fail loading the image
        String resourceName = resource.getName();
        String requestName = request.getRequestPath();
        requestName = URLDecoder.decode(requestName);
        int index = requestName.lastIndexOf("/");
        if(index >= 0) {
            requestName = requestName.substring(index + 1);
        }
        if(!"rendition".equals(selector) && !resourceName.equals(requestName)) {
            logger.trace("Redirect as this is not an rendition (selector: '{}') or resource name: '{}' odes not match request: '{}'", selector, resourceName, requestName);
            redirectServlet.service(request.getRequest(), request.getResponse());
            return new ResponseHandledResponse();
        }
        String extension = request.getExtension();
        // Check if there is a suffix
        String suffix = request.getSuffix();
        logger.trace("Rendition Call, Selector: '{}', Extension: '{}', Suffix: '{}'", selector, extension, suffix);
        String sourceMimeType = asset.getContentProperty(JCR_MIME_TYPE, String.class);
        if(sourceMimeType == null || sourceMimeType.isEmpty()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Given Resource has no Mime Type");
        }
        if(suffix != null && suffix.length() > 0) {
            // Get final Rendition Name and Mime Type
            String renditionName = suffix;
            index = renditionName.indexOf('/');
            if(index >= 0) {
                renditionName = renditionName.substring(index + 1);
            }
            ImageContext imageContext = null;
            try {
                imageContext = renditionHandler.createRendition(resource, renditionName, sourceMimeType);
                request.getResourceResolver().commit();
            } catch(HandlerException e) {
                logger.debug("Create Rendition failed !!", e);
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(e.getMessage()).setException(e);
            }
            if(imageContext != null) {
                // Got a output stream -> send it back
                answer = new StreamResponse(imageContext.getTargetMimeType(), imageContext.getImageStream());
            } else {
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to create a rendition of image: " + asset.getName() + ", rendition: " + renditionName);
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

    /**
     * Class that provides an AbstractBaseServlet Response
     * for an Output Stream
     */
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

