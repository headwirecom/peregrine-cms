package com.peregrine.admin.servlets;

import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.admin.replication.Replication;
import com.peregrine.admin.replication.Replication.ReplicationException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
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
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.convertSuffixToParams;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Peregrine: Replication Servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=POST",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=api/admin/repl"
        }
)
@SuppressWarnings("serial")
/**
 * This servlet replicates the given resource with its JCR Content
 * ane any references
 *
 * It is invoked like this: curl -u admin:admin -X POST http://localhost:8080/api/admin/repl.json/path///content/sites/example//name//local
 */
public class ReplicationServlet extends SlingAllMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ReplicationServlet.class);

    @Reference
    private ReferenceLister referenceLister;
    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    private Collection<Replication> replications;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
        throws ServletException,
               IOException
    {
        Map<String, String> params = convertSuffixToParams(request);
        log.debug("Parameters from Suffix: '{}'", params);
        String sourcePath = params.get("path");
        String replicationName = params.get("name");
        if(replicationName == null || replicationName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Parameter 'name' for the replication name is not provided");
            return;
        }
        String deepParameter = params.get("deep");
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
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            response.setContentType("application/json");
            List<Resource> replicates = new ArrayList<>();
            try {
                replicates = replication.replicate(source, deep);
            } catch(ReplicationException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                e.printStackTrace(response.getWriter());
                replicates = null;
            }
            if(replicates != null) {
                StringBuffer answer = new StringBuffer();
                answer.append("{");
                answer.append("\"sourceName\":\"" + source.getName() + "\", ");
                answer.append("\"sourcePath\":\"" + source.getPath() + "\", ");
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
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Suffix: " + sourcePath + " is not a resource");
        }
    }
}

