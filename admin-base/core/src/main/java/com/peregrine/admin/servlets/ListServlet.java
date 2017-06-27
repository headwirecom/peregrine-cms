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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Collections;

import static com.peregrine.util.PerUtil.EQUALS;
import static com.peregrine.util.PerUtil.PER_PREFIX;
import static com.peregrine.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "List Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/list"
    }
)
@SuppressWarnings("serial")
public class ListServlet extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;


    @Override
    Response handleRequest(Request request) throws IOException {
        String path = request.getParameter("path");
        if(path == null || path.isEmpty()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("No suffix provided").setRequestPath(path);
        }
        Response answer;
        if("/tools".equals(path)) {
            answer = getJSONFromResource(request.getResource(), "/content/admin/tools");
        } else if("/tools/config".equals(path)) {
            answer = getJSONFromResource(request.getResource(), "/content/admin/toolsConfig");
        } else {
            answer = new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Unknown suffix: " + path);
        }
        return answer;
    }

    private Response getJSONFromResource(Resource resource, String resourcePath) throws IOException {
        Response answer;
        Resource res = resource.getResourceResolver().getResource(resourcePath);
        try {
            String out = modelFactory.exportModelForResource(res, "jackson", String.class, Collections.<String, String> emptyMap());
            answer = new PlainJsonResponse(out);
        } catch (ExportException e) {
            answer = new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Error while exporting model").setException(e);
        } catch (MissingExporterException e) {
            answer = new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("no exporter 'jackson' defined").setException(e);
        }
        return answer;
    }

    public static class PlainJsonResponse
        extends JsonResponse {

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

