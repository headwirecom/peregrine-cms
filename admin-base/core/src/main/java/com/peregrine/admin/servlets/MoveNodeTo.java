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

import com.peregrine.admin.resource.ResourceRelocation;
import com.peregrine.admin.servlets.AbstractBaseServlet.Request;
import com.peregrine.admin.servlets.AbstractBaseServlet.Response;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.obtainParameters;
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
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Move Node To Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/moveNodeTo"
    }
)
@SuppressWarnings("serial")
public class MoveNodeTo extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Reference
    private ResourceRelocation resourceRelocation;

    @Override
    Response handleRequest(Request request) throws IOException {
        Response answer;
        String path = request.getParameter("path");
        String component = request.getParameter("component");
        String drop = request.getParameter("drop");
        try {
            ResourceResolver rs = request.getResourceResolver();
            Session session = rs.adaptTo(Session.class);
            Resource to = rs.getResource(path);
            Resource from = rs.getResource(component);
            if("into-after".equals(drop)) {
                logger.debug("mode from {} to {}", component, path);
                if(!to.getPath().equals(from.getParent().getPath())) {
//AS TODO: Shouldn't we try to update the references ?
//                    resourceRelocation.moveToNewParent(from, to, false);
                    // If not the same parent then just move as they are added at the end
                    session.move(component, path + '/' + from.getName());
                } else {
                    // If the same parent then just move them to the end with a 'null' as destination
                    Node parent = session.getNode(path);
//                    resourceRelocation.reorder(to, from.getName(), null, false);
                    parent.orderBefore(from.getName(), null);
                }
                session.save();
                answer = new RedirectResponse(path + ".model.json");
//                response.sendRedirect(path + ".model.json");
            }
            else if("into-before".equals(drop)) {
                logger.debug("mode from {} to {}", component, path);
                Node parent = session.getNode(path);
                NodeIterator nodes = parent.getNodes();
                Node firstChild = null;
                if(nodes.hasNext()) {
                    firstChild = (Node) nodes.next();
                }
                if(!to.getPath().equals(from.getParent().getPath())) {
                    // If not the same parent then MOVE first
                    session.move(component, path + '/' + from.getName());
                }
                if(firstChild != null) {
                    // If there is any child then order BEFORE the first
                    parent.orderBefore(from.getName(), firstChild.getName());
                }
                session.save();
                answer = new RedirectResponse(path + ".model.json");
            } else if("before".equals(drop)) {
                //
                // Both BEFORE and AFTER can be handled in one as the only difference is if added before or after
                // and if they are the same parent we means we only ORDER otherwise we MOVE first
                if(to.getParent().getPath().equals(from.getParent().getPath())) {
                    logger.debug("same parent, just reorder before");
//                    resourceRelocation.reorder(to.getParent(), from.getName(), to.getName(), true);
                    Node node = session.getNode(to.getParent().getPath());
                    node.orderBefore(from.getName(), to.getName());
                    session.save();
                    answer = new RedirectResponse(to.getParent().getPath() + ".model.json");
//                    response.sendRedirect(to.getParent().getPath()+".model.json");
                } else {
                    logger.debug("moving from {} to {}", to.getParent().getPath());
                    session.move(component, to.getParent().getPath()+'/'+from.getName());
//                    resourceRelocation.moveToNewParent(from, )
                    Node node = session.getNode(to.getParent().getPath());
                    resourceRelocation.reorder(to.getParent(), from.getName(), to.getName(), true);
                    node.orderBefore(from.getName(), to.getName());
                    session.save();
                    answer = new RedirectResponse(to.getParent().getPath() + ".model.json");
//                    response.sendRedirect(to.getParent().getPath()+".model.json");
                }
            } else if("after".equals(drop)) {
                if(to.getParent().getPath().equals(from.getParent().getPath())) {
                    logger.debug("same parent, just reorder after");
                    Node node = session.getNode(to.getParent().getPath());
                    Node toNode = session.getNode(to.getPath());
                    int toIndexInParent = toNode.getIndex();
                    Node after = getNodeAfter(node, toNode);
                    // find the next
                    node.orderBefore(from.getName(), after != null ? after.getName(): null);
                    session.save();
                    answer = new RedirectResponse(to.getParent().getPath() + ".model.json");
                } else {
                    logger.debug("moving from {} to {}", to.getParent().getPath());
                    session.move(component, to.getParent().getPath()+'/'+from.getName());
                    Node node = session.getNode(to.getParent().getPath());
                    Node toNode = session.getNode(to.getPath());
                    Node after = getNodeAfter(node, toNode);
                    node.orderBefore(from.getName(), after != null ? after.getName(): null);
                    session.save();
                    answer = new RedirectResponse(to.getParent().getPath() + ".model.json");
                }
            } else {
                answer = new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Unknown Drop: " + drop);
            }
//            if ("into".equals(drop)) {
//                Node node = session.getNode(path).addNode("n"+UUID.randomUUID(), "nt:unstructured");
//                node.setProperty("sling:resourceType", component);
//                session.save();
//                response.sendRedirect(path+".model.json");
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
        } catch (Exception e) {
            logger.error("problems while moving", e);
            answer = new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to Move Resource").setException(e);
        }
        return answer;
    }

    private Node getNodeAfter(Node node, Node toNode) throws RepositoryException {
        Node after = null;
        for (NodeIterator it = node.getNodes(); it.hasNext(); ) {
            Node child = (Node) it.next();
            if(child.getPath().equals(toNode.getPath())) {
                if(it.hasNext()) {
                    after = (Node) it.next();
                    break;
                }
            }
        }
        return after;
    }
}

