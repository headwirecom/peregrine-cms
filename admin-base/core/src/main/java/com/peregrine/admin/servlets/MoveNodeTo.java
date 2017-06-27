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

import com.peregrine.admin.resource.ResourceRelocation;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.util.PerConstants.ORDER_CHILD_TYPE;
import static com.peregrine.util.PerUtil.EQUALS;
import static com.peregrine.util.PerUtil.PER_PREFIX;
import static com.peregrine.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Move Node To Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/moveNodeTo"
    }
)
@SuppressWarnings("serial")
public class MoveNodeTo extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Reference
    private ResourceRelocation resourceRelocation;

    @Override
    Response handleRequest(Request request) throws IOException {
        Response answer;
        String toPath = request.getParameter("path");
        String fromPath = request.getParameter("component");
        String type = request.getParameter("type");
        // Next Block is only here to be backwards compatible
        if(type == null || type.isEmpty()) {
            type = request.getParameter("drop", "not provided");
        }
        boolean addAsChild = ORDER_CHILD_TYPE.equals(type) || type.startsWith("into");
        boolean addBefore = ORDER_BEFORE_TYPE.equals(type) || type.endsWith("-before");
        logger.trace("Add resource: '{}' to {}: '{}' {}",
            fromPath, addAsChild ? "parent" : "sibling", toPath, addBefore ? "before" : "after"
        );
        try {
            Resource toResource = request.getResourceByPath(toPath);
            Resource fromResource = request.getResourceByPath(fromPath);
            if(addAsChild) {
                boolean sameParent = resourceRelocation.hasSameParent(fromResource, toResource);
                if(!sameParent) {
//AS TODO: Shouldn't we try to update the references ?
                    // If not the same parent then just move as they are added at the end
                    resourceRelocation.moveToNewParent(fromResource, toResource, false);
                }
                if(addBefore || sameParent) {
                    // If we move to the front or if it is the same parent (move to the end)
                    // No Target Child Name means we move it the front for before and end for after
                    resourceRelocation.reorder(toResource.getParent(), fromResource.getName(), null, addBefore);
                }
                answer = new RedirectResponse(toPath + ".model.json");
            } else {
                // Both BEFORE and AFTER can be handled in one as the only difference is if added before or after
                // and if they are the same parent we means we only ORDER otherwise we MOVE first
                boolean sameParent = resourceRelocation.hasSameParent(fromResource, toResource);
                if(!sameParent) {
                    resourceRelocation.moveToNewParent(fromResource, toResource.getParent(), false);
                }
                resourceRelocation.reorder(toResource.getParent(), fromResource.getName(), toResource.getName(), addBefore);
                answer = new RedirectResponse(toResource.getParent().getPath() + ".model.json");
            }
        } catch (Exception e) {
            logger.error("problems while moving", e);
            answer = new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to Move Resource").setException(e);
        }
        return answer;
    }
}

