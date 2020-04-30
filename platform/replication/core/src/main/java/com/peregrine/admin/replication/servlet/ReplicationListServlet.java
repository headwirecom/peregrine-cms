package com.peregrine.admin.replication.servlet;

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

import com.peregrine.admin.replication.DefaultReplicationMapper;
import com.peregrine.replication.Replication;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.admin.replication.ReplicationConstants.RESOURCE_TYPE_LIST_REPLICATION;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet lists all available replication services
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 *
 * It is invoked like this:
 *      curl -X GET "http://localhost:8080/perapi/admin/listRepl.json" -H  "accept: application/json"
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Replication Lister Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_LIST_REPLICATION
    }
)
@SuppressWarnings("serial")
public class ReplicationListServlet extends AbstractBaseServlet {

    public static final String REPLICATION_SERVICES = "replicationServices";
    public static final String DESCRIPTION = "description";

    private Map<String, Replication> replications = new HashMap<>();

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    public void bindReplication(Replication replication) {
        String replicationName = replication.getName();
        logger.trace("Register replication with name: '{}': {}", replicationName, replication);
        if(replicationName != null && !replicationName.isEmpty()) {
            replications.put(replicationName, replication);
        } else {
            logger.error("Replication: '{}' does not provide an operation name -> binding is ignored", replication);
        }
    }

    @SuppressWarnings("unused")
    public void unbindReplication(Replication replication) {
        String replicationName = replication.getName();
        if(replications.containsKey(replicationName)) {
            replications.remove(replicationName);
        } else {
            logger.error("Replication: '{}' is not register with operation name: '{}' -> unbinding is ignored", replication, replicationName);
        }
    }

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    @SuppressWarnings("unused")
    public void bindDefaultReplicationMapper(DefaultReplicationMapper defaultReplicationMapper) {
        logger.trace("Register Default Replication Mapper: '{}'", defaultReplicationMapper.getName());
        bindReplication(defaultReplicationMapper);
    }

    @SuppressWarnings("unused")
    public void unbindDefaultReplicationMapper(DefaultReplicationMapper defaultReplicationMapper) {
        logger.trace("UnRegister Default Replication Mapper: '{}'", defaultReplicationMapper.getName());
        unbindReplication(defaultReplicationMapper);
    }

    @Override
    protected Response handleRequest(Request request) throws IOException {
        JsonResponse answer;
        answer = new JsonResponse();
        answer.writeArray(REPLICATION_SERVICES);
        for(Replication replication: replications.values()) {
            answer.writeObject();
            answer.writeAttribute(NAME, replication.getName());
            answer.writeAttribute(DESCRIPTION, replication.getDescription());
            answer.writeClose();
        }
        answer.writeClose();
        return answer;
    }
}

