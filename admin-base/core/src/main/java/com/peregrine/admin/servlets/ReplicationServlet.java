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

import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.admin.replication.Replication;
import com.peregrine.admin.replication.Replication.ReplicationException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.convertSuffixToParams;
import static com.peregrine.util.PerUtil.EQUALS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + "Peregrine: Replication Servlet",
        SERVICE_VENDOR + EQUALS + "headwire.com, Inc",
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/repl"
    }
)
@SuppressWarnings("serial")
/**
 * This servlet replicates the given resource with its JCR Content
 * ane any references
 *
 * It is invoked like this: curl -u admin:admin -X POST http://localhost:8080/api/admin/repl.json/path///content/sites/example//name//local
 */
public class ReplicationServlet extends SlingAllMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ReplicationServlet.class);

    @Reference
    private ReferenceLister referenceLister;
    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    private Collection<Replication> replications;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
        throws ServletException,
               IOException
    {
        Map<String, String> params = convertSuffixToParams(request);
        log.debug("Parameters from Suffix: '{}'", params);
        String sourcePath = params.get("path");
        String replicationName = params.get("name");
        if(replicationName == null || replicationName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Parameter 'name' for the replication name is not provided");
            return;
        }
        String deepParameter = params.get("deep");
        boolean deep = deepParameter != null && "true".equals(deepParameter.toLowerCase());
        Replication replication = null;
        for(Replication item: replications) {
            if(replicationName.equals(item.getName())) {
                replication = item;
                break;
            }
        }
        if(replication == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Replication not found for name: " + replicationName);
            return;
        }
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            response.setContentType("application/json");
            List<Resource> replicates = new ArrayList<>();
            try {
                replicates = replication.replicate(source, deep);
            } catch(ReplicationException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                e.printStackTrace(response.getWriter());
                replicates = null;
            }
            if(replicates != null) {
                StringBuffer answer = new StringBuffer();
                answer.append("{");
                answer.append("\"sourceName\":\"" + source.getName() + "\", ");
                answer.append("\"sourcePath\":\"" + source.getPath() + "\", ");
                answer.append("\"replicates\":[");
                boolean first = true;
                for(Resource child : replicates) {
                    if(first) {
                        first = false;
                    } else {
                        answer.append(", ");
                    }
                    answer.append("{\"name\":\"");
                    answer.append(child.getName());
                    answer.append("\", \"path\":\"");
                    answer.append(child.getPath());
                    answer.append("\"}");
                }
                answer.append("]}");
                String temp = answer.toString();
                log.debug("Answer: '{}'", temp);
                response.getWriter().write(temp);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Suffix: " + sourcePath + " is not a resource");
        }
    }
}

