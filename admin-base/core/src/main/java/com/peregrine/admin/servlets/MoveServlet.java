package com.peregrine.admin.servlets;

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
        String fromPath = params.get("path");
        String toPath = params.get("to");
        String type = params.get("type");
        response.setContentType("application/json");
        Resource from = JcrUtil.getResource(request.getResourceResolver(), fromPath);
        Resource to = JcrUtil.getResource(request.getResourceResolver(), toPath);
        if(from == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Given Path does not yield a resource\", \"path\":\"" + fromPath + "\"}");
        } else if(!acceptedTypes.contains(type)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Type is not recognized: " + type + "\", \"path\":\"" + fromPath + "\"}");
        } else if(to == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Target Path: " + toPath + " is not found\", \"path\":\"" + fromPath + "\"}");
        } else {
            // Look for Referenced By list before we updating
            List<com.peregrine.admin.replication.Reference> references = referenceLister.getReferencedByList(from);

            boolean addAsChild = CHILD_TYPE.equals(type);
            boolean addBefore = BEFORE_TYPE.equals(type);
            Resource target = addAsChild ?
                to :
                to.getParent();
            Resource newResource = request.getResourceResolver().move(from.getPath(), target.getPath());
            // Reorder if needed
            if(!addAsChild) {
                Node toNode = target.adaptTo(Node.class);
                if(addBefore) {
                    try {
                        toNode.orderBefore(newResource.getName(), to.getName());
                    } catch(RepositoryException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"New Resource: " + newResource.getPath() + " could not be reordered\", \"path\":\"" + fromPath + "\"}");
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
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"New Resource: " + newResource.getPath() + " could not be reordered (after)\", \"path\":\"" + fromPath + "\"}");
                        return;
                    }
                }
            }
            // Update the references
            for(com.peregrine.admin.replication.Reference reference: references) {
                Resource propertyResource = reference.getPropertyResource();
                ModifiableValueMap properties = JcrUtil.getModifiableProperties(propertyResource);
                if(properties.containsKey(reference.getPropertyName())) {
                    properties.put(reference.getPropertyName(), to.getPath());
                }
            }
            request.getResourceResolver().commit();
            StringBuffer answer = new StringBuffer();
            answer.append("{");
            answer.append("\"sourceName\":\"" + from.getName() + "\", ");
            answer.append("\"sourcePath\":\"" + from.getPath() + "\", ");
            answer.append("\"tagetName\":\"" + newResource.getName() + "\", ");
            answer.append("\"targetPath\":\"" + newResource.getPath() + "\", ");
            answer.append("}");
            String temp = answer.toString();
            log.debug("Answer: '{}'", temp);
            response.getWriter().write(temp);
        }
    }
}