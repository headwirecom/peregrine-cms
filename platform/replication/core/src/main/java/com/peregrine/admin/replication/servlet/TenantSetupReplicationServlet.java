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
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.replication.Replication.ReplicationException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.peregrine.admin.replication.ReplicationConstants.RESOURCE_TYPE_TENANT_SETUP_REPLICATION;
import static com.peregrine.admin.replication.ReplicationConstants.SOURCE_NAME;
import static com.peregrine.admin.replication.ReplicationConstants.SOURCE_PATH;
import static com.peregrine.admin.replication.servlet.ReplicationServlet.REPLICATES;
import static com.peregrine.admin.replication.servlet.ReplicationServlet.REPLICATION_FAILED;
import static com.peregrine.admin.replication.servlet.ReplicationServlet.SUFFIX_IS_NOT_RESOURCE;
import static com.peregrine.commons.util.PerConstants.FELIBS_ROOT;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SITE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet replicates the necessary resources for a replicated site to work
 * and if desired replicates also the given site afterwards
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 *
 * It is invoked like this:
 *      curl -X POST "http://localhost:8080/perapi/admin/tenantSetupReplication.json/content/themeclean" -H  "accept: application/json" -H  "content-type: application/x-www-form-urlencoded" -d "name=defaultRepl&deep=false"
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Site Setup Replication Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_TENANT_SETUP_REPLICATION
    }
)
@SuppressWarnings("serial")
public class TenantSetupReplicationServlet extends AbstractBaseServlet {

    public static final String SUFFIX_IS_NOT_SITE = "Suffix: '%s' is not a Peregrine Site";
    public static final String WITH_SITE = "withSite";

    private static final String SOURCE_SITE = "sourceSite";
    private static final List<String> EXCLUDED_RESOURCES = new ArrayList<>();

    static {
        EXCLUDED_RESOURCES.add(JCR_CONTENT);
    }

    @Reference
    DefaultReplicationMapper defaultReplicationMapper;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        JsonResponse answer = null;
        logger.trace("Request Path: '{}'", request.getRequestPath());
        logger.trace("Request URI: '{}'", request.getRequest().getRequestURI());
        logger.trace("Request URL: '{}'", request.getRequest().getRequestURL());
        String sourcePath = request.getParameter(PATH);
//        String replicationName = request.getPa
//        if(replicationName == null || replicationName.isEmpty()) {
//            return new ErrorResponse()
//                .setHttpErrorCode(SC_BAD_REQUEST)
//                .setErrorMessage(PARAMETER_NAME_FOR_THE_REPLICATION_NAME_IS_NOT_PROVIDED);
//        }
        String withSiteParameter = request.getParameter(WITH_SITE);
        boolean withSite = withSiteParameter != null && Boolean.TRUE.toString().equals(withSiteParameter.toLowerCase());
//        String deactivateParameter = request.getParameter(DEACTIVATE);
//        boolean deactivate = deactivateParameter != null && Boolean.TRUE.toString().equals(deactivateParameter.toLowerCase());
//        Replication replication = replications.get(replicationName);
//        if(replication == null) {
//            return new ErrorResponse()
//                .setHttpErrorCode(SC_BAD_REQUEST)
//                .setErrorMessage(REPLICATION_NOT_FOUND_FOR_NAME + replicationName);
//        }
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            // Make sure that the Resource is a Site
            if(!source.getResourceType().equals(SITE_PRIMARY_TYPE)) {
                return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(String.format(SUFFIX_IS_NOT_SITE, sourcePath));
            }
            List<Resource> replicationList = new ArrayList<>();
            // Thinks to search for
            // - /content/<site>/pages/css
            // - /etc/felibs/<theme>.css / .js
            // - /etc/felibs/<site>.css / .js
            // - /etc/felibs/<theme dependencies>.css / .js
//            if(withSite) {
                replicationList.add(source);
//            } else {
//                Resource css = source.getChild("pages/css");
//                if (css != null) {
//                    replicationList.add(css);
//                }
//            }
            Resource felibs = request.getResourceResolver().getResource(FELIBS_ROOT);
            handleSourceSites(source, replicationList, felibs);
            logger.info("List of Resource to be replicated: '{}'", replicationList);
            List<Resource> allReplicatedResource = new ArrayList<>();
            for(Resource resource: replicationList) {
                try {
                    logger.info("Replication Resource: '{}'", resource);
                    List<Resource> replicatedItems = defaultReplicationMapper.replicate(resource, true);
                    allReplicatedResource.addAll(replicatedItems);
                } catch (ReplicationException e) {
                    logger.warn("Replication Failed", e);
                    return new ErrorResponse()
                        .setHttpErrorCode(SC_BAD_REQUEST)
                        .setErrorMessage(REPLICATION_FAILED)
                        .setException(e);
                }
            }
            answer = new JsonResponse();
            answer.writeAttribute(SOURCE_NAME, source.getName());
            answer.writeAttribute(SOURCE_PATH, source.getPath());
            answer.writeArray(REPLICATES);
            if(allReplicatedResource != null) {
                for(Resource child : allReplicatedResource) {
                    answer.writeObject();
                    answer.writeAttribute(NAME, child.getName());
                    answer.writeAttribute(PATH, child.getPath());
                    answer.writeClose();
                }
            }
            answer.writeClose();
        } else {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(String.format(SUFFIX_IS_NOT_RESOURCE, sourcePath));
        }
        return answer;
    }

    private void handleSourceSites(Resource sourceSite, List<Resource> replicationList, Resource felibs) {
        logger.info("Source Site: '{}'", sourceSite);
        String siteName = sourceSite.getName();
        Resource siteFeLibs = felibs.getChild(siteName);
        logger.info("Source FeLibs: '{}', Site Name: '{}'", siteFeLibs, siteName);
        if (siteFeLibs != null) {
            logger.info("Add Site Felibs: '{}'", siteFeLibs);
            replicationList.add(siteFeLibs);
        }
        String parentSourceSiteName = sourceSite.getValueMap().get(SOURCE_SITE, String.class);
        logger.info("Parent Source Site Name: '{}'", parentSourceSiteName);
        Resource parentSourceSite = sourceSite.getParent().getChild(parentSourceSiteName);
        logger.info("Parent Source Resource: '{}'", parentSourceSite);
        if(parentSourceSite != null) {
            logger.info("Add Site Parent: '{}'", parentSourceSite);
            replicationList.add(parentSourceSite);
            handleSourceSites(parentSourceSite, replicationList, felibs);
        }
    }
}
