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

import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import java.util.ArrayList;
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
import org.osgi.service.component.annotations.Component;

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
        return findAndOutputToWriterAsJSON(request, query);
    }

    private Response findTemplates(Request request) throws IOException {
        String query = "select * from per:Page where jcr:path like '/content/%/templates%' order by jcr:path";
        return findAndOutputToWriterAsJSON(request, query);
    }

    private Response findAndOutputToWriterAsJSON(Request request, String query) throws IOException {
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
                        if(node.getPrimaryNodeType().toString().equals(COMPONENT_PRIMARY_TYPE)) {
                            // Check if this component supports variations and if then loop over all child nodes
                            // and add each one of them to the result set
                            boolean done = false;
                            Node jcrContent = findChildNodeRecursive(node, JCR_CONTENT);
                            if(jcrContent != null && jcrContent.hasProperty(VARIATIONS)) {
                                boolean isVariations = jcrContent.getProperty(VARIATIONS).getBoolean();
                                if(isVariations) {
                                    NodeIterator variations = jcrContent.getNodes();
                                    while(variations.hasNext()) {
                                        Node variation = variations.nextNode();
                                        writeComponentNode(node, variation, answer);
                                        done = true;
                                    }
                                }
                            }
                            if(!done) {
                                writeComponentNode(node, null, answer);
                            }
                        } else {
                            answer.writeObject();
                            answer.writeAttribute(NAME, node.getName());
                            if(node.hasProperty(JCR_TITLE)) {
                                answer.writeAttribute(TITLE, node.getProperty(JCR_TITLE).getString());
                            }
                            answer.writeAttribute(PATH, node.getPath());
                            answer.writeAttribute(NODE_TYPE, node.getPrimaryNodeType() + "");
                            answer.writeClose();
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

    private Node findChildNodeRecursive(Node node, String nodeName) throws RepositoryException {
        Node jcrContent = null;
        if(node.hasNode(nodeName)) {
            jcrContent = node.getNode(nodeName);
        } else {
            // Loop for a sling:resourceSuperType and copy this one in instead
            Node superTypeNode = node;
            List<String> alreadyVisitedNodes = new ArrayList<>();
            while(true) {
                // If we already visited that node then exit to avoid an endless loop
                if(alreadyVisitedNodes.contains(superTypeNode.getPath())) { break; }
                alreadyVisitedNodes.add(superTypeNode.getPath());
                if(superTypeNode.hasProperty(SLING_RESOURCE_SUPER_TYPE)) {
                    String resourceSuperType = superTypeNode.getProperty(SLING_RESOURCE_SUPER_TYPE).getString();
                    if(isNotEmpty(resourceSuperType)) {
                        try {
                            superTypeNode = superTypeNode.getSession().getNode(APPS_ROOT + SLASH + resourceSuperType);
                            logger.trace("Found Resource Super Type: '{}'", superTypeNode.getPath());
                            // If we find the JCR Content then we are done here otherwise try to find this one's super resource type
                            if(superTypeNode.hasNode(nodeName)) {
                                jcrContent = superTypeNode.getNode(nodeName);
                                logger.trace("Found Content Node of Super Resource Type: '{}': '{}'", superTypeNode.getPath(), jcrContent.getPath());
                                break;
                            }
                        } catch(PathNotFoundException e) {
                            logger.warn("Could not find Resource Super Type Component: " + APPS_ROOT + SLASH + resourceSuperType + " -> ignore component", e);
                            break;
                        }
                    }
                }
            }
        }
        return jcrContent;
    }

    private void writeComponentNode(Node component, Node variation, JsonResponse answer) throws RepositoryException, IOException {
        answer.writeObject();
        answer.writeAttribute(NAME, component.getName());
        answer.writeAttribute(PATH, component.getPath());
        String group = null;
        String title = null;
        if(variation != null) {
            String id = variation.getIdentifier();
            String name = variation.getName();
            answer.writeAttribute(VARIATION, name);
            answer.writeAttribute(VARIATION_PATH, id);
            if(variation.hasProperty(TITLE)) {
                title = variation.getProperty(TITLE).getString();
            }
            if(variation.hasProperty(GROUP)) {
                group = variation.getProperty(GROUP).getString();
            }
        }
        if(isEmpty(title) && component.hasProperty(JCR_TITLE)) {
            title = component.getProperty(JCR_TITLE).getString();
        }
        if(isEmpty(group) && component.hasProperty(GROUP)) {
            group = component.getProperty(GROUP).getString();
        }
        if(isNotEmpty(title)) {
            answer.writeAttribute(TITLE, title);
        }
        if(isNotEmpty(group)) {
            answer.writeAttribute(GROUP, group);
        }
        if(component.hasProperty(TEMPLATE_COMPONENT)) {
            Property templateComponent = component.getProperty(TEMPLATE_COMPONENT);
            if(templateComponent != null) {
                answer.writeAttribute(TEMPLATE_COMPONENT, templateComponent.getBoolean());
            }
        }
        if(variation == null && findChildNodeRecursive(component, THUMBNAIL_PNG) != null) {
            answer.writeAttribute(THUMBNAIL, findChildNodeRecursive(component, THUMBNAIL_PNG).getPath());
        } else if(variation == null && findChildNodeRecursive(component, THUMBNAIL_SAMPLE_PNG) != null) {
            answer.writeAttribute(THUMBNAIL, findChildNodeRecursive(component, THUMBNAIL_SAMPLE_PNG).getPath());
        } else if(variation != null) {
            String thumbnailName = THUMBNAIL + "-" + variation.getName().toLowerCase()+".png";
            if(findChildNodeRecursive(component, thumbnailName) != null) {
                answer.writeAttribute(THUMBNAIL, findChildNodeRecursive(component, thumbnailName).getPath());
            }
        }
        answer.writeAttribute(NODE_TYPE, component.getPrimaryNodeType() + "");
        answer.writeClose();
    }
}

