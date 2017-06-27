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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.admin.resource.ResourceRelocation;
import com.peregrine.util.PerUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.peregrine.util.PerConstants.ORDER_BEFORE_TYPE;
import static com.peregrine.util.PerConstants.ORDER_CHILD_TYPE;
import static com.peregrine.util.PerUtil.EQUALS;
import static com.peregrine.util.PerUtil.PER_PREFIX;
import static com.peregrine.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "insert node at servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/insertNodeAt"
    }
)
@SuppressWarnings("serial")
public class InsertNodeAt extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Reference
    ResourceRelocation resourceRelocation;

//AS This servlet creates a node (changing the JCR tree) so GET was deprecated
//    @Override
//    protected void doGet(SlingHttpServletRequest request,
//                         SlingHttpServletResponse response) throws ServletException,
//            IOException {
//
//        Map<String, String> params = convertSuffixToParams(request);
//        String path = params.get("path");
//        String component = params.get("component");
//        if(component.startsWith("/apps")) {
//            component = component.substring(component.indexOf('/', 1) + 1);
//        }
//        String drop = params.get("drop");
//        log.debug(params.toString());
//
//        try {
//            Session session = request.getResourceResolver().adaptTo(Session.class);
//            if ("into-before".equals(drop)) {
//                Node parent = session.getNode(path);
//                NodeIterator nodes = parent.getNodes();
//                Node firstChild = null;
//                if(nodes.hasNext()) {
//                    firstChild = (Node) nodes.next();
//                }
//                Node node = parent.addNode("n" + UUID.randomUUID(), "nt:unstructured");
//                node.setProperty("sling:resourceType", component);
//                if(firstChild != null) parent.orderBefore(node.getName(), firstChild.getName());
//                session.save();
//                response.sendRedirect(path + ".model.json");
//            }
//            else if ("into-after".equals(drop)) {
//                    Node node = session.getNode(path).addNode("n"+UUID.randomUUID(), "nt:unstructured");
//                    node.setProperty("sling:resourceType", component);
//                    session.save();
//                    response.sendRedirect(path+".model.json");
//            } else if("before".equals(drop)) {
//                Node node = session.getNode(path);
//                Node parent = node.getParent();
//                Node newNode = parent.addNode("n"+UUID.randomUUID(), "nt:unstructured");
//                newNode.setProperty("sling:resourceType", component);
//                parent.orderBefore(newNode.getName(), node.getName());
//                session.save();
//                response.sendRedirect(parent.getPath()+".model.json");
//
//            } else if("after".equals(drop)) {
//                Node node = session.getNode(path);
//                Node parent = node.getParent();
//                Node newNode = parent.addNode("n"+UUID.randomUUID(), "nt:unstructured");
//                newNode.setProperty("sling:resourceType", component);
//                Node after = null;
//                for (NodeIterator it = parent.getNodes(); it.hasNext(); ) {
//                    Node child = (Node) it.next();
//                    if(child.getPath().equals(node.getPath())) {
//                        if(it.hasNext()) {
//                            after = (Node) it.next();
//                            break;
//                        }
//                    }
//                }
//                if(after != null) {
//                    // if we are inserting in the last position then we are already at the right place
//                    parent.orderBefore(newNode.getName(), after.getName());
//                }
//                session.save();
//                response.sendRedirect(parent.getPath()+".model.json");
//
//            }
//        } catch (RepositoryException e) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            e.printStackTrace(response.getWriter());
//        }
//
//    }

    @Override
    Response handleRequest(Request request) throws IOException {
        String path = request.getParameter("path");
        Resource resource = PerUtil.getResource(request.getResourceResolver(), path);
        if(resource == null) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Resource not found by Path").setRequestPath(path);
        }
        String type = request.getParameter("type");
        // Next Block is only here to be backwards compatible
        if(type == null || type.isEmpty()) {
            type = request.getParameter("drop", "not provided");
        }
        boolean addAsChild = ORDER_CHILD_TYPE.equals(type) || type.startsWith("into");
//        // This line is only here to be backwards compatible
//        addAsChild = addAsChild || "into".equals(type);
        boolean addBefore = ORDER_BEFORE_TYPE.equals(type) || type.endsWith("-before");
        String component = request.getParameter("component");
        if(component != null && component.startsWith("/apps")) {
            component = component.substring(component.indexOf('/', 1) + 1);
        }
        String data = request.getParameter("content");
        Map nodeProperties = jsonToMap(data);

        try {
            Node node = resource.adaptTo(Node.class);
            Node newNode;
            if(addAsChild) {
                Resource firstChild = null;
                if(addBefore) {
                    Iterator<Resource> i = resource.listChildren();
                    if(i.hasNext()) { firstChild = i.next(); }
                }
                if(component != null) {
                    newNode = createNode(node, component);
                } else {
                    newNode = createNode(node, nodeProperties);
                }
                if(firstChild != null) {
                    resourceRelocation.reorder(resource, newNode.getName(), firstChild.getName(), true);
                }
                node.getSession().save();
                return new RedirectResponse(path + ".model.json");
            } else {
                Node parent = node.getParent();
                if(component != null) {
                    newNode = createNode(parent, component);
                } else {
                    newNode = createNode(parent, nodeProperties);
                }
                resourceRelocation.reorder(resource.getParent(), newNode.getName(), node.getName(), addBefore);
                node.getSession().save();
                return new RedirectResponse(parent.getPath() + ".model.json");
            }
        } catch (RepositoryException e) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to insert node at: " + path).setException(e);
        }

    }

    private Map jsonToMap(String data) throws IOException {
        Map answer;
        if(data == null || data.isEmpty()) {
            answer = Collections.EMPTY_MAP;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            answer = mapper.readValue(data, Map.class);
            logger.debug("Got Map: '{}' from JSon String", answer.toString());
        }
        return answer;
    }

    private Node createNode(Node parent, String component) throws RepositoryException {
        Node newNode = parent.addNode("n"+ UUID.randomUUID(), "nt:unstructured");
        newNode.setProperty("sling:resourceType", component);
        return newNode;
    }

    // todo: needs deep clone
    private Node createNode(Node parent, Map data) throws RepositoryException {
        data.remove("path");
        String component = (String) data.remove("component");

        Node newNode = parent.addNode("n"+ UUID.randomUUID(), "nt:unstructured");
        newNode.setProperty("sling:resourceType", ServletHelper.componentNameToPath(component));
        for (Object key: data.keySet()) {
            Object val = data.get(key);
            if(val instanceof String) {
                newNode.setProperty(key.toString(), (String) val);
            }
        }
        return newNode;
    }
}

