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
import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.peregrine.admin.util.AdminPathConstants.RESOURCE_TYPE_NODES;
import static com.peregrine.commons.util.PerConstants.ALLOWED_OBJECTS;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.ECMA_DATE_FORMAT;
import static com.peregrine.commons.util.PerConstants.ECMA_DATE_FORMAT_LOCALE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_CREATED;
import static com.peregrine.commons.util.PerConstants.JCR_CREATED_BY;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED_BY;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.METAPROPERTIES;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED_BY;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATION_REF;
import static com.peregrine.commons.util.PerConstants.TAGS;
import static com.peregrine.commons.util.PerConstants.TITLE;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.getProperties;
import static com.peregrine.commons.util.PerUtil.isPrimaryType;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * List all the resources part of the given Path
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definintions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Nodes servlet",
        SERVICE_VENDOR + EQUAL + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUAL + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUAL + RESOURCE_TYPE_NODES
    }
)
@SuppressWarnings("serial")
public class NodesServlet extends AbstractBaseServlet {

    public static final String NO_PATH_PROVIDED = "No Path provided";
    public static final String CHILDREN = "children";
    public static final String MIME_TYPE = "mimeType";
    public static final String ACTIVATED = "activated";
    public static final String DEACTIVATED = "deactivated";
    public static final String REPLICATION_STATUS = "ReplicationStatus";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String JCR_PREFIX = "jcr:";
    public static final String PER_PREFIX = "per:";

    private static final String[] OMIT_PREFIXES = new String[] {JCR_PREFIX, PER_PREFIX};

    private DateFormat formatter = new SimpleDateFormat(ECMA_DATE_FORMAT, ECMA_DATE_FORMAT_LOCALE);

