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

import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.admin.util.JcrUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.convertSuffixToParams;
import static com.peregrine.admin.util.JcrUtil.EQUALS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + "Peregrine: Move Resource Servlet",
        SERVICE_VENDOR + EQUALS + "headwire.com, Inc",
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/move",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/rename",
        SLING_SERVLET_SELECTORS + EQUALS + "json"
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides the ability to move a resource
 */
public class MoveServlet extends SlingAllMethodsServlet {

    public static final String BEFORE_TYPE = "before";
    public static final String AFTER_TYPE = "after";
    public static final String CHILD_TYPE = "child";

    private final Logger log = LoggerFactory.getLogger(MoveServlet.class);
    private List<String> acceptedTypes = Arrays.asList(BEFORE_TYPE, AFTER_TYPE, CHILD_TYPE);

    @Reference
    private ReferenceLister referenceLister;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
        IOException
    {
        Map<String, String> params = convertSuffixToParams(request);
        log.debug("Parameters from Suffix: '{}'", params);
        response.setContentType("application/json");
        String fromPath = params.get("path");
        Resource from = JcrUtil.getResource(request.getResourceResolver(), fromPath);
        String toPath = params.get("to");
        if(request.getResource().getName().equals("move")) {
            String type = params.get("type");
            Resource to = JcrUtil.getResource(request.getResourceResolver(), toPath);
            if(from == null) {
                reportError(response, "Given Path does not yield a resource", fromPath);
            } else if(!acceptedTypes.contains(type)) {
                reportError(response, "Type is not recognized: " + type, fromPath);
            } else if(to == null) {
                reportError(response, "Target Path: " + toPath + " is not found", fromPath);
            } else {
                // Look for Referenced By list before we updating
                List<com.peregrine.admin.replication.Reference> references = referenceLister.getReferencedByList(from);

                boolean addAsChild = CHILD_TYPE.equals(type);
                boolean addBefore = BEFORE_TYPE.equals(type);
                Resource target = addAsChild ? to : to.getParent();
                Resource newResource = request.getResourceResolver().move(from.getPath(), target.getPath());
                // Reorder if needed
                if(!addAsChild) {
                    Node toNode = target.adaptTo(Node.class);
                    if(addBefore) {
                        try {
                            toNode.orderBefore(newResource.getName(), to.getName());
                        } catch(RepositoryException e) {
                            reportError(response, "New Resource: " + newResource.getPath() + " could not be reordered", fromPath);
                            return;
                        }
                    } else {
                        try {
                            NodeIterator i = toNode.getNodes();
                            Node nextNode = null;
                            while(i.hasNext()) {
                                Node child = i.nextNode();
                                if(child.getName().equals(to.getName())) {
                                    if(i.hasNext()) {
                                        nextNode = i.nextNode();
                                    }
                                    break;
                                }
                            }
                            if(nextNode != null) {
                                toNode.orderBefore(newResource.getName(), nextNode.getName());
                            }
                        } catch(RepositoryException e) {
                            reportError(response, "New Resource: " + newResource.getPath() + " could not be reordered (after)", fromPath);
                            return;
                        }
                    }
                }
                // Update the references
                for(com.peregrine.admin.replication.Reference reference : references) {
                    Resource propertyResource = reference.getPropertyResource();
                    ModifiableValueMap properties = JcrUtil.getModifiableProperties(propertyResource);
                    if(properties.containsKey(reference.getPropertyName())) {
                        properties.put(reference.getPropertyName(), newResource.getPath());
                    }
                }
                request.getResourceResolver().commit();
                StringBuffer answer = new StringBuffer();
                answer.append("{");
                answer.append("\"sourceName\":\"" + from.getName() + "\", ");
                answer.append("\"sourcePath\":\"" + from.getPath() + "\", ");
                answer.append("\"tagetName\":\"" + newResource.getName() + "\", ");
                answer.append("\"targetPath\":\"" + newResource.getPath() + "\"");
                answer.append("}");
                String temp = answer.toString();
                log.debug("Answer: '{}'", temp);
                response.getWriter().write(temp);
            }
        } else if(request.getResource().getName().equals("rename")) {
            if(from == null) {
                reportError(response, "Given Path does not yield a resource", fromPath);
            } else if(toPath == null || toPath.isEmpty()) {
                reportError(response, "Given New Name (to) is not provided", fromPath);
            } else if(toPath.indexOf('/') >= 0) {
                reportError(response, "Given New Name: " + toPath + " cannot have a slash", fromPath);
            } else {
                String newPath = from.getParent().getPath() + "/" + toPath;
                log.info("Rename from: '{}' to: '{}'", from.getPath(), newPath);
                Node fromNode = from.adaptTo(Node.class);
                try {
                    // Before the rename obtain the next node sibling and then after the move order the renamed node before its next sibling
                    Node parent = fromNode.getParent();
                    Node next = null;
                    if(parent != null) {
                        NodeIterator i = parent.getNodes();
                        while(i.hasNext()) {
                            Node child = i.nextNode();
                            if(child.getName().equals(fromNode.getName())) {
                                if(i.hasNext()) {
                                    next = i.nextNode();
                                    break;
                                }
                            }
                        }
                    }
                    fromNode.getSession().move(from.getPath(), newPath);
                    if(next != null) {
                        parent.orderBefore(toPath, next.getName());
                    }
                    fromNode.getSession().save();
                } catch(RepositoryException e) {
                    reportError(response, "Rename Failed: " + e.getMessage(), fromPath);
                }
                StringBuffer answer = new StringBuffer();
                answer.append("{");
                answer.append("\"sourceName\":\"" + from.getName() + "\", ");
                answer.append("\"sourcePath\":\"" + from.getPath() + "\", ");
                answer.append("\"targetName\":\"" + toPath + "\", ");
                answer.append("\"targetPath\":\"" + newPath + "\"");
                answer.append("}");
                String temp = answer.toString();
                log.debug("Answer: '{}'", temp);
                response.getWriter().write(temp);
            }
        }
    }

    private void reportError(SlingHttpServletResponse response, String message, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"" + message + "\", \"path\":\"" + path + "\"}");
    }
}
