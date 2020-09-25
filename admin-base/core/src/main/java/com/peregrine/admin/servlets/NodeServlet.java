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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_NODE;
import static com.peregrine.commons.util.PerConstants.DATA_JSON_EXTENSION;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.JSON_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.intra.IntraSlingCaller;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.Servlet;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Redirects the Request to the ".data.json" rendering
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Node Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_NODE
    }
)
@SuppressWarnings("serial")
public class NodeServlet extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Reference
    @SuppressWarnings("unused")
    private IntraSlingCaller intraSlingCaller;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String path = request.getParameter(PATH);
        Resource resource = request.getResourceByPath(path);
	 // Load that content internally  and return as JSON Content. If it fails redirect
        try {
            byte[] response = intraSlingCaller.call(
                intraSlingCaller.createContext()
                    .setResourceResolver(request.getRequest().getResourceResolver())
                    .setPath(resource.getPath())
                    .setExtension(DATA_JSON_EXTENSION)
            );
            return new TextResponse(JSON, JSON_MIME_TYPE)
                .write(new String(response, Charset.forName("utf-8")));
        } catch(IntraSlingCaller.CallException e) {
             logger.warn("Internal call failed", e);
        }
        return new RedirectResponse(path + DATA_JSON_EXTENSION);
    }
}
