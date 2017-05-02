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
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.convertSuffixToParams;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=delete page servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=api/admin/deletePage"
        }
)
@SuppressWarnings("serial")
public class DeletePageServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(DeletePageServlet.class);

    @Reference
    ModelFactory modelFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        Map<String, String> params = convertSuffixToParams(request);
        String path = params.get("path");
        log.debug(params.toString());

        Session session = request.getResourceResolver().adaptTo(Session.class);
        try {
            session.getRootNode().getNode(path.substring(1)).remove();
            session.save();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace(response.getWriter());
        }
    }

}

