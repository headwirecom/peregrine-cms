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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_TENANTS;
import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.ASSETS_ROOT;
import static com.peregrine.commons.util.PerConstants.CONTENT_ROOT;
import static com.peregrine.commons.util.PerConstants.FELIBS_ROOT;
import static com.peregrine.commons.util.PerConstants.INTERNAL;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.OBJECTS_ROOT;
import static com.peregrine.commons.util.PerConstants.PAGES_ROOT;
import static com.peregrine.commons.util.PerConstants.SITE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.TEMPLATES_ROOT;
import static com.peregrine.commons.util.PerConstants.TENANT;
import static com.peregrine.commons.util.PerConstants.TITLE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.google.common.collect.ImmutableSortedMap;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a list of the tenants (top-level sites) on this Peregrine instance.
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "List Tenants Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_LIST_TENANTS,
        SLING_SERVLET_SELECTORS + EQUALS + JSON
    }
)
@SuppressWarnings("serial")
public class ListTenantsServlet extends AbstractBaseServlet {

    private static final String SITE_ROOT_MISSING = "The site root '" + PAGES_ROOT + "' did not resolve to a resource.";
    private static final String TENANTS = "tenants";
    private static final String ROOTS = "roots";

    private static final Map<String, String> ROOT_MAP = ImmutableSortedMap.<String, String>naturalOrder()
        .put("apps", APPS_ROOT + SLASH + TENANT)
        .put("felibs", FELIBS_ROOT + SLASH + TENANT)
        .put("assets", ASSETS_ROOT)
        .put("objects", OBJECTS_ROOT)
        .put("pages", PAGES_ROOT)
        .put("templates", TEMPLATES_ROOT)
        .build();

    @Override
    protected Response handleRequest(Request request) throws IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();

        Resource sitesRoot = resourceResolver.getResource(CONTENT_ROOT);
        if (sitesRoot == null) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(SITE_ROOT_MISSING);
        }

        Predicate<Resource> isTenant = resource -> {
            if (resource == null) return false;
            ValueMap properties = resource.getValueMap();
            String primaryType = properties.get(JCR_PRIMARY_TYPE, String.class);
            // boolean template = properties.get(TEMPLATE, false);
            // boolean internal = properties.get(INTERNAL, false);
            return SITE_PRIMARY_TYPE.equals(primaryType); //  && !template && !internal;
        };

        List<Resource> tenants = StreamSupport.stream(sitesRoot.getChildren().spliterator(), false)
            .filter(isTenant)
            .collect(Collectors.toList());

        JsonResponse answer = new JsonResponse();
        answer.writeArray(TENANTS);

        for (Resource tenant : tenants) {
            answer.writeObject();
            answer.writeAttribute(NAME, tenant.getName());
            answer.writeAttribute(TITLE, tenant.getValueMap().get(JCR_TITLE, String.class));
            answer.writeAttribute(TEMPLATE, tenant.getValueMap().get(TEMPLATE, Boolean.class));
            answer.writeAttribute(INTERNAL, tenant.getValueMap().get(INTERNAL, Boolean.class));
            answer.writeObject(ROOTS);
            for (String key : ROOT_MAP.keySet()) {
                answer.writeAttribute(key, ROOT_MAP.get(key).replace(TENANT, tenant.getName()));
            }
            answer.writeClose();
            answer.writeClose();
        }
        answer.writeClose();
        return answer;
    }
}
