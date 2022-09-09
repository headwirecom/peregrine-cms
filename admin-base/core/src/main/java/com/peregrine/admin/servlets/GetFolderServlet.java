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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.servlets.ServletHelper;
import com.peregrine.intra.IntraSlingCaller;
import com.peregrine.intra.IntraSlingCaller.CallException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_GET_FOLDER;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_GET_FOLDER_SCHEMA;
import static com.peregrine.commons.util.PerConstants.DIALOG_JSON;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.JSON_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Redirects the Request to the ".json" for folder data
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Get Folder Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_GET_FOLDER,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_GET_FOLDER_SCHEMA
    }
)
@SuppressWarnings("serial")
public class GetFolderServlet extends AbstractBaseServlet {

    public static final String UNSUPPORTED_RESOURCE_TYPE = "Unsupported Resource Type";
    public static final String UNSUPPORTED_REQUEST_RESOURCE_TYPE = "Unsupported Request Resource Type";
    public static final String UNSUPPORTED_REQUEST_FOLDER_FOR_SCHEMA = "Schema not supported for no Objects Folder";

    private static final Pattern OBJECTS_PATH_PATTERN = Pattern.compile("^/content/.*/objects/.*");
    private static final String MODEL_FOLDER_PATH = "/apps/admin/object-definitions";

    private static final String MODEL_FORMAT =
        "{ " +
        "  \"name\": \"%s\", " +
        "  \"path\": \"%s\", " +
        "  \"resourceType\": \"%s\", " +
        "  \"model\": %s " +
        "}";

    private static final List<String> folderTypes = Arrays.asList(
        "nt:folder",
        "sling:Folder",
        "sling:OrderedFolder"
    );

    @Reference
    @SuppressWarnings("unused")
    private IntraSlingCaller intraSlingCaller;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String path = request.getParameter(PATH);
        Resource resource = request.getResourceByPath(path);
        if(request.getRequest().getResource().isResourceType(RESOURCE_TYPE_GET_FOLDER_SCHEMA)) {
            if(OBJECTS_PATH_PATTERN.matcher(path).matches()) {
                Resource objectDefinitionsFolder = request.getResourceResolver().getResource(MODEL_FOLDER_PATH);
                String model = "{}";
                if (objectDefinitionsFolder != null) {
                    Resource dialogResource = objectDefinitionsFolder.getChild(DIALOG_JSON);
                    InputStream is = dialogResource != null ? dialogResource.adaptTo(InputStream.class) : null;
                    model = is != null ? ServletHelper.asString(is).toString() : model;
                }
                String modelText = String.format(MODEL_FORMAT, resource.getName(), resource.getPath(), resource.getResourceType(), model);
                return new TextResponse(JSON, JSON_MIME_TYPE)
                    .write(modelText);
            } else {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(UNSUPPORTED_REQUEST_FOLDER_FOR_SCHEMA)
                    .setRequestPath(path);
            }
        } else
        if(request.getRequest().getResource().isResourceType(RESOURCE_TYPE_GET_FOLDER)) {
            String resourceType = resource.getResourceType();
            if (folderTypes.contains(resourceType)) {
                byte[] response = new byte[0];
                try {
                    response = intraSlingCaller.call(
                        intraSlingCaller.createContext()
                            .setResourceResolver(request.getRequest().getResourceResolver())
                            .setPath(resource.getPath())
                            .setExtension(JSON)
                    );
                    String jsonText = new String(response, Charset.forName("utf-8"));
                    JsonNode root = new ObjectMapper().readTree(jsonText);
                    ObjectNode object = (ObjectNode) root;
                    // Check if there is a 'jcr:title' entry and if provided set it as name
                    String title = object.has(JCR_TITLE) ? object.get(JCR_TITLE).asText() : resource.getName();
                    object.put("name", title);
                    object.put("path", resource.getPath());
                    object.put("resourceType", resourceType);

                    return new TextResponse(JSON, JSON_MIME_TYPE)
                        .write(object.toString());
                } catch (CallException e) {
                    return new ErrorResponse()
                        .setHttpErrorCode(SC_BAD_REQUEST)
                        .setErrorMessage(UNSUPPORTED_RESOURCE_TYPE)
                        .setRequestPath(path)
                        .setCustom("resource-type", resourceType);
                }
            } else {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(UNSUPPORTED_RESOURCE_TYPE)
                    .setRequestPath(path)
                    .setCustom("resource-type", resourceType);
            }
        } else {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(UNSUPPORTED_REQUEST_RESOURCE_TYPE)
                .setRequestPath(path);
        }
    }
}
