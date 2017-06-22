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

import com.fasterxml.jackson.core.JsonGenerator;
import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.admin.replication.Replication;
import com.peregrine.admin.replication.Replication.ReplicationException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.util.PerUtil.EQUALS;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
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
public class ReplicationServlet extends AbstractBaseServlet {

    private final Logger log = LoggerFactory.getLogger(ReplicationServlet.class);

    @Reference
    private ReferenceLister referenceLister;

    private Map<String, Replication> replications = new HashMap<>();

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    public void bindImageTransformation(Replication replication) {
        String replicationName = replication.getName();
        if(replicationName != null && !replicationName.isEmpty()) {
            replications.put(replicationName, replication);
        } else {
            log.error("Replication: '{}' does not provide an operation name -> binding is ignored", replication);
        }
    }

    @SuppressWarnings("unused")
    public void unbindImageTransformation(Replication replication) {
        String replicationName = replication.getName();
        if(replications.containsKey(replicationName)) {
            replications.remove(replicationName);
        } else {
            log.error("Replication: '{}' is not register with operation name: '{}' -> unbinding is ignored", replication, replicationName);
        }
    }

    @Override
    Response handleRequest(Request request) throws IOException {
        String sourcePath = request.getParameter("path");
        String replicationName = request.getParameter("name");
        if(replicationName == null || replicationName.isEmpty()) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Parameter 'name' for the replication name is not provided");
        }
        String deepParameter = request.getParameter("deep");
        boolean deep = deepParameter != null && "true".equals(deepParameter.toLowerCase());
        Replication replication = replications.get(replicationName);
        if(replication == null) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Replication not found for name: " + replicationName);
        }
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            List<Resource> replicates;
            try {
                replicates = replication.replicate(source, deep);
            } catch(ReplicationException e) {
                return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Replication Failed").setException(e);
            }
            JsonResponse response = new JsonResponse();
            JsonGenerator json = response.getJson();
            json.writeStringField("sourceName", source.getName());
            json.writeStringField("sourcePath", source.getPath());
            json.writeArrayFieldStart("replicates");
            if(replicates != null) {
                for(Resource child : replicates) {
                    json.writeStartObject();
                    json.writeStringField("name", child.getName());
                    json.writeStringField("path", child.getPath());
                    json.writeEndObject();
                }
            }
            json.writeEndArray();
            return response;
        } else {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Suffix: " + sourcePath + " is not a resource");
        }
    }
}

