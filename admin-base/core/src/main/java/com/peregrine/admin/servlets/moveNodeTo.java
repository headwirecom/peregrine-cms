package com.peregrine.admin.servlets;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.peregrine.admin.servlets.ServletHelper.convertSuffixToParams;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=move node to servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=api/admin/moveNodeTo"
        }
)
@SuppressWarnings("serial")
public class moveNodeTo extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(moveNodeTo.class);

    @Reference
    ModelFactory modelFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        Map<String, String> params = convertSuffixToParams(request);
        String path = params.get("path");
        String component = params.get("component");

        String drop = params.get("drop");
        log.debug(params.toString());

        try {
            ResourceResolver rs = request.getResourceResolver();
            Session session = rs.adaptTo(Session.class);
            Resource to = rs.getResource(path);
            Resource from = rs.getResource(component);
            if("into".equals(drop)) {
                log.debug("mode from {} to {}", component, path);
                session.move(component, path+'/'+from.getName());
                session.save();
                response.sendRedirect(path+".model.json");
            } else if("before".equals(drop)) {
                if(to.getParent().getPath().equals(from.getParent().getPath())) {
                    log.debug("same parent, just reorder before");
                    Node node = session.getNode(to.getParent().getPath());
                    node.orderBefore(from.getName(), to.getName());
                    session.save();
                    response.sendRedirect(to.getParent().getPath()+".model.json");
                } else {
                    log.debug("moving from {} to {}", to.getParent().getPath());
                    session.move(component, to.getParent().getPath()+'/'+from.getName());
                    Node node = session.getNode(to.getParent().getPath());
                    node.orderBefore(from.getName(), to.getName());
                    session.save();
                    response.sendRedirect(to.getParent().getPath()+".model.json");
                }
            } else if("after".equals(drop)) {
                if(to.getParent().getPath().equals(from.getParent().getPath())) {
                    log.debug("same parent, just reorder after");
                    Node node = session.getNode(to.getParent().getPath());
                    Node toNode = session.getNode(to.getPath());
                    int toIndexInParent = toNode.getIndex();
                    Node after = getNodeAfter(node, toNode);
                    // find the next
                    node.orderBefore(from.getName(), after != null ? after.getName(): null);
                    session.save();
                    response.sendRedirect(to.getParent().getPath()+".model.json");
                } else {
                    log.debug("moving from {} to {}", to.getParent().getPath());
                    session.move(component, to.getParent().getPath()+'/'+from.getName());
                    Node node = session.getNode(to.getParent().getPath());
                    Node toNode = session.getNode(to.getPath());
                    Node after = getNodeAfter(node, toNode);
                    node.orderBefore(from.getName(), after != null ? after.getName(): null);
                    session.save();
                    response.sendRedirect(to.getParent().getPath()+".model.json");
                }
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
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.error("problems while moving", e);
        }

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

