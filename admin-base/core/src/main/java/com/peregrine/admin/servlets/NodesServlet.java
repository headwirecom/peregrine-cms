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
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

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
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Nodes servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/nodes"
    }
)
@SuppressWarnings("serial")
public class NodesServlet extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Override
    Response handleRequest(Request request) throws IOException {
        String path = request.getParameter("path");
        if(path == null || path.isEmpty()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("No Path provided").setRequestPath(path);
        }
        String[] segments = path.split("/");
        logger.debug("lookup path {}, {}", path, segments.length);
        JsonResponse answer = new JsonResponse();
        convertResource(answer, request.getResourceResolver(), segments, 1, path);
        return answer;
    }

    private void convertResource(JsonResponse json, ResourceResolver rs, String[] segments, int pos, String fullPath) throws IOException {
        String path = "";
        for(int i = 1; i <= pos; i++) {
            path += "/" + segments[i];
        }
        logger.debug("looking up {}", path);
        Resource res = rs.getResource(path);
        json.writeAttribute("name",res.getName());
        json.writeAttribute("path",res.getPath());
        json.writeAttribute("resourceType",res.getValueMap().get("jcr:primaryType", String.class));
        json.writeArray("children");
        Iterable<Resource> children = res.getChildren();
        for(Resource child : children) {
            if(fullPath.startsWith(child.getPath())) {
                json.writeObject();
                convertResource(json, rs, segments, pos+1, fullPath);
                json.writeClose();
            } else {
                if(!"jcr:content".equals(child.getName())) {
                    json.writeObject();
                    json.writeAttribute("name",child.getName());
                    json.writeAttribute("path",child.getPath());
                    String resourceType = child.getValueMap().get("jcr:primaryType", String.class);
                    json.writeAttribute("resourceType", resourceType);
                    if("per:Asset".equals(resourceType)) {
                        String mimeType = child.getChild("jcr:content").getValueMap().get("jcr:mimeType", String.class);
                        json.writeAttribute("mimeType", mimeType);
                    }
                    if("per:Page".equals(resourceType)) {
                        String title = child.getChild("jcr:content").getValueMap().get("jcr:title", String.class);
                        json.writeAttribute("title", title);

                    }
                    json.writeClose();
                }
            }
        }
        json.writeClose();
    }
}

