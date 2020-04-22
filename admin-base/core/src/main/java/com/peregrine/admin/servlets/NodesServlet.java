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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_NODES;
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
import static com.peregrine.commons.util.PerUtil.EQUALS;
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

import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.util.PerUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * List all the resources part of the given Path
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Nodes Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_NODES
    }
)
@SuppressWarnings("serial")
public class NodesServlet extends AbstractBaseServlet {

    public static final String NO_PATH_PROVIDED = "No Path provided";
    public static final String CHILDREN = "children";
    public static final String HAS_CHILDREN = "hasChildren";
    public static final String MIME_TYPE = "mimeType";
    public static final String ACTIVATED = "activated";
    public static final String DEACTIVATED = "deactivated";
    public static final String REPLICATION_STATUS = "ReplicationStatus";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String JCR_PREFIX = "jcr:";
    public static final String PER_PREFIX = "per:";

    private static final String[] OMIT_PREFIXES = new String[] {JCR_PREFIX, PER_PREFIX};

    private static DateFormat formatter = new SimpleDateFormat(ECMA_DATE_FORMAT, ECMA_DATE_FORMAT_LOCALE);

    @Reference
    ModelFactory modelFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String path = request.getParameter(PATH);
        if(path == null || path.isEmpty()) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(NO_PATH_PROVIDED)
                .setRequestPath(path);
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
                if(key.indexOf(":") < 0) {
                    json.writeAttribute(key, child.getValueMap().get(key, String.class));
                }
            }
            json.writeClose();
        }
    }

    private void convertResource(JsonResponse json, Resource resource) throws IOException {
        convertResource(json, resource, false);
    }

    private boolean hasNonJcrContentChild(Resource res) {
        for (Resource child : res.getChildren()) {
            if(!JCR_CONTENT.equals(child.getName())) {
                return true;
            }            
        }
        return false;
    }

    private void convertResource(JsonResponse json, ResourceResolver rs, String[] segments, int pos, String fullPath) throws IOException {
        String path = "";
        for(int i = 1; i <= pos; i++) {
            path += "/" + segments[i];
        }
        logger.debug("looking up {}", path);
        Resource res = rs.getResource(path);
        json.writeAttribute(NAME,res.getName());
        json.writeAttribute(PATH,res.getPath());
        json.writeAttribute(HAS_CHILDREN, hasNonJcrContentChild(res));
        writeProperties(res, json);
        json.writeArray(CHILDREN);
        Iterable<Resource> children = res.getChildren();
        for(Resource child : children) {
            String childPath = child.getPath();
            if(fullPath.startsWith(childPath+'/') || fullPath.equals(childPath)) {
                json.writeObject();
                convertResource(json, rs, segments, pos+1, fullPath);
                json.writeClose();
            } else {
                if(!JCR_CONTENT.equals(child.getName())) {
                    json.writeObject();
                    json.writeAttribute(NAME,child.getName());
                    json.writeAttribute(PATH,child.getPath());
                    json.writeAttribute(HAS_CHILDREN, hasNonJcrContentChild(child));
                    writeProperties(child, json);
                    if(isPrimaryType(child, ASSET_PRIMARY_TYPE)) {
                        ValueMap props = child.getChild(JCR_CONTENT).getValueMap();
                        String mimeType = props.get(JCR_MIME_TYPE, String.class);
                        String title = props.get(TITLE, String.class);
                        String description = props.get("description", String.class);
                        json.writeAttribute(MIME_TYPE, mimeType);
                        json.writeAttribute(TITLE, title);
                        json.writeAttribute("description", description);

                        Resource tags = child.getChild("jcr:content/tags");
                        List<Tag> answer = new ArrayList<Tag>();
                        if(tags != null) {
                            for(Resource tag: tags.getChildren()) {
                                answer.add(new Tag(tag));
                            }
                        }
                        if(answer.size() > 0) {
                            json.writeArray("tags");
                            for (Tag tag : answer) {
                                json.writeObject();
                                json.writeAttribute(PATH, tag.getPath());
                                json.writeAttribute(NAME, tag.getName());
                                json.writeAttribute("value", tag.getValue());
                                json.writeClose();
                            }
                            json.writeClose();
                        }
                    
                    }
                    if(isPrimaryType(child, PAGE_PRIMARY_TYPE)) {
                        Resource content = child.getChild(JCR_CONTENT);
                        if(content != null) {
                            for (String key: content.getValueMap().keySet()) {
                                if(key.equals(JCR_TITLE)) {
                                    String title = content.getValueMap().get(JCR_TITLE, String.class);
                                    json.writeAttribute(TITLE, title);
                                } else {
                                    if(key.indexOf(":") < 0) {
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
                    json.writeClose();
                }
            }
        }
        json.writeClose();
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
        // then we try with the resource's properties for non jcr:content nodes
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

    class Tag {
        private String path;
        private String name;
        private String value;

        public Tag(Resource r) {
            this.path = r.getPath();
            this.path = path.substring(path.indexOf("/jcr:content"));
            this.name = r.getName();
            this.value = r.getValueMap().get("value", String.class);
        }

        public String getName() { return name; }
        public String getValue() { return value; }
        public String getPath() { return path; }
        @Override
        public String toString() { return name; }
    }

}