    @Reference
    private transient ModelFactory modelFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String path = request.getParameter(PATH);
        if(path == null || path.isEmpty()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(NO_PATH_PROVIDED).setRequestPath(path);
        }
        String[] segments = path.split("/");
        logger.debug("lookup path {}, {}", path, segments.length);
        JsonResponse answer = new JsonResponse();
        convertResource(answer, request.getResourceResolver(), segments, 1, path);
        return answer;
    }

    private void convertResource(JsonResponse json, Resource resource, boolean path) throws IOException {
        Iterable<Resource> children = resource.getChildren();
        for(Resource child : children) {
            json.writeObject();
            json.writeAttribute(NAME, child.getName());
            if(path) {
                json.writeAttribute(PATH, child.getPath());
            }
            for (String key: child.getValueMap().keySet()) {
                if(key.indexOf(':') < 0) {
                    json.writeAttribute(key, child.getValueMap().get(key, String.class));
                }
            }
            json.writeClose();
        }
    }

    private void convertResource(JsonResponse json, ResourceResolver rs, String[] segments, int pos, String fullPath) throws IOException {
        StringBuilder builder = new StringBuilder();
        for(int i = 1; i <= pos; i++) {
            builder.append('/').append(segments[i]);
        }
        String path = builder.toString();
        logger.debug("looking up {}", path);
        Resource res = rs.getResource(path);
        if(res != null) {
            json.writeAttribute(NAME, res.getName());
            json.writeAttribute(PATH, res.getPath());
            writeProperties(res, json);
            json.writeArray(CHILDREN);
            Iterable<Resource> children = res.getChildren();
            for (Resource child : children) {
                if (fullPath.startsWith(child.getPath())) {
                    json.writeObject();
                    convertResource(json, rs, segments, pos + 1, fullPath);
                    json.writeClose();
                } else {
                    if (!JCR_CONTENT.equals(child.getName())) {
                        json.writeObject();
                        json.writeAttribute(NAME, child.getName());
                        json.writeAttribute(PATH, child.getPath());
                        writeProperties(child, json);
                        if (isPrimaryType(child, ASSET_PRIMARY_TYPE)) {
                            convertAssetChild(child, json);
                        } else if (isPrimaryType(child, PAGE_PRIMARY_TYPE)) {
                            convertPageChild(child, json);
                        }
                        json.writeClose();
                    }
                }
            }
            json.writeClose();
        }
    }

    private void convertAssetChild(Resource child, JsonResponse json) throws IOException {
        Resource childContent = child.getChild(JCR_CONTENT);
        if(childContent != null) {
            String mimeType = childContent.getValueMap().get(JCR_MIME_TYPE, String.class);
            json.writeAttribute(MIME_TYPE, mimeType);
        } else {
            logger.debug("No Asset Content Child found for: '{}'", child.getPath());
        }
    }
    private void convertPageChild(Resource child, JsonResponse json) throws IOException {
        Resource content = child.getChild(JCR_CONTENT);
        if(content != null) {
            for (String key: content.getValueMap().keySet()) {
                if(key.equals(JCR_TITLE)) {
                    String title = content.getValueMap().get(JCR_TITLE, String.class);
                    json.writeAttribute(TITLE, title);
                } else {
                    if(key.indexOf(':') < 0) {
                        json.writeAttribute(key, content.getValueMap().get(key, String.class));
                    }
                }
            }
            String component = PerUtil.getComponentNameFromResource(content);
            json.writeAttribute(COMPONENT, component);
            convertNamedChild(json, content, TAGS);
            convertNamedChild(json, content, METAPROPERTIES);
        } else {
            logger.debug("No Content Child found for: '{}'", child.getPath());
        }
    }

    private void convertNamedChild(JsonResponse json, Resource content, String name) throws IOException {
        Resource res = content.getChild(name);
        if (res != null) {
            json.writeArray(name);
            convertResource(json, res, true);
            json.writeClose();
        }
    }

    private void writeProperties(Resource resource, JsonResponse json) throws IOException {
        ValueMap properties = resource.getValueMap();
        writeIfFound(json, JCR_PRIMARY_TYPE, properties, RESOURCE_TYPE);
        writeIfFound(json, JCR_CREATED, properties);
        writeIfFound(json, JCR_CREATED_BY, properties);
        writeIfFound(json, JCR_LAST_MODIFIED, properties);
        writeIfFound(json, JCR_LAST_MODIFIED_BY, properties);
        writeIfFound(json, JCR_LAST_MODIFIED_BY, properties);
        writeIfFound(json, ALLOWED_OBJECTS, properties);

        // For the Replication data we need to obtain the content properties. If not found
        // then we try with the resoure's properties for non jcr:content nodes
        ValueMap replicationProperties = getProperties(resource);
        if(replicationProperties == null) { replicationProperties = properties; }
        String replicationDate = writeIfFound(json, PER_REPLICATED, replicationProperties);
        writeIfFound(json, PER_REPLICATED_BY, replicationProperties);
        String replicationLocationRef = writeIfFound(json, PER_REPLICATION_REF, replicationProperties);
        if(replicationDate != null && !replicationDate.isEmpty()) {
            String status = ACTIVATED;
            if(replicationLocationRef == null || replicationLocationRef.isEmpty()) {
                status = DEACTIVATED;
            }
            json.writeAttribute(REPLICATION_STATUS, status);
        }
    }

    private String writeIfFound(JsonResponse json, String propertyName, ValueMap properties) throws IOException {
        return writeIfFound(json, propertyName, properties, propertyName);
    }

    private String writeIfFound(JsonResponse json, String propertyName, ValueMap properties, String responseName) throws IOException {
        Object value = properties.get(propertyName);
        String data;
        if(value instanceof Calendar) {
            data = formatter.format(((Calendar) value).getTime());
        } else {
            data = properties.get(propertyName, String.class);
        }
        if(data != null) {
            String name = responseName;
            for(String omitPrefix: OMIT_PREFIXES) {
                if(name.startsWith(omitPrefix)) {
                    name = name.substring(omitPrefix.length());
                    break;
                }
            }
            json.writeAttribute(name, data);
        }
        return data;
    }
}

