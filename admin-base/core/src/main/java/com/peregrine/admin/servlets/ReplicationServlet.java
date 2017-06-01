package com.peregrine.admin.servlets;

import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.admin.replication.Replication;
import com.peregrine.admin.replication.Replication.ReplicationException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Peregrine: Replication Servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=sling/servlet/default",
                ServletResolverConstants.SLING_SERVLET_SELECTORS +"=json",
                ServletResolverConstants.SLING_SERVLET_EXTENSIONS +"=rep"
        }
)
@SuppressWarnings("serial")
/**
 * This servlet provides renditions of Peregrine Assets (per:Asset)
 * and creates them if they are not available yet
 */
public class ReplicationServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ReplicationServlet.class);

    @Reference
    private ReferenceLister referenceLister;
    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    private Collection<Replication> replications;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
        throws ServletException,
               IOException
    {
        String replicationName = request.getParameter("name");
        if(replicationName == null || replicationName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Paramter 'name' for the replication name is not provided");
            return;
        }
        String deepParameter = request.getParameter("deep");
        boolean deep = deepParameter != null && "true".equals(deepParameter.toLowerCase());
        Replication replication = null;
        for(Replication item: replications) {
            if(replicationName.equals(item.getName())) {
                replication = item;
                break;
            }
        }
        if(replication == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Replication not found for name: " + replicationName);
            return;
        }
        Resource resource = request.getResource();
        response.setContentType("application/json");
        List<Resource> replicates = new ArrayList<>();
        try {
            replicates = replication.replicate(resource, deep);
        } catch(ReplicationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace(response.getWriter());
            replicates = null;
        }
        if(replicates != null) {
            StringBuffer answer = new StringBuffer();
            answer.append("{");
            answer.append("\"sourceName\":\"" + resource.getName() + "\", ");
            answer.append("\"sourcePath\":\"" + resource.getPath() + "\", ");
            answer.append("\"replicates\":[");
            boolean first = true;
            for(Resource child : replicates) {
                if(first) {
                    first = false;
                } else {
                    answer.append(", ");
                }
                answer.append("{\"name\":\"");
                answer.append(child.getName());
                answer.append("\", \"path\":\"");
                answer.append(child.getPath());
                answer.append("\"}");
            }
            answer.append("]}");
            String temp = answer.toString();
            log.debug("Answer: '{}'", temp);
            response.getWriter().write(temp);
        }
    }
}

