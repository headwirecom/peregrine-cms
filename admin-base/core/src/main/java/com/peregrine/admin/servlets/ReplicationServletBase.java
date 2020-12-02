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
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.peregrine.admin.replication.ReplicationConstants.SOURCE_NAME;
import static com.peregrine.admin.replication.ReplicationConstants.SOURCE_PATH;
import static com.peregrine.admin.servlets.ReplicationServlet.REPLICATION_FAILED;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.AddAllResourceChecker;
import static java.util.Objects.isNull;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public abstract class ReplicationServletBase extends AbstractBaseServlet {

    public static final String REPLICATES = "replicates";
    public static final AddAllResourceChecker ADD_ALL_RESOURCE_CHECKER = new AddAllResourceChecker();

    @Override
    protected final Response handleRequest(final Request request) throws IOException {
        logger.trace("Request Path: '{}'", request.getRequestPath());
        final var slingRequest = request.getRequest();
        logger.trace("Request URI: '{}'", slingRequest.getRequestURI());
        logger.trace("Request URL: '{}'", slingRequest.getRequestURL());

        final String sourcePath = request.getParameter(PATH);
        final ResourceResolver resourceResolver = request.getResourceResolver();
        final Resource source = resourceResolver.getResource(sourcePath);
        if (isNull(source)) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(String.format("Suffix: '%s' is not a resource", sourcePath));
        }

        return performReplication(request, source, resourceResolver);
    }

    protected abstract Response performReplication(
            Request request,
            Resource resource,
            ResourceResolver resourceResolver
    ) throws IOException;

    protected static ErrorResponse badRequestReplicationFailed(final Exception e) throws IOException {
        return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(REPLICATION_FAILED)
                .setException(e);
    }

    protected static Stream<Resource> streamReplicableResources(final Collection<Resource> resources) {
        return resources.stream()
                .map(r -> r.adaptTo(PerReplicable.class))
                .filter(Objects::nonNull)
                .map(PerReplicable::getContentResource)
                .filter(Objects::nonNull);
    }

    protected static Response prepareResponse(final Resource resource, final List<Resource> replicatedStuff) throws IOException {
        final JsonResponse answer = new JsonResponse();
        answer.writeAttribute(SOURCE_NAME, resource.getName());
        answer.writeAttribute(SOURCE_PATH, resource.getPath());
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

}