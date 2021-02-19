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

import com.peregrine.replication.PerReplicable;
import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.util.PerConstants;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_DO_REPLICATION;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerUtil.AddAllResourceChecker;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.listMissingResources;
import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.isNull;
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
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
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
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_DO_REPLICATION
    }
)
@SuppressWarnings("serial")
public final class ReplicationServlet extends ReplicationServletBase {

    public static final String DEACTIVATE = "deactivate";
    public static final String REPLICATION_NOT_FOUND_FOR_NAME = "Replication not found for name: ";
    public static final String REPLICATION_FAILED = "Replication Failed";
    public static final String REPLICATES = "replicates";
    public static final String RESOURCES = "resources";
    public static final String SUFFIX_IS_NOT_RESOURCE = "Suffix: '%s' is not a resource";
    public static final AddAllResourceChecker ADD_ALL_RESOURCE_CHECKER = new AddAllResourceChecker();

    @Reference
    private ReplicationsContainer replicationsContainer;

    @Reference
    private AdminResourceHandler resourceManagement;

    @Override
    protected Response performReplication(
            final Request request,
            final Resource resource,
            final ResourceResolver resourceResolver
    ) throws IOException {
        final String replicationName = request.getParameter(NAME);
        final Replication replication = replicationsContainer.getOrDefault(replicationName);
        if (isNull(replication)) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(REPLICATION_NOT_FOUND_FOR_NAME + replicationName);
        }

        final PerReplicable replicable = resource.adaptTo(PerReplicable.class);
        if (isNull(replicable)) {
            return prepareResponse(resource, Collections.emptyList());
        }

        replicable.ensureReplicableMixin();
        try {
            if (parseBoolean(request.getParameter(DEACTIVATE))) {
                replicable.setLastReplicationActionAsDeactivated();
                final var replicatedStuff = replication.deactivate(resource);
                for (final Resource r : streamReplicableResources(replicatedStuff)
                        .collect(Collectors.toList())) {
                    resourceManagement.deleteVersionLabel(r, PerConstants.PUBLISHED_LABEL);
                }

                return prepareResponse(resource, replicatedStuff);
            }

            replicable.setLastReplicationActionAsActivated();
            final boolean deep = parseBoolean(request.getParameter("deep"));
            List<Resource> toBeReplicated = listMissingResources(resource, new LinkedList<>(), ADD_ALL_RESOURCE_CHECKER, deep);
            for (final Resource r : Optional.of(RESOURCES)
                    .map(request::getParameterValues)
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .map(resourceResolver::getResource)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())){
                listMissingResources(r, toBeReplicated, ADD_ALL_RESOURCE_CHECKER, deep);
            }

            toBeReplicated = replication.prepare(toBeReplicated);
            // Replication can be local or remote and so the commit of the changes is done inside the Replication Service
            streamReplicableResources(toBeReplicated)
                    .map(Resource::getPath)
                    .forEach(p -> {
                        try {
                            resourceManagement.createVersion(resourceResolver, p, PerConstants.PUBLISHED_LABEL);
                        } catch (final AdminResourceHandler.ManagementException e) {
                            logger.trace("Unable to create a version for path: {} ", p, e);
                        }
                    });

            return prepareResponse(resource, replication.replicate(toBeReplicated));
        } catch (final ReplicationException e) {
            return badRequestReplicationFailed(e);
        }
    }

}