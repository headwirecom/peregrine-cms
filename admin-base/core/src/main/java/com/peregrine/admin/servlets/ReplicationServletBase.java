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
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.replication.Replication;
import com.peregrine.replication.Replication.ReplicationException;
import com.peregrine.replication.ReplicationsContainerWithDefault;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static java.util.Objects.isNull;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public abstract class ReplicationServletBase extends AbstractBaseServlet {

    public static final String REPLICATION_NOT_FOUND_FOR_NAME = "Replication not found for name: ";
    public static final String REPLICATION_FAILED = "Replication Failed";
    public static final String REPLICATES = "replicates";

    @Override
    protected final Response handleRequest(final Request request) throws IOException {
        logger.trace("Request Path: '{}'", request.getRequestPath());
        final var slingRequest = request.getRequest();
        logger.trace("Request URI: '{}'", slingRequest.getRequestURI());
        logger.trace("Request URL: '{}'", slingRequest.getRequestURL());

        final String sourcePath = request.getParameter(PATH);
        final ResourceResolver resourceResolver = request.getResourceResolver();
        final Resource resource = resourceResolver.getResource(sourcePath);
        if (isNull(resource)) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(String.format("Suffix: '%s' is not a resource", sourcePath));
        }

        final String replicationName = StringUtils.defaultString(request.getParameter(NAME), "defaultRepl");
        final Replication replication = getReplications().getOrDefault(replicationName);
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
            return performReplication(replication, request, replicable, resourceResolver);
        } catch (final ReplicationException e) {
            return badRequestReplicationFailed(e);
        }
    }

    protected abstract ReplicationsContainerWithDefault getReplications();

    protected abstract Response performReplication(
            Replication replication, Request request,
            PerReplicable replicable,
            ResourceResolver resourceResolver
    ) throws IOException, ReplicationException;

    protected static ErrorResponse badRequestReplicationFailed(final Exception e) throws IOException {
        return badRequest(REPLICATION_FAILED).setException(e);
    }

    protected static ErrorResponse badRequest(final String message) throws IOException {
        return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(message);
    }

    protected static Stream<Resource> streamReplicableResources(final Collection<Resource> resources) {
        return resources.stream()
                .map(r -> r.adaptTo(PerReplicable.class))
                .filter(Objects::nonNull)
                .map(PerReplicable::getMainResource)
                .filter(Objects::nonNull);
    }

    protected static Response prepareResponse(final Resource resource, final List<Resource> replicatedStuff) throws IOException {
        final JsonResponse answer = new JsonResponse();
        answer.writeAttribute("sourceName", resource.getName());
        answer.writeAttribute("sourcePath", resource.getPath());
        answer.writeArray(REPLICATES);
        for (final Resource r : replicatedStuff) {
            answer.writeObject();
            answer.writeAttribute(NAME, r.getName());
            answer.writeAttribute(PATH, r.getPath());
            answer.writeClose();
        }

        answer.writeClose();
        return answer;
    }

    protected static void ensureReplicationMixin(final List<Resource> resources) {
        streamReplicableResources(resources)
                .map(r -> r.adaptTo(PerReplicable.class))
                .filter(Objects::nonNull)
                .forEach(PerReplicable::ensureReplicableMixin);
    }

    protected static void markAsActivated(final List<Resource> resources) {
        resources.stream()
                .map(r -> r.adaptTo(PerReplicable.class))
                .filter(Objects::nonNull)
                .forEach(PerReplicable::setLastReplicationActionAsActivated);
    }

}