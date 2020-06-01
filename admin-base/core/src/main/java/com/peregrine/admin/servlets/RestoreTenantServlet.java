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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.intra.IntraSlingCaller;
import com.peregrine.intra.IntraSlingCaller.CallException;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_BACKUP_TENANT;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_RESTORE_TENANT;
import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.loginService;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_POST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet restores a previously backed up Tenant Site
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 *
 * It is invoked like this:
 *      curl -X POST "http://localhost:8080/perapi/admin/restoreTenant.json/<tenant name>"
 */

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Tenant Restore",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_RESTORE_TENANT
    }
)
@SuppressWarnings("serial")
public class RestoreTenantServlet extends AbstractBaseServlet {

    private static final String JOB_CONTROL_PATH = "/bin/cpm/core/jobcontrol";
    private static final String JOB_CONTROL_SELECTOR = "job";
    private static final String JOB_TOPIC = "event.job.topic";
    private static final String JOB_TOPIC_NAME = "com/composum/sling/core/pckgmgr/PackageJobExecutor";
    private static final String REFERENCE_NAME = "reference";
    private static final String OPERATION_NAME = "operation";
    private static final String OPERATION_ASSEMBLE = "assemble";
    private static final String CHARSET_NAME = "_charset_";
    private static final String CHARSET = "UTF-8";
    private static final String TIMESTAMP = "_=%s";

    private static final String PACKAGE_FORMAT = "%1$s/%1$s-full-package-1.0.zip";

    private static final String SLING_EVENT_ID = "slingevent:eventId";
    private static final String JOB_STATE = "jobState";
    private static final String EVENT_FINISHED_STATE = "slingevent:finishedState";

    private static final String JOB_STATE_ACTIVE = "ACTIVE";
    private static final String JOB_STATE_SUCCEEDED = "SUCCEEDED";
    private static final String JOB_STATE_FAILED = "FAILED";

    @Reference
    @SuppressWarnings("unused")
    private IntraSlingCaller intraSlingCaller;

    @Reference
    @SuppressWarnings("unused")
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {
//        try {
//            ResourceResolver resourceResolver = request.getResourceResolver();
            String tenantPath = request.getParameter(PATH);
            String tenantName = extractTenantName(tenantPath);
//            // Execute the build of the Tenant Package
//            JsonNode jobDetails = buildPackage(resourceResolver, tenantName);
//            // Check the progress
//            String eventId = "";
//            logger.info("Build Package Job Details: '{}'", jobDetails);
//            if(jobDetails.has(SLING_EVENT_ID)) {
//                eventId = jobDetails.get("slingevent:eventId").textValue();
//            } else {
//                return new ErrorResponse().setErrorMessage("Backup Run did not yield an Event Id -> exit");
//            }
//            logger.info("Sling Event Id: '{}'",eventId);
//            int count = 0;
//            String eventFinalState = "";
//            JsonNode jobUpdate = null;
//            long timestamp = System.currentTimeMillis();
//            while(count < 20) {
//                count++;
//                logger.info("Check Round: '{}'", count);
//                // Using the user's resource resolver is failing even though /var is readable to everyone -> use Peregrine Service User instead
//                ResourceResolver tester = loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME);
//                jobUpdate = checkPackageJob(tester, eventId, timestamp++);
//                logger.info("Check Details: '{}'", jobUpdate);
//                if(jobUpdate != null && jobUpdate.has(EVENT_FINISHED_STATE)) {
//                    eventFinalState = jobUpdate.get(EVENT_FINISHED_STATE).textValue();
//                    if(JOB_STATE_SUCCEEDED.equals(eventFinalState)) {
//                        logger.info("Package Build successfully finished");
//                        break;
//                    } else {
//                        return new ErrorResponse().setErrorMessage("Build Failed with error state: " + eventFinalState);
//                    }
//                }
//                logger.info("Wait 5s and then try once more");
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    // Ignore
//                }
//            }
//            if(eventFinalState.isEmpty()) {
//                return new ErrorResponse().setErrorMessage("Build did not finish in time");
//            }
//            logger.info("Build finished: '{}'", jobUpdate);
            // Execute the download of the Package
            return new JsonResponse()
                .writeAttribute("tenant", tenantName)
                .writeAttribute("action", "restore")
                .writeAttribute("outcome", "success");
//        } catch (CallException e) {
//            logger.warn("Build Failure", e);
//            return new ErrorResponse().setErrorMessage("Build Failed").setException(e);
//        } catch (LoginException e) {
//            logger.warn("Build Failure 2", e);
//            return new ErrorResponse().setErrorMessage("Build Failed 2").setException(e);
//        }
    }

    private JsonNode buildPackage(ResourceResolver resourceResolver, String tenantName)
        throws CallException, IOException
    {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(JOB_TOPIC, JOB_TOPIC_NAME);
        parameters.put(REFERENCE_NAME, String.format(PACKAGE_FORMAT, tenantName));
        parameters.put(CHARSET_NAME, CHARSET);
        parameters.put(OPERATION_NAME, OPERATION_ASSEMBLE);
        byte[] response = intraSlingCaller.call(
            intraSlingCaller.createContext()
                .setResourceResolver(resourceResolver)
                .setMethod(METHOD_POST)
                .setPath(JOB_CONTROL_PATH)
                .setSelectors(JOB_CONTROL_SELECTOR)
                .setExtension(JSON)
                .setParameterMap(parameters)
        );
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response);
    }

    private JsonNode checkPackageJob(ResourceResolver resourceResolver, String slingEventId, long timestamp)
        throws CallException, IOException
    {
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("_", timestamp + "");
//        logger.info("Before start check package job, timestamp: '{}', event id: '{}'", timestamp, slingEventId);
//        final Iterator<Resource> resources = resourceResolver.findResources(
//            "/jcr:root/var/audit/jobs//*[slingevent:eventId='" + slingEventId + "']",
//            "xpath"
//        );
//        if(resources.hasNext()) {
//            logger.info("Found Job Audit: '{}'", resources.next());
//        }
        byte[] response = intraSlingCaller.call(
            intraSlingCaller.createContext()
                .setResourceResolver(resourceResolver)
                .setMethod(METHOD_GET)
                .setPath(JOB_CONTROL_PATH)
                .setSelectors(JOB_CONTROL_SELECTOR)
                .setExtension(JSON)
                .setSuffix(SLASH + slingEventId)
//                .setParameterMap(parameters)
        );
        logger.info("After start check package job, details: '{}'", new String(response));
        ObjectMapper objectMapper = new ObjectMapper();
        return response.length == 0 ?
            null : objectMapper.readTree(response);
    }

    private String extractTenantName(String tenantPath) {
        String answer = tenantPath;
        if(tenantPath != null && tenantPath.length() > 1) {
            while (tenantPath.length() > 0 && tenantPath.endsWith("/")) {
                tenantPath.substring(0, tenantPath.length() - 1);
            }
            if (tenantPath.length() > 1) {
                int index = tenantPath.lastIndexOf('/');
                if (index >= 0) {
                    answer = tenantPath.substring(index + 1);
                }
            }
        }
        return answer;
    }
}
