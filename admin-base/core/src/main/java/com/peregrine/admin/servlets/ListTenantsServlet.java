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

import com.google.common.collect.ImmutableSortedMap;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.peregrine.admin.servlets.AdminPaths.JSON_EXTENSION;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_LIST_TENANTS;
import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.*;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "List Tenants Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_LIST_TENANTS,
        SLING_SERVLET_SELECTORS + EQUALS + JSON_EXTENSION
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides a list of the tenants (top-level sites) on this
 * peregrine instance.
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/api/definintions/admin.yaml
 */
public class ListTenantsServlet extends AbstractBaseServlet {

    private static final String SITE_ROOT_MISSING = "The site root '" + SITES_ROOT + "' did not resolve to a resource.";
    private static final String TENANTS = "tenants";
    public static final String ROOTS = "roots";

    private static final Map<String, String> ROOT_MAP = ImmutableSortedMap.<String, String>naturalOrder()
            .put("apps", APPS_ROOT)
            .put("assets", ASSETS_ROOT)
            .put("felibs", FELIBS_ROOT)
            .put("objects", OBJECTS_ROOT)
            .put("sites", SITES_ROOT)
            .put("templates", TEMPLATES_ROOT)
            .build();

    @Override
    protected Response handleRequest(Request request) throws IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource siteRoot = resourceResolver.getResource(SITES_ROOT);
        if(siteRoot == null) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(SITE_ROOT_MISSING);
        }

        Predicate<Resource> isPage = resource -> {
            if(resource == null) return false;
            ValueMap properties = resource.getValueMap();
            String primaryType = properties.get(JCR_PRIMARY_TYPE, String.class);
            return PAGE_PRIMARY_TYPE.equals(primaryType);
        };

        List<Resource> tenants = StreamSupport.stream(siteRoot.getChildren().spliterator(), false)
                .filter(isPage)
                .collect(Collectors.toList());

        JsonResponse answer = new JsonResponse();
        answer.writeArray(TENANTS);
        for(Resource tenant : tenants) {
            answer.writeObject();
            answer.writeAttribute(NAME, tenant.getName());

            Resource contentResource = tenant.getChild(JCR_CONTENT);
            if(contentResource != null) {
                ValueMap properties = contentResource.getValueMap();
                answer.writeAttribute(TITLE, properties.get(JCR_TITLE, String.class));
            }
            answer.writeObject(ROOTS);
            for(String key : ROOT_MAP.keySet()) {
                answer.writeAttribute(key, ROOT_MAP.get(key) + SLASH + tenant.getName());
            }
            answer.writeClose();

            answer.writeClose();
        }
        answer.writeClose();
        return answer;
    }
}

