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

import com.peregrine.adaption.PerReplicable;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_NODES;
import static com.peregrine.admin.servlets.ReferenceListerServlet.IS_STALE;
import static com.peregrine.commons.Chars.COLON;
import static com.peregrine.commons.ResourceUtils.getAbsoluteParent;
import static com.peregrine.commons.ResourceUtils.isAncestorOrEqual;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

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
    public static final String ACTIVATED_SELF_OR_DESCENDANT = "activatedSelfOrDescendant";
    public static final String DEACTIVATED = "deactivated";
    public static final String REPLICATION_STATUS = "ReplicationStatus";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String JCR_PREFIX = "jcr:";
    public static final String PER_PREFIX = "per:";

    private static final String[] OMIT_PREFIXES = new String[] { JCR_PREFIX, PER_PREFIX };

    public static DateFormat DATE_FORMATTER = new SimpleDateFormat(ECMA_DATE_FORMAT, ECMA_DATE_FORMAT_LOCALE);

    @Override
    protected Response handleRequest(final Request request) throws IOException {
        final ResourceResolver resourceResolver = request.getResourceResolver();
        final Optional<Resource> optionalResource = Optional.of(PATH)
                .map(request::getParameter)
                .map(resourceResolver::getResource);
        if(optionalResource.isEmpty()) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(NO_PATH_PROVIDED);
        }

        final Resource resource = optionalResource.get();
        return convertResource(new JsonResponse(), resource, 0);
    }

    private JsonResponse convertResource(JsonResponse json, final Resource resource, final int level) throws IOException {
        final Resource parent = getAbsoluteParent(resource, level);
        if (isNull(parent)) {
            return json;
        }

        writeProperties(parent, json);
        json.writeArray(CHILDREN);
        for(final Resource child : parent.getChildren()) {
            if (isAncestorOrEqual(resource, child)) {
                json.writeObject();
                convertResource(json, resource, level + 1);
                json.writeClose();
            } else if (!isJcrContent(child)) {
                json.writeObject();
                writeProperties(child, json);
                if (isPrimaryType(child, ASSET_PRIMARY_TYPE)) {
                    final ValueMap props = child.getChild(JCR_CONTENT).getValueMap();
                    json.writeAttribute(MIME_TYPE, props.get(JCR_MIME_TYPE, String.class));
                    json.writeAttribute(TITLE, props.get(TITLE, String.class));
                    json.writeAttribute("description", props.get("description", String.class));
                    final List<Tag> tags = Optional.ofNullable(child.getChild("jcr:content/tags"))
                            .map(Resource::getChildren)
                            .map(Iterable::spliterator)
                            .map(s -> StreamSupport.stream(s, false))
                            .orElse(Stream.empty())
                            .map(Tag::new)
                            .collect(Collectors.toList());
                    if (tags.size() > 0) {
                        json.writeArray("tags");
                        for (final Tag tag : tags) {
                            json.writeObject();
                            writeBasicProperties(tag, json);
                            json.writeAttribute("value", tag.getValue());
                            json.writeClose();
                        }

                        json.writeClose();
                    }
                } else if (isPrimaryType(child, PAGE_PRIMARY_TYPE)) {
                    final Resource content = getJcrContent(child);
                    if (nonNull(content)) {
                        final ValueMap valueMap = content.getValueMap();
                        for (final String key : valueMap.keySet()) {
                            if (key.equals(JCR_TITLE)) {
                                json.writeAttribute(TITLE, valueMap.get(key, String.class));
                            } else if (key.indexOf(COLON) < 0) {
                                json.writeAttribute(key, valueMap.get(key, String.class));
                            }
                        }

                        json.writeAttribute(COMPONENT, getComponentNameFromResource(content));
                        convertNamedChild(json, content, TAGS);
                        convertNamedChild(json, content, METAPROPERTIES);
                    } else {
                        logger.debug("No Content Child found for: '{}'", child.getPath());
                    }
                }

                json.writeClose();
            }
        }

        json.writeClose();
        return json;
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

    private boolean hasNonJcrContentChild(final Resource res) {
        for (final Resource child : res.getChildren()) {
            if (!isJcrContent(child)) {
                return true;
            }
        }

        return false;
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
        writeBasicProperties(resource, json);
        json.writeAttribute(HAS_CHILDREN, hasNonJcrContentChild(resource));
        final ValueMap properties = resource.getValueMap();
        writeIfFound(json, JCR_PRIMARY_TYPE, properties, RESOURCE_TYPE);
        writeIfFound(json, JCR_CREATED, properties);
        writeIfFound(json, JCR_CREATED_BY, properties);
        writeIfFound(json, JCR_LAST_MODIFIED, properties);
        writeIfFound(json, JCR_LAST_MODIFIED_BY, properties);
        writeIfFound(json, JCR_LAST_MODIFIED_BY, properties);
        writeIfFound(json, ALLOWED_OBJECTS, properties);

        // For the Replication data we need to obtain the content properties. If not found
        // then we try with the resource's properties for non jcr:content nodes
        final ValueMap replicationProperties = Optional.ofNullable(resource)
                .map(PerUtil::getProperties)
                .orElse(properties);
        writeIfFound(json, PER_REPLICATED_BY, replicationProperties);
        final String replicationDate = writeIfFound(json, PER_REPLICATED, replicationProperties);
        if (isNotBlank(replicationDate)) {
            String status = ACTIVATED;
            final String replicationLocationRef = writeIfFound(json, PER_REPLICATION_REF, replicationProperties);
            if (isNotBlank(replicationLocationRef)) {
                status = DEACTIVATED;
            }

            json.writeAttribute(REPLICATION_STATUS, status);
        }

        // TODO refactor code above to use PerReplicable when writing replication properties
        final PerReplicable sourceRepl = resource.adaptTo(PerReplicable.class);
        json.writeAttribute(ACTIVATED, sourceRepl.isReplicated());
        if (sourceRepl.getLastModified()!=null && sourceRepl.getReplicated()!= null) {
            json.writeAttribute(IS_STALE, sourceRepl.isStale());
        }
    }

    private void writeBasicProperties(final Resource resource, final JsonResponse target) throws IOException {
        target.writeAttribute(NAME, resource.getName());
        target.writeAttribute(PATH, resource.getPath());
    }

    private String writeIfFound(JsonResponse json, String propertyName, ValueMap properties) throws IOException {
        return writeIfFound(json, propertyName, properties, propertyName);
    }

    private String writeIfFound(JsonResponse json, String propertyName, ValueMap properties, String responseName) throws IOException {
        Object value = properties.get(propertyName);
        String data;
        if(value instanceof Calendar) {
            data = DATE_FORMATTER.format(((Calendar) value).getTime());
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

    private final class Tag extends ResourceWrapper {
        private final String path;
        private final String value;

        public Tag(final Resource r) {
            super(r);
            this.path = substringAfter(r.getPath(), substringBefore(r.getPath(), "/jcr:content"));
            this.value = getValueMap().get("value", String.class);
        }

        @Override
        public String getPath() { return path; }
        public String getValue() { return value; }
        @Override
        public String toString() { return getName(); }
    }

}

