package com.peregrine.admin.servlets;

import com.peregrine.admin.replication.ReferenceLister;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Peregrine: Reference Lister Servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=sling/servlet/default",
                ServletResolverConstants.SLING_SERVLET_SELECTORS +"=json",
                ServletResolverConstants.SLING_SERVLET_EXTENSIONS +"=ref"
        }
)
@SuppressWarnings("serial")
/**
 * This servlet provides renditions of Peregrine Assets (per:Asset)
 * and creates them if they are not available yet
 */
public class ReferenceListerServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ReferenceListerServlet.class);

    @Reference
    private ReferenceLister referenceLister;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
        IOException
    {
        Resource resource = request.getResource();
//        com.peregrine.admin.replication.Reference root = referenceLister.getReferences(resource, true);
        response.setContentType("application/json");
//        List<com.peregrine.admin.replication.Reference> rootReferences = root.getReferences();
        List<Resource> references = referenceLister.getReferenceList(resource, true);
        StringBuffer answer = new StringBuffer();
        answer.append("{");
        answer.append("\"sourceName\":\"" + resource.getName() + "\", ");
        answer.append("\"sourcePath\":\"" + resource.getPath() + "\", ");
        answer.append("\"references\":[");
        boolean first = true;
        for(Resource child: references) {
            if(first) { first = false; } else { answer.append(", "); }
            answer.append("{\"name\":\"");
            answer.append(child.getName());
            answer.append("\", \"path\":\"");
            answer.append(child.getPath());
            answer.append("\"}");
        }
//        for(com.peregrine.admin.replication.Reference child : rootReferences) {
//            traverse(answer, child);
//        }
        answer.append("]}");
        String temp = answer.toString();
        log.debug("Answer: '{}'", temp);
        response.getWriter().write(temp);
    }

//    private void traverse(StringBuffer buffer, com.peregrine.admin.replication.Reference reference) throws IOException {
//        Resource resource = reference.getSource();
//        buffer.append("\"name\":\"");
//        buffer.append(resource.getName());
//        buffer.append("\", \"path\":\"");
//        buffer.append(resource.getPath());
//        buffer.append("\", \"references\":[");
//        for(com.peregrine.admin.replication.Reference referenceChild: reference.getReferences()) {
//            traverse(buffer, referenceChild);
//        }
//        buffer.append("]");
//    }
}

