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
import com.peregrine.admin.replication.ReplicationUtil;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.Resource;
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
import static com.peregrine.commons.ResourceUtils.*;
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
    public static final String ANY_DESCENDANT_ACTIVATED = "anyDescendantActivated";
    public static final String DEACTIVATED = "deactivated";
    public static final String REPLICATION_STATUS = "ReplicationStatus";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String JCR_PREFIX = "jcr:";
    public static final String PER_PREFIX = "per:";

    private static final String[] OMIT_PREFIXES = new String[]{JCR_PREFIX, PER_PREFIX};

    public static DateFormat DATE_FORMATTER = new SimpleDateFormat(ECMA_DATE_FORMAT, ECMA_DATE_FORMAT_LOCALE);

    @Override
    protected Response handleRequest(final Request request) throws IOException {
        final Resource resource = getDeepestExistingResource(request.getResourceResolver(), request.getParameter(PATH));
        if (isNull(resource)) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(NO_PATH_PROVIDED);
        }

        return convertResource(resource, 0, new JsonResponse());
    }

    private JsonResponse convertResource(final Resource resource, final int level, final JsonResponse json) throws IOException {
        final Resource parent = getAbsoluteParent(resource, level);
        if (isNull(parent)) {
            return json;
        }

        writeBasicProperties(parent, json);
        json.writeArray(CHILDREN);
        for (final Resource child : parent.getChildren()) {
            if (isAncestorOrEqual(resource, child)) {
                json.writeObject();
                convertResource(resource, level + 1, json);
                json.writeClose();
            } else if (!isJcrContent(child)) {
                json.writeObject();
                writeBasicProperties(child, json);
                if (isPrimaryType(child, ASSET_PRIMARY_TYPE)) {
                    final ValueMap props = child.getChild(JCR_CONTENT).getValueMap();
                    json.writeAttribute(MIME_TYPE, props.get(JCR_MIME_TYPE, String.class));
                    json.writeAttribute(TITLE, props.get(TITLE, String.class));
                    json.writeAttribute("description", props.get("description", String.class));
                    final List<Tag> tags = Optional.ofNullable(child.getChild("jcr:content/tags"))
                            .map(Resource::getChildren)
                            .map(Iterable::spliterator)
                            .map(s -> StreamSupport.stream(s, false))
                            .orElseGet(() -> Stream.empty())
                            .map(Tag::new)
                            .collect(Collectors.toList());
                    if (tags.size() > 0) {
                        json.writeArray("tags");
                        for (final Tag tag : tags) {
                            json.writeObject();
                            writePathProperties(tag, json);
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
                        convertNamedChild(content, TAGS, json);
                        convertNamedChild(content, METAPROPERTIES, json);
                    } else {
                        logger.debug("No Content Child found for: '{}'", child.getPath());
                    }
                }

                json.writeAttribute(ANY_DESCENDANT_ACTIVATED, ReplicationUtil.isAnyDescendantReplicated(child));
                json.writeClose();
            }
        }

        json.writeClose();
        return json;
    }

    private void writeBasicProperties(final Resource resource, final JsonResponse json) throws IOException {
        writePathProperties(resource, json);
        final ValueMap properties = resource.getValueMap();
        writeIfFound(properties, JCR_PRIMARY_TYPE, json, RESOURCE_TYPE);
        writeIfFound(properties, JCR_CREATED, json);
        writeIfFound(properties, JCR_CREATED_BY, json);
        writeIfFound(properties, JCR_LAST_MODIFIED, json);
        writeIfFound(properties, JCR_LAST_MODIFIED_BY, json);
        writeIfFound(properties, JCR_LAST_MODIFIED_BY, json);
        json.writeAttribute(HAS_CHILDREN, hasNonJcrContentChild(resource));
        writeIfFound(properties, ALLOWED_OBJECTS, json);
        writeReplicationProperties(resource, json);
    }

    private void writePathProperties(final Resource resource, final JsonResponse json) throws IOException {
        json.writeAttribute(NAME, resource.getName());
        json.writeAttribute(PATH, resource.getPath());
    }

    private boolean writeReplicationProperties(final Resource resource, final JsonResponse json) throws IOException {
        // For the Replication data we need to obtain the content properties. If not found
        // then we try with the resource's properties for non jcr:content nodes
        final ValueMap properties = Optional.ofNullable(resource)
                .map(PerUtil::getProperties)
                .orElseGet(resource::getValueMap);
        writeIfFound(properties, PER_REPLICATED_BY, json);
        final String replicationDate = writeIfFound(properties, PER_REPLICATED, json);
        if (isNotBlank(replicationDate)) {
            String status = ACTIVATED;
            final String replicationLocationRef = writeIfFound(properties, PER_REPLICATION_REF, json);
            if (isBlank(replicationLocationRef)) {
                status = DEACTIVATED;
            }

            json.writeAttribute(REPLICATION_STATUS, status);
        }

        // TODO refactor code above to use PerReplicable when writing replication properties
        final PerReplicable replicable = resource.adaptTo(PerReplicable.class);
        final boolean isReplicated = replicable.isReplicated();
        json.writeAttribute(ACTIVATED, isReplicated);
        if (nonNull(replicable.getLastModified()) && nonNull(replicable.getReplicated())) {
            json.writeAttribute(IS_STALE, replicable.isStale());
        }

        return isReplicated;
    }

    private boolean hasNonJcrContentChild(final Resource res) {
        for (final Resource child : res.getChildren()) {
            if (!isJcrContent(child)) {
                return true;
            }
        }

        return false;
    }

    private String writeIfFound(final ValueMap properties, final String propertyName, final JsonResponse json) throws IOException {
        return writeIfFound(properties, propertyName, json, propertyName);
    }

    private String writeIfFound(final ValueMap properties, final String propertyName, final JsonResponse json, final String responseName) throws IOException {
        final Object value = properties.get(propertyName);
        final String data;
        if (value instanceof Calendar) {
            data = DATE_FORMATTER.format(((Calendar) value).getTime());
        } else {
            data = properties.get(propertyName, String.class);
        }

        if (nonNull(data)) {
            String name = responseName;
            for (final String prefix : OMIT_PREFIXES) {
                if (startsWith(name, prefix)) {
                    name = substringAfter(name, prefix);
                    break;
                }
            }

            json.writeAttribute(name, data);
        }

        return data;
    }

    private void convertNamedChild(final Resource content, final String name, final JsonResponse json) throws IOException {
        final Resource res = content.getChild(name);
        if (nonNull(res)) {
            json.writeArray(name);
            convertResource(res, json);
            json.writeClose();
        }
    }

    private void convertResource(final Resource resource, final JsonResponse json) throws IOException {
        for (final Resource child : resource.getChildren()) {
            json.writeObject();
            writePathProperties(child, json);
            final ValueMap valueMap = child.getValueMap();
            for (final String key : valueMap.keySet()) {
                if (key.indexOf(COLON) < 0) {
                    json.writeAttribute(key, valueMap.get(key, String.class));
                }
            }

            json.writeClose();
        }
    }

    private final class Tag extends ResourceWrapper {

        private final String path;
        private final String value;

        public Tag(final Resource r) {
            super(r);
            final String path = r.getPath();
            this.path = substringAfter(path, substringBefore(path, "/jcr:content"));
            this.value = getValueMap().get("value", String.class);
        }

        @Override
        public String getPath() {
            return path;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

}
