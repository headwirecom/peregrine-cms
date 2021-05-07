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
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.replication.PerReplicable;
import com.peregrine.replication.ReplicationUtil;
import org.apache.sling.api.resource.Resource;

import java.io.IOException;
import java.util.Calendar;

import static com.peregrine.admin.servlets.NodesServlet.ACTIVATED;
import static com.peregrine.admin.servlets.NodesServlet.DATE_FORMATTER;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerConstants.SOURCE_PATH;
import static java.util.Objects.nonNull;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public final class ReferenceServletUtils {

    public static final String SAME_TENANT = "sameTenant";
    public static final String IS_STALE = "is_stale";
    public static final String GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE = "Given Path does not yield a resource";

    public static AbstractBaseServlet.ErrorResponse badRequest(final String path) throws IOException {
        return new AbstractBaseServlet.ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE)
                .setRequestPath(path);
    }

    public static void addBasicProps(final Resource resource, final JsonResponse target) throws IOException {
        target.writeAttribute(NAME, resource.getName());
        target.writeAttribute(PATH, resource.getPath());
    }

    public static void addBasicSourceProps(final Resource resource, final JsonResponse target) throws IOException {
        target.writeAttribute(SOURCE_NAME, resource.getName());
        target.writeAttribute(SOURCE_PATH, resource.getPath());
    }

    public static void addReplicationProps(final Resource resource, final JsonResponse target) throws IOException {
        final PerReplicable replicable = resource.adaptTo(PerReplicable.class);
        target.writeAttribute(ACTIVATED, replicable.isReplicated());
        final Calendar replicated = replicable.getReplicated();
        if (nonNull(replicated)) {
            target.writeAttribute(PER_REPLICATED, DATE_FORMATTER.format(replicated.getTime()));
        }

        final Calendar lastModified = replicable.getLastModified();
        if (nonNull(lastModified)) {
            target.writeAttribute(JCR_LAST_MODIFIED, DATE_FORMATTER.format(lastModified.getTime()));
        }

        if (nonNull(lastModified) && nonNull(replicated)) {
            target.writeAttribute(IS_STALE, replicable.isStale());
        }
    }

    public static PerUtil.ResourceChecker getChecker(final AbstractBaseServlet.Request request) {
        if (!request.getBooleanParameter(SAME_TENANT, false)) {
            return PerUtil.ADD_ALL_RESOURCE_CHECKER;
        }

        final String path = request.getParameter(PATH);
        final Resource resource = request.getResourceResolver().getResource(path);
        return new ReplicationUtil.TenantOwnedResourceChecker(resource);
    }

}
