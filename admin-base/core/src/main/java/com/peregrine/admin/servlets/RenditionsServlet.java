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
import com.peregrine.admin.transform.ImageTransformationConfiguration;
import com.peregrine.admin.transform.ImageContext;
import com.peregrine.admin.transform.ImageTransformation;
import com.peregrine.admin.transform.ImageTransformation.TransformationException;
import com.peregrine.admin.transform.ImageTransformationConfigurationProvider;
import com.peregrine.admin.transform.ImageTransformationProvider;
import com.peregrine.admin.transform.OperationContext;
import com.peregrine.util.PerConstants;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.util.PerUtil.EQUALS;
import static com.peregrine.util.PerConstants.JCR_MIME_TYPE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + "Peregrine: Rendition Provider Servlet",
        SERVICE_VENDOR + EQUALS + "headwire.com, Inc",
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
    
    private final Logger log = LoggerFactory.getLogger(RenditionsServlet.class);

    @Reference
    private ImageTransformationConfigurationProvider imageTransformationConfigurationProvider;
    @Reference
    private ImageTransformationProvider imageTransformationProvider;
    @Reference
    MimeTypeService mimeTypeService;


    @Override
    Response handleRequest(Request request) throws IOException {
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
            String targetMimeType = mimeTypeService.getMimeType(renditionName);

            List<ImageTransformationConfiguration> imageTransformationConfigurationList =
                imageTransformationConfigurationProvider.getImageTransformationConfigurations(renditionName);
            if(imageTransformationConfigurationList != null) {
                InputStream imageStream = asset.getRenditionStream(renditionName);
                if(imageStream == null) {
                    try {
                        InputStream sourceStream = asset.getRenditionStream((Resource) null);
                        if(sourceStream != null) {
                            ImageContext imageContext = transform(renditionName, sourceMimeType, sourceStream, targetMimeType, imageTransformationConfigurationList, false);
                            asset.addRendition(renditionName, imageContext.getImageStream(), targetMimeType);
                            imageStream = asset.getRenditionStream(renditionName);
                        } else {
                            log.error("Resource: '{}' does not contain a data element", resource.getName());
                        }
                    } catch(TransformationException e) {
                        log.error("Transformation failed, image ignore", e);
                    } catch(RepositoryException e) {
                        log.error("Failed to create Rendition Node for Resource: '{}', rendition name: '{}'", resource, renditionName);
                    }
                }
                if(imageStream == null) {
                    // Rendition was not found and could not be created therefore load and thumbnail the broken image
                    String imagePath = ETC_FELIBS_ADMIN_IMAGES_BROKEN_IMAGE_SVG;
                    Resource brokenImageResource = request.getResourceResolver().getResource(imagePath);
                    if(brokenImageResource != null) {
                        try {
                            InputStream brokenImageStream = getDataStream(brokenImageResource);
                            imageTransformationConfigurationList =
                                imageTransformationConfigurationProvider.getImageTransformationConfigurations("thumbnail.png");
                            ImageContext imageContext = transform(renditionName, "image/svg+xml", brokenImageStream, "image/png", imageTransformationConfigurationList, true);
                            imageStream = imageContext.getImageStream();
                        } catch(TransformationException e) {
                            log.error("Transformation failed, image ignore", e);
                        }
                    }
                }
                if(imageStream != null) {
                    // Got a output stream -> send it back
                    answer = new StreamResponse(targetMimeType, imageStream);
                } else {
                    return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to load or transform the broken image");
                }
            } else {
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("No Rendition Transformation found for: " + renditionName);
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
     * Takes the given source data streams and transforms it into the desired rendition
     * @param renditionName Name of the Rendition (node name)
     * @param sourceMimeType Mime Type of the Source
     * @param sourceStream Data Stream of the Source
     * @param targetMimeType Desired Target Mime Type
     * @param imageTransformationConfigurationList List of Image Transformation Configuration
     * @param noCrop If true then a thumbnail will not crop the image and hence the image might be smaller or shorter
     * @return Image Context that contains the final Data Stream
     * @throws TransformationException If the Transformation failed
     */
    private ImageContext transform(
        String renditionName, String sourceMimeType, InputStream sourceStream, String targetMimeType,
        List<ImageTransformationConfiguration> imageTransformationConfigurationList, boolean noCrop
    )
        throws TransformationException
    {
        ImageContext imageContext = new ImageContext(sourceMimeType, targetMimeType, sourceStream);

        for(ImageTransformationConfiguration imageTransformationConfiguration : imageTransformationConfigurationList) {
            ImageTransformation imageTransformation = imageTransformationProvider.getImageTransformation(imageTransformationConfiguration.getTransformationName());
            if(imageTransformation != null) {
                Map<String, String> parameters = null;
                if(noCrop) {
                    parameters = new HashMap<>(imageTransformationConfiguration.getParameters());
                    parameters.put("noCrop", "true");
                } else {
                    parameters = imageTransformationConfiguration.getParameters();
                }
                OperationContext operationContext = new OperationContext(renditionName, parameters);
                // Disabled Transformations will stop the rendition creation as it does create incomplete or non-renditioned images
                imageTransformation.transform(imageContext, operationContext);
            }
        }
        return imageContext;
    }

    /**
     * Obtains the Data Stream of the given resource
     * @param resource Resource containing the data. If this is not the jcr:content node then this node will be obtained first
     * @return Input Stream if resource, properties and data property was found otherwise <code>null</code>
     */
    private InputStream getDataStream(Resource resource) {
        InputStream answer = null;
        if(resource != null && !PerConstants.JCR_CONTENT.equals(resource.getName())) {
            resource = resource.getChild(PerConstants.JCR_CONTENT);
        }
        if(resource != null) {
            ValueMap properties = resource != null ? resource.getValueMap() : null;
            answer = properties != null ? properties.get(PerConstants.JCR_DATA, InputStream.class) : null;
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

