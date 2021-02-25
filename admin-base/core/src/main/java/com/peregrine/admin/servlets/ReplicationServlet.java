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

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.replication.Replication;
import com.peregrine.replication.Replication.ReplicationException;
import com.peregrine.replication.ReplicationsContainerWithDefault;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_DO_REPLICATION;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.listMissingResources;
import static java.lang.Boolean.parseBoolean;
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
    public static final String RESOURCES = "resources";

    @Reference
    private ReplicationsContainerWithDefault replications;

    @Reference
    private AdminResourceHandler resourceManagement;

    protected ReplicationsContainerWithDefault getReplications() {
        return replications;
    }

    @Override
    protected Response performReplication(
            final Replication replication,
            final Request request,
            final Resource resource,
            final ResourceResolver resourceResolver
    ) throws IOException, ReplicationException {
        if (parseBoolean(request.getParameter(DEACTIVATE))) {
            return performDeactivation(replication, resource);
        }

        final boolean deep = parseBoolean(request.getParameter("deep"));
        List<Resource> toBeReplicated = listMissingResources(resource, deep, new LinkedList<>());
        for (final Resource r : Optional.of(RESOURCES)
                .map(request::getParameterValues)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(resourceResolver::getResource)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())) {
            listMissingResources(r, deep, toBeReplicated);
        }

        toBeReplicated = replication.prepare(toBeReplicated);
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
    }

    @NotNull
    private Response performDeactivation(
            final Replication replication,
            final Resource resource
    ) throws ReplicationException, IOException {
        final var replicatedStuff = replication.deactivate(resource);
        for (final Resource r : streamReplicableResources(replicatedStuff)
                .collect(Collectors.toList())) {
            resourceManagement.deleteVersionLabel(r, PerConstants.PUBLISHED_LABEL);
        }

        return prepareResponse(resource, replicatedStuff);
    }

}