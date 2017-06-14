package com.peregrine.admin.servlets;

import com.peregrine.admin.replication.ReferenceLister;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
        SERVICE_DESCRIPTION + EQUALS + "Peregrine: Referenced By Lister Servlet",
        SERVICE_VENDOR + EQUALS + "headwire.com, Inc",
        SLING_SERVLET_METHODS + EQUALS + "GET",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/refBy",
        SLING_SERVLET_SELECTORS + EQUALS + "json"
    }
)
@SuppressWarnings("serial")
/**
 * This servlet provides a list of references of a given page
 */
public class ReferencedByListerServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ReferencedByListerServlet.class);

    @Reference
    private ReferenceLister referenceLister;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
        IOException
    {
        Map<String, String> params = convertSuffixToParams(request);
        log.debug("Parameters from Suffix: '{}'", params);
        String sourcePath = params.get("path");
        response.setContentType("application/json");
        Resource source = request.getResourceResolver().getResource(sourcePath);
        if(source != null) {
            List<Resource> references = referenceLister.getReferencedByList(source);
            StringBuffer answer = new StringBuffer();
            answer.append("{");
            answer.append("\"sourceName\":\"" + source.getName() + "\", ");
            answer.append("\"sourcePath\":\"" + source.getPath() + "\", ");
            answer.append("\"referencedBy\":[");
            boolean first = true;
            for(Resource child : references) {
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
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Given Path does not yield a resource\", \"path\":\"" + sourcePath + "\"}");
        }
    }
}