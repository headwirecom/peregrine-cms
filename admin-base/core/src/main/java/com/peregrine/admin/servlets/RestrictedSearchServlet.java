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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_SEARCH;
import static com.peregrine.admin.util.AdminConstants.CURRENT;
import static com.peregrine.admin.util.AdminConstants.DATA;
import static com.peregrine.admin.util.AdminConstants.MORE;
import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.COMPONENTS;
import static com.peregrine.commons.util.PerConstants.COMPONENT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.NODE_TYPE;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_SUPER_TYPE;
import static com.peregrine.commons.util.PerConstants.TEMPLATES;
import static com.peregrine.commons.util.PerConstants.TITLE;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerConstants.VARIATION;
import static com.peregrine.commons.util.PerConstants.VARIATIONS;
import static com.peregrine.commons.util.PerConstants.VARIATION_PATH;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.admin.resource.TenantAppsResourceProviderManager;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Limited Search of either Peregrine:
 * - Components
 * - Templates
 * - Objects
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Search Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_SEARCH
    }
)
@SuppressWarnings("serial")
public class RestrictedSearchServlet extends AbstractBaseServlet {

    private static final long ROWS_PER_PAGE = 1000;
    public static final String UNKNOWN_TYPE = "Unknown Type: ";
    public static final String UNABLE_TO_GET_QUERY_MANAGER = "Unable to get query manager";
    public static final String GROUP = "group";
    public static final String TEMPLATE_COMPONENT = "templateComponent";
    public static final String THUMBNAIL_PNG = "thumbnail.png";
    public static final String THUMBNAIL_SAMPLE_PNG = "thumbnail-sample.png";
    public static final String THUMBNAIL = "thumbnail";

    @Reference
    private TenantAppsResourceProviderManager tenantAppsResourceProviderManager;

    @Override
    protected Response handleRequest(Request request) throws IOException {
        // Path / Suffix is obtained but not used ?
        String path = request.getParameter(PATH);
        Resource res = request.getResource();
        String type = res.getValueMap().get(TYPE, String.class);
        Response answer;
        if(COMPONENTS.equals(type)) {
            answer = findComponents(request);
        } else if(TEMPLATES.equals(type)) {
            answer = findTemplates(request);
        } else if(OBJECTS.equals(type)) {
            answer = findObjects(request);
        } else {
            answer = new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(UNKNOWN_TYPE + type);
        }
        return answer;
    }

    private Response findObjects(Request request) throws IOException {
        String query = "select * from per:ObjectDefinition order by jcr:path";
        return findAndOutputToWriterAsJSON(request, query);
    }

    private Response findComponents(Request request) throws IOException {
        String query = "select * from per:Component order by jcr:path";
        List<String> tenants = tenantAppsResourceProviderManager.getListOfTenants();
        List<String> tenantPaths = new ArrayList<>(tenants.size());
        for(String tenant: tenants) {
            tenantPaths.add(APPS_ROOT + "/" + tenant);
        }
        return findAndOutputToWriterAsJSON(request, query, tenantPaths, COMPONENT_PRIMARY_TYPE);
    }

    private Response findTemplates(Request request) throws IOException {
        String query = "select * from per:Page where jcr:path like '/content/%/templates%' order by jcr:path";
        return findAndOutputToWriterAsJSON(request, query);
    }

    private Response findAndOutputToWriterAsJSON(Request request, String query) throws IOException {
        return findAndOutputToWriterAsJSON(request, query, null, null);
    }

    private Response findAndOutputToWriterAsJSON(Request request, String query, List<String> additionalResources, String resourceType) throws IOException {
        JsonResponse answer = new JsonResponse();
        if(query.length() == 0) {
            answer
                .writeAttribute(CURRENT, 1)
                .writeAttribute(MORE, false)
                .writeArray(DATA)
                .writeClose();
        } else {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            try {
                if (query != null && query.trim().length() > 0) {
                    QueryManager qm = session.getWorkspace().getQueryManager();
                    Query q = qm.createQuery(query, Query.SQL);
                    q.setLimit(ROWS_PER_PAGE+1);
                    String pageParam = request.getParameter("page");
                    int page = 0;
                    if(pageParam != null) {
                        page = Integer.parseInt(pageParam);
                    }
                    q.setOffset(page*ROWS_PER_PAGE);

                    QueryResult res = q.execute();
                    NodeIterator nodes = res.getNodes();
                    answer.writeAttribute(CURRENT, 1);
                    answer.writeAttribute(MORE, nodes.getSize() > ROWS_PER_PAGE);
                    answer.writeArray(DATA);
                    while(nodes.hasNext()) {
                        Node node = nodes.nextNode();
                        writeComponentsAsJSON(request.getResourceResolver(), node, answer);
                    }
                    if(additionalResources != null) {
                        for (String additionalResource : additionalResources) {
                            Resource resource = request.getResourceResolver().getResource(additionalResource);
                            if(resource != null) {
                                List<Resource> resourceList = new ArrayList<>();
                                getResourceRecursively(resource, resourceType, resourceList);
                                for(Resource component: resourceList) {
                                    writeComponentsAsJSON(component, answer);
                                }
                            }
                        }
                    }
                    answer.writeClose();
                }
            } catch(Exception e) {
                answer = new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage(UNABLE_TO_GET_QUERY_MANAGER).setException(e);
            }
        }
        return answer;
    }

    private void writeComponentsAsJSON(ResourceResolver resourceResolver, Node node, JsonResponse answer) throws RepositoryException, IOException {
        Resource resource = resourceResolver.getResource(node.getPath());
        writeComponentsAsJSON(resource, answer);
    }

    private void writeComponentsAsJSON(Resource resource, JsonResponse answer) throws IOException {
        ValueMap properties = resource.getValueMap();
        if(COMPONENT_PRIMARY_TYPE.equals(properties.get(JCR_PRIMARY_TYPE, String.class))) {
            // Check if this component supports variations and if then loop over all child nodes
            // and add each one of them to the result set
            boolean done = false;
            Resource jcrContent = findChildResourceRecursive(resource, JCR_CONTENT);
            if(jcrContent != null) {
                boolean isVariations = jcrContent.getValueMap().get(VARIATIONS, false);
                if(isVariations) {
                    Iterator<Resource> i = jcrContent.listChildren();
                    while(i.hasNext()) {
                        Resource variation = i.next();
                        writeComponentResource(resource, variation, answer);
                        done = true;
                    }
                }
            }
            if(!done) {
                writeComponentResource(resource, null, answer);
            }
        } else {
            answer.writeObject();
            answer.writeAttribute(NAME, resource.getName());
            String title = resource.getValueMap().get(JCR_TITLE, String.class);
            if(title != null) {
                answer.writeAttribute(TITLE, title);
            }
            answer.writeAttribute(PATH, resource.getPath());
            answer.writeAttribute(NODE_TYPE, properties.get(JCR_PRIMARY_TYPE, ""));
            answer.writeClose();
        }
    }

    private Resource findChildResourceRecursive(Resource resource, String nodeName) {
        Resource answer = resource.getChild(nodeName);
        if(answer == null) {
            // Loop for a sling:resourceSuperType and copy this one in instead
            Resource superTypeResource = resource;
            List<String> alreadyVisitedNodes = new ArrayList<>();
            while(true) {
                // If we already visited that node then exit to avoid an endless loop
                if(alreadyVisitedNodes.contains(superTypeResource.getPath())) { break; }
                alreadyVisitedNodes.add(superTypeResource.getPath());
                ValueMap properties = superTypeResource.getValueMap();
                if(properties.containsKey(SLING_RESOURCE_SUPER_TYPE)) {
                    String resourceSuperType = properties.get(SLING_RESOURCE_SUPER_TYPE, String.class);
                    if(isNotEmpty(resourceSuperType)) {
                        superTypeResource = superTypeResource.getResourceResolver().getResource(APPS_ROOT + SLASH + resourceSuperType);
                        if(superTypeResource != null) {
                            logger.trace("Found Resource Super Type: '{}'", superTypeResource.getPath());
                            // If we find the JCR Content then we are done here otherwise try to find this one's super resource type
                            Resource temp = superTypeResource.getChild(nodeName);
                            if(temp != null) {
                                answer = temp;
                                logger.trace("Found Content Node of Super Resource Type: '{}': '{}'", superTypeResource.getPath(), answer.getPath());
                                break;
                            }
                        }
                    } else {
                        logger.warn("Could not find Resource Super Type Component: " + APPS_ROOT + SLASH + resourceSuperType + " -> ignore component");
                    }
                }
            }
        }
        return answer;
    }

    private void writeComponentResource(Resource component, Resource variation, JsonResponse answer) throws IOException {
        answer.writeObject();
        answer.writeAttribute(NAME, component.getName());
        answer.writeAttribute(PATH, component.getPath());
        String group = null;
        String title = null;
        if(variation != null) {
            String name = variation.getName();
            answer.writeAttribute(VARIATION, name);
            answer.writeAttribute(VARIATION_PATH, variation.getPath());
            ValueMap properties = variation.getValueMap();
            if(properties.containsKey(TITLE)) {
                title = properties.get(TITLE, String.class);
            }
            if(properties.containsKey(GROUP)) {
                group = properties.get(GROUP, String.class);
            }
        }
        ValueMap properties = component.getValueMap();
        if(isEmpty(title) && properties.containsKey(JCR_TITLE)) {
            title = properties.get(JCR_TITLE, String.class);
        }
        if(isEmpty(group) && properties.containsKey(GROUP)) {
            group = properties.get(GROUP, String.class);
        }
        if(isNotEmpty(title)) {
            answer.writeAttribute(TITLE, title);
        }
        if(isNotEmpty(group)) {
            answer.writeAttribute(GROUP, group);
        }
        if(properties.containsKey(TEMPLATE_COMPONENT)) {
            boolean isTemplateComponent = properties.get(TEMPLATE_COMPONENT, Boolean.class);
            answer.writeAttribute(TEMPLATE_COMPONENT, isTemplateComponent);
        }
        if(variation == null) {
            Resource resource = findChildResourceRecursive(component, THUMBNAIL_PNG);
            if(resource != null) {
                answer.writeAttribute(THUMBNAIL, resource.getPath());
            } else {
                resource = findChildResourceRecursive(component, THUMBNAIL_SAMPLE_PNG);
                if(resource != null) {
                    answer.writeAttribute(THUMBNAIL, resource.getPath());
                }
            }
        } else {
            String thumbnailName = THUMBNAIL + "-" + variation.getName().toLowerCase()+".png";
            Resource resource = findChildResourceRecursive(component, thumbnailName);
            if(resource != null) {
                answer.writeAttribute(THUMBNAIL, resource.getPath());
            }
        }
        answer.writeAttribute(NODE_TYPE, component.getResourceType());
        answer.writeClose();
    }

    private void getResourceRecursively(Resource resource, String resourceType, List<Resource> answer) {
        if(resource.getResourceType().equals(resourceType)) {
            answer.add(resource);
        }
        Iterator<Resource> i = resource.listChildren();
        while(i.hasNext()) {
            Resource child = i.next();
            getResourceRecursively(child, resourceType, answer);
        }
    }
}

