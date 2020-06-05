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
import com.peregrine.intra.IntraSlingCaller;
import com.peregrine.intra.IntraSlingCaller.CallException;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_BACKUP_TENANT;
import static com.peregrine.admin.util.AdminConstants.BACKUP_FORMAT;
import static com.peregrine.admin.util.AdminConstants.EVENT_FINISHED_STATE;
import static com.peregrine.admin.util.AdminConstants.JOBS_PATH;
import static com.peregrine.admin.util.AdminConstants.JOB_STATE_SUCCEEDED;
import static com.peregrine.admin.util.AdminConstants.OPERATION_ASSEMBLE;
import static com.peregrine.admin.util.AdminConstants.PACKAGE_FORMAT;
import static com.peregrine.admin.util.AdminConstants.PEREGRINE_SERVICE_NAME;
import static com.peregrine.admin.util.AdminConstants.REFERENCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SLING_EVENT_CREATED;
import static com.peregrine.admin.util.AdminConstants.SLING_EVENT_ID;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.extractName;
import static com.peregrine.commons.util.PerUtil.loginService;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * This servlet backs up a Tenant Site and provides Information about
 * the latest backup
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definitions/admin.yaml
 *
 * It is invoked like this:
 *      curl -X GET "http://localhost:8080/perapi/admin/backupTenant.json/<tenant name>"
 *      curl -X POST "http://localhost:8080/perapi/admin/backupTenant.json/<tenant name>"
 */

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Tenant Backup",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_METHODS + EQUALS + POST,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_BACKUP_TENANT
    }
)
@SuppressWarnings("serial")
public class BackupTenantServlet extends AbstractPackageServlet {

    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Reference
    @SuppressWarnings("unused")
    private IntraSlingCaller intraSlingCaller;

    @Reference
    @SuppressWarnings("unused")
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    Packaging getPackaging() { return null; }

    @Reference
    private JobManager jobManager;

    JobManager getJobManager() { return jobManager; }

