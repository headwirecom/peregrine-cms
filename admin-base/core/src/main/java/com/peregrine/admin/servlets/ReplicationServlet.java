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
import com.peregrine.admin.replication.ReplicationConstants;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.replication.Replication;
import com.peregrine.replication.Replication.ReplicationException;
import com.peregrine.replication.ReplicationsContainer;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.*;

import static com.peregrine.admin.replication.ReplicationConstants.*;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.AddAllResourceChecker;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.listMissingResources;
import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet replicates the given resource with its JCR Content
 * and any references
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 *
 * It is invoked like this:
 *      curl -X POST "http://localhost:8080/perapi/admin/repl.json/content/themeclean" -H  "accept: application/json" -H  "content-type: application/x-www-form-urlencoded" -d "name=defaultRepl&deep=false"
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Replication Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + ReplicationConstants.RESOURCE_TYPE_DO_REPLICATION
    }
)
@SuppressWarnings("serial")
public final class ReplicationServlet extends AbstractBaseServlet {

    public static final String DEACTIVATE = "deactivate";
    public static final String REPLICATION_NOT_FOUND_FOR_NAME = "Replication not found for name: ";
    public static final String REPLICATION_FAILED = "Replication Failed";
    public static final String REPLICATES = "replicates";
    public static final String PUBLISHED = "Published";
    public static final String RESOURCES = "resources";
    public static final String SUFFIX_IS_NOT_RESOURCE = "Suffix: '%s' is not a resource";
    public static final AddAllResourceChecker ADD_ALL_RESOURCE_CHECKER = new AddAllResourceChecker();

    @Reference
    private ReplicationsContainer replicationsContainer;

    @Reference
    private AdminResourceHandler resourceManagement;

    @Override
    protected Response handleRequest(final Request request) throws IOException {
        logger.trace("Request Path: '{}'", request.getRequestPath());
        logger.trace("Request URI: '{}'", request.getRequest().getRequestURI());
        logger.trace("Request URL: '{}'", request.getRequest().getRequestURL());
        final String replicationName = request.getParameter(NAME);
        final Replication replication = replicationsContainer.getOrDefault(replicationName);
        if (isNull(replication)) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(REPLICATION_NOT_FOUND_FOR_NAME + replicationName);
        }

        final String sourcePath = request.getParameter(PATH);
        final ResourceResolver resourceResolver = request.getResourceResolver();
        final Resource source = resourceResolver.getResource(sourcePath);
        if (isNull(source)) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(String.format(SUFFIX_IS_NOT_RESOURCE, sourcePath));
        }

        final List<Resource> replicates = new LinkedList<>();
        final PerReplicable sourceReplicable = source.adaptTo(PerReplicable.class);
        if (nonNull(sourceReplicable)) {
            final boolean deep = parseBoolean(request.getParameter(DEEP));
            final List<Resource> resourcesToReplicate = new ArrayList<>();
            listMissingResources(source, resourcesToReplicate, ADD_ALL_RESOURCE_CHECKER, deep);
            final String[] references = request.getParameterValues(RESOURCES);
            if (nonNull(references)) {
                Arrays.stream(references)
                        .map(resourceResolver::getResource)
                        .filter(Objects::nonNull)
                        .forEach(r -> listMissingResources(r, resourcesToReplicate, ADD_ALL_RESOURCE_CHECKER, deep));
            }

            try {
                if (parseBoolean(request.getParameter(DEACTIVATE))) {
                    sourceReplicable.setLastReplicationActionAsDeactivated();
                    replicates.addAll(replication.deactivate(source));
                } else {
                    // Replication can be local or remote and so the commit of the changes is done inside the Replication Service
                    final Optional<String> path = Optional.ofNullable(sourceReplicable.getContentResource())
                            .map(Resource::getPath);
                    if (path.isPresent()) {
                        resourceManagement.createVersion(resourceResolver, path.get(), PUBLISHED);
                    }

                    sourceReplicable.setLastReplicationActionAsActivated();
                    replicates.addAll(replication.replicate(resourcesToReplicate));
                }
            } catch (ReplicationException | AdminResourceHandler.ManagementException e) {
                return new ErrorResponse()
                        .setHttpErrorCode(SC_BAD_REQUEST)
                        .setErrorMessage(REPLICATION_FAILED)
                        .setException(e);
            }
        }

        final JsonResponse answer = new JsonResponse();
        answer.writeAttribute(SOURCE_NAME, source.getName());
        answer.writeAttribute(SOURCE_PATH, source.getPath());
        answer.writeArray(REPLICATES);
        for (final Resource child : replicates) {
            answer.writeObject();
            answer.writeAttribute(NAME, child.getName());
            answer.writeAttribute(PATH, child.getPath());
            answer.writeClose();
        }

        answer.writeClose();
        return answer;
    }

}