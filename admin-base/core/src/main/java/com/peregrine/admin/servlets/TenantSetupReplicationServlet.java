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

import com.peregrine.admin.resource.AdminResourceHandler;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.replication.Replication;
import com.peregrine.replication.Replication.ReplicationException;
import com.peregrine.replication.ReplicationUtil;
import com.peregrine.replication.ReplicationsContainerWithDefault;
import com.peregrine.sitemap.SiteMapFilesCache;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_TENANT_SETUP_REPLICATION;

import static com.peregrine.commons.util.PerConstants.FELIBS_ROOT;
import static com.peregrine.commons.util.PerConstants.PAGES;
import static com.peregrine.commons.util.PerConstants.SITE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
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
 *      curl -X POST "http://localhost:8080/perapi/admin/tenantSetupReplication.json/content/themeclean" -H\
 *      "accept: application/json" -H  "content-type: application/x-www-form-urlencoded" -d "name=defaultRepl&deep=false"
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
public final class TenantSetupReplicationServlet extends ReplicationServletBase {

    private static final String SOURCE_SITE = "sourceSite";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SimpleDateFormat dateLabelFormat = new SimpleDateFormat("yyyy-MM-dd@HH.mm.ss");

    @Reference
    private ReplicationsContainerWithDefault replications;

    @Reference
    private AdminResourceHandler resourceManagement;

    @Reference
    private SiteMapFilesCache siteMapFilesCache;

    protected ReplicationsContainerWithDefault getReplications() {
        return replications;
    }

    @Override
    protected Response performReplication(
            final Replication replication,
            final Request request,
            final Resource site,
            final ResourceResolver resourceResolver
    ) throws IOException, ReplicationException {
        final String path = site.getPath();
        // Make sure that the Resource is a Site
        if (!SITE_PRIMARY_TYPE.equals(site.getResourceType())) {
            return badRequest(String.format("Suffix: '%s' is not a Peregrine Site", path));
        }

        final PerUtil.ResourceChecker tenantChecker = new ReplicationUtil.TenantOwnedResourceChecker(site);
        final var toBeReplicatedInitial = extractSiteFeLibs(site, resourceResolver.getResource(FELIBS_ROOT), tenantChecker);
        toBeReplicatedInitial.add(0, site);
        logger.trace("List of Resource to be replicated: '{}'", toBeReplicatedInitial);
        List<Resource> toBeReplicated = new LinkedList<>(toBeReplicatedInitial);
        for (final Resource resource : toBeReplicatedInitial) {
            try {
                logger.trace("Replication Resource: '{}'", resource);
                toBeReplicated.addAll(replication.findReferences(resource, true, tenantChecker));
            } catch (final ReplicationException e) {
                logger.warn("Replication Failed", e);
                return badRequestReplicationFailed(e);
            }
        }

        final String dateLabel = site.getName() + "_" + dateLabelFormat.format(new Date(System.currentTimeMillis()));
        toBeReplicated = replication.prepare(toBeReplicated);
        streamReplicableResources(toBeReplicated)
                .map(Resource::getPath)
                .forEach(p -> {
                    try {
                        resourceManagement.createVersion(resourceResolver, p, dateLabel, PerConstants.PUBLISHED_LABEL);
                    } catch (final AdminResourceHandler.ManagementException e) {
                        logger.trace("Unable to create a version for path: {} ", p, e);
                    }
                });
        final var replicatedStuff = replication.replicate(toBeReplicated);
        siteMapFilesCache.build(path + SLASH + PAGES);
        return prepareResponse(site, replicatedStuff);
    }

    /**
     * Things to search for
     * - /content/<site>/pages/css
     * - /etc/felibs/<site>.(css|js) **/
    private List<Resource> extractSiteFeLibs(final Resource site, final Resource feLibsRoot, final PerUtil.ResourceChecker checker) {
        final List<Resource> result = new LinkedList<>();
        logger.trace("Source Site: '{}'", site);
        final String siteName = site.getName();
        final Resource siteFeLibs = feLibsRoot.getChild(siteName);
        logger.trace("Source FeLibs: '{}', Site Name: '{}'", siteFeLibs, siteName);
        if (checker.doAdd(siteFeLibs)) {
            logger.trace("Add Site FE libs: '{}'", siteFeLibs);
            result.add(siteFeLibs);
        }

        final Resource parentSite = Optional.of(site)
                .map(Resource::getValueMap)
                .map(props -> props.get(SOURCE_SITE, String.class))
                .map(site.getParent()::getChild)
                .orElse(null);
        logger.trace("Parent Source Resource: '{}'", parentSite);
        if (checker.doAdd(parentSite)) {
            logger.trace("Add Site Parent: '{}'", parentSite);
            result.add(parentSite);
            result.addAll(extractSiteFeLibs(parentSite, feLibsRoot, checker));
        }

        return result;
    }

}