    @Override
    protected Response handleRequest(Request request) throws IOException {
        ResourceResolver tester = null;
        try {
            ResourceResolver resourceResolver = request.getResourceResolver();
            String tenantPath = request.getParameter(PATH);
            String tenantName = extractName(tenantPath);
            logger.debug("Backup Tenant: '{}'", tenantName);
            if(request.isPost()) {
                // Execute the build of the Tenant Package
                JsonNode jobDetails = executeJob(
                    request.getResourceResolver(),
                    request.getParameters(),
                    tenantName,
                    OPERATION_ASSEMBLE
                );
                // Check the progress
                String eventId = "";
                if (jobDetails.has(SLING_EVENT_ID)) {
                    eventId = jobDetails.get("slingevent:eventId").textValue();
                } else {
                    return new JsonResponse()
                        .writeAttribute("tenant", tenantName)
                        .writeAttribute("action", "backup")
                        .writeAttribute("message", "backup job not created properly")
                        .writeAttribute("outcome", "failed");
                }
                logger.debug("Build Package Job Details: '{}', Event Id: '{}'", jobDetails, eventId);
                int count = 0;
                String eventFinalState = "";
                JsonNode jobUpdate = null;
                while (count < 20) {
                    count++;
                    logger.trace("Check Round: '{}'", count);
                    // Using the user's resource resolver is failing even though /var is readable to everyone -> use Peregrine Service User instead
                    tester = loginService(resourceResolverFactory, PEREGRINE_SERVICE_NAME);
                    jobUpdate = getJobById(getJobManager(), tester, eventId);
                    logger.trace("Check Details: '{}'", jobUpdate);
                    if (jobUpdate != null && jobUpdate.has(EVENT_FINISHED_STATE)) {
                        eventFinalState = jobUpdate.get(EVENT_FINISHED_STATE).textValue();
                        if (JOB_STATE_SUCCEEDED.equals(eventFinalState)) {
                            logger.trace("Package Build successfully finished");
                            break;
                        } else {
                            return new JsonResponse()
                                .writeAttribute("tenant", tenantName)
                                .writeAttribute("action", "backup")
                                .writeAttribute("path", jobUpdate.get(REFERENCE_NAME).textValue())
                                .writeAttribute("state", eventFinalState)
                                .writeAttribute("last", jobUpdate.get(SLING_EVENT_CREATED).textValue())
                                .writeAttribute("outcome", "failed");
                        }
                    }
                    logger.trace("Wait 5s and then try once more");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                if (eventFinalState.isEmpty()) {
                    return new ErrorResponse().setErrorMessage("Build did not finish in time");
                }
                logger.debug("Build finished: '{}'", jobUpdate);
                // Execute the download of the Package
                return new JsonResponse()
                    .writeAttribute("tenant", tenantName)
                    .writeAttribute("action", "backup")
                    .writeAttribute("path", jobUpdate.get(REFERENCE_NAME).textValue())
                    .writeAttribute("state", jobUpdate.get(EVENT_FINISHED_STATE).textValue())
                    .writeAttribute("last", jobUpdate.get(SLING_EVENT_CREATED).textValue())
                    .writeAttribute("outcome", "success");
            } else if(request.isGet()) {
                // Get the Node of /var/audit/jobs
                Resource jobs = resourceResolver.getResource(JOBS_PATH);
                logger.trace("Backup Audit Jobs: '{}'", jobs);
                if(jobs != null) {
                    // Loop over the Package Job Executor Nodes
                    Resource backup = null;
                    for (Resource child : jobs.getChildren()) {
                        // Find the Backup Entry
                        backup = child.getChild(String.format(BACKUP_FORMAT, tenantName));
                        if (backup != null) {
                            break;
                        }
                    }
                    logger.debug("Backup Audit Job: '{}'", backup);
                    if (backup != null) {
                        // Find newest node
                        Resource details = null;
                        Calendar latest = Calendar.getInstance();
                        latest.setTimeInMillis((new Date(0)).getTime());
                        for (Resource child : backup.getChildren()) {
                            Calendar temp = child.getValueMap().get(SLING_EVENT_CREATED, latest);
                            if (latest.getTimeInMillis() < temp.getTimeInMillis()) {
                                details = child;
                                latest = temp;
                            }
                        }
                        if (details != null) {
                            ValueMap properties = details.getValueMap();
                            return new JsonResponse()
                                .writeAttribute("tenant", tenantName)
                                .writeAttribute("action", "info")
                                .writeAttribute("path", properties.get(REFERENCE_NAME, String.format(PACKAGE_FORMAT, tenantName)))
                                .writeAttribute("state", properties.get(EVENT_FINISHED_STATE, "FAILED"))
                                .writeAttribute("last", dateFormatter.format(latest.getTime()))
                                .writeAttribute("outcome", "success");
                        } else {
                            return new JsonResponse()
                                .writeAttribute("tenant", tenantName)
                                .writeAttribute("action", "info")
                                .writeAttribute("message", "no details")
                                .writeAttribute("outcome", "failed");
                        }
                    } else {
                        return new JsonResponse()
                            .writeAttribute("tenant", tenantName)
                            .writeAttribute("action", "info")
                            .writeAttribute("message", "no backup")
                            .writeAttribute("outcome", "failed");
                    }
                } else {
                    return new JsonResponse()
                        .writeAttribute("tenant", tenantName)
                        .writeAttribute("action", "info")
                        .writeAttribute("message", "no audit jobs")
                        .writeAttribute("outcome", "failed");
                }
            } else {
                return new JsonResponse()
                    .writeAttribute("tenant", tenantName)
                    .writeAttribute("message", "unsupported request")
                    .writeAttribute("outcome", "failed");
            }
        } catch (CallException e) {
            logger.warn("Build Failure", e);
            return new ErrorResponse().setErrorMessage("Build Failed").setException(e);
        } catch (LoginException e) {
            logger.warn("Cannot login to Peregrine Service User", e);
            return new ErrorResponse().setErrorMessage("Cannot login to Peregrine Service User").setException(e);
        } finally {
            if(tester != null) {
                tester.close();
            }
        }
    }
}
