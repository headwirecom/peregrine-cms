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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * List Tools or Tools Config Info in a JSon representation
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "List Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_LIST
    }
)
@SuppressWarnings("serial")
public class ListServlet extends AbstractBaseServlet {

    private static final String TOOLS = "/tools";
    private static final String TOOLS_CONFIG = "/tools/config";
    private static final String CONTENT_ADMIN_TOOLS = "/content/admin/pages/tools";
    private static final String CONTENT_ADMIN_TOOLS_CONFIG = "/content/admin/pages/toolsConfig";
    private static final String UNKNOWN_SUFFIX = "Unknown suffix: ";
    private static final String ERROR_WHILE_EXPORTING_MODEL = "Error while exporting model";

    @Reference
    ModelFactory modelFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        String path = request.getParameter(PATH);
        if(path == null || path.isEmpty()) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage("No suffix provided")
                .setRequestPath(path);
        }
        Response answer;
        if(TOOLS.equals(path)) {
            answer = getJSONFromResource(request.getResource(), CONTENT_ADMIN_TOOLS);
        } else if(TOOLS_CONFIG.equals(path)) {
            answer = getJSONFromResource(request.getResource(), CONTENT_ADMIN_TOOLS_CONFIG);
        } else {
            answer = new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(UNKNOWN_SUFFIX + path);
        }
        return answer;
    }

    private Response getJSONFromResource(Resource resource, String resourcePath) throws IOException {
        Resource res = resource.getResourceResolver().getResource(resourcePath);
        try {
            String out = modelFactory.exportModelForResource(res, JACKSON, String.class, Collections.emptyMap());
            return new PlainJsonResponse(out);
        } catch (ExportException | MissingExporterException e) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(ERROR_WHILE_EXPORTING_MODEL)
                .setException(e);
        }
    }

    private static class PlainJsonResponse extends JsonResponse {

        private String json = "{}";

        public PlainJsonResponse(String json) throws IOException {
            if(json != null && !json.isEmpty()) {
                this.json = json;
            }
        }

        @Override
        public String getContent() throws IOException {
            return json;
        }
    }
}

