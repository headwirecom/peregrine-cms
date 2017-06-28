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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.peregrine.adaption.PerAsset;
import com.peregrine.admin.replication.ImageMetadataSelector;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.obtainParameters;
import static com.peregrine.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_DATA;
import static com.peregrine.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.util.PerConstants.JCR_TITLE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.util.PerUtil.EQUALS;
import static com.peregrine.util.PerUtil.PER_VENDOR;
import static com.peregrine.util.PerUtil.TEMPLATE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=upload files servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=api/admin/uploadFiles",
    }
)
@SuppressWarnings("serial")
public class UploadFilesServlet extends AbstractBaseServlet {

    private final Logger log = LoggerFactory.getLogger(UploadFilesServlet.class);

    @Reference
    ModelFactory modelFactory;

    private List<ImageMetadataSelector> imageMetadataSelectors = new ArrayList<>();

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC
    )
    void addImageMetadataSelector(ImageMetadataSelector selector)    { imageMetadataSelectors.add(selector); }
    void removeImageMetadataSelector(ImageMetadataSelector selector) { imageMetadataSelectors.remove(selector); }

    @Override
    Response handleRequest(Request request) throws IOException {
        String path = request.getParameter("path");
        try {
            Resource resource = request.getResourceByPath(path);
//            Node node = session.getRootNode().getNode(parentPath.substring(1));
            log.debug("Upload files to resource: '{}'", resource);
            List<PerAsset> assets = new ArrayList<>();
            for (Part part : request.getParts()) {
                log.debug("part type {}",part.getContentType());
                log.debug("part name {}",part.getName());

                Node node = resource.adaptTo(Node.class);
                Node newAsset = node.addNode(part.getName(), ASSET_PRIMARY_TYPE);
                Node content = newAsset.addNode(JCR_CONTENT, ASSET_CONTENT_TYPE);
                Binary data = node.getSession().getValueFactory().createBinary(part.getInputStream());
                content.setProperty(JCR_DATA, data);
                content.setProperty(JCR_MIME_TYPE, part.getContentType());
                newAsset.getSession().save();

                Resource assetResource = request.getResourceResolver().getResource(newAsset.getPath());
                PerAsset perAsset = assetResource.adaptTo(PerAsset.class);
                assets.add(perAsset);
                try {
                    Metadata metadata = ImageMetadataReader.readMetadata(perAsset.getRenditionStream((Resource) null));
                    for(Directory directory : metadata.getDirectories()) {
                        String directoryName = directory.getName();
                        ImageMetadataSelector selector = null;
                        for(ImageMetadataSelector item: imageMetadataSelectors) {
                            String temp = item.acceptCategory(directoryName);
                            if(temp != null) {
                                selector = item;
                                directoryName = temp;
                            }
                        }
                        boolean asJson = selector != null && selector.asJsonProperty();
                        String json = "{";
                        for(Tag tag : directory.getTags()) {
                            String name = tag.getTagName();
                            String tagName = selector != null ? selector.acceptTag(name) : name;
                            if(tagName != null) {
                                log.debug("Add Tag, Category: '{}', Tag Name: '{}', Value: '{}'", new Object[]{directoryName, tagName, tag.getDescription()});
                                if(asJson) {
                                    json += "\"" + tagName + "\":\"" + tag.getDescription() + "\",";
                                } else {
                                    perAsset.addTag(directoryName, tagName, tag.getDescription());
                                }
                            }
                        }
                        if(asJson) {
                            if(json.length() > 1) {
                                json = json.substring(0, json.length() - 1);
                                json += "}";
                                perAsset.addTag(directoryName, "raw_tags", json);
                            }
                        }
                    }
                } catch(ImageProcessingException e) {
                    e.printStackTrace();
                }
            }
            log.debug("Upload Done successfully and saved");
            JsonResponse answer = new JsonResponse()
                .writeAttribute("resourceName", resource.getName())
                .writeAttribute("resourcePath", resource.getPath())
                .writeArray("assets");
            for(PerAsset asset: assets) {
                answer.writeObject();
                answer.writeAttribute("assetName", asset.getName());
                answer.writeAttribute("assetPath", asset.getPath());
                answer.writeClose();
            }
            return answer;
        } catch (RepositoryException e) {
            log.debug("Upload Failed", e);
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Upload Failed because of JCR Repository").setRequestPath(path).setException(e);
        } catch(ServletException e) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Upload Failed because of Servlet Parts Problem").setRequestPath(path).setException(e);
        }
    }

}

