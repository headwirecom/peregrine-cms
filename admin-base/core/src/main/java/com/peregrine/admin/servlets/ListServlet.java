package com.peregrine.admin.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=search servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES +"=api/admin/list"
        }
)
@SuppressWarnings("serial")
public class ListServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ListServlet.class);

    @Reference
    ModelFactory modelFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        String suffix = request.getRequestPathInfo().getSuffix();

        response.setContentType("application/json");
        if("/tools".equals(suffix)) {
            getJSONFromResource(request, response, "/content/admin/tools");
        } else if("/tools/config".equals(suffix)) {
            getJSONFromResource(request, response, "/content/admin/toolsConfig");
        }

    }

    private void getJSONFromResource(SlingHttpServletRequest request, SlingHttpServletResponse response, String resourcePath) throws IOException {
        Resource res = request.getResourceResolver().getResource(resourcePath);
        try {
            String out = modelFactory.exportModelForResource(res, "jackson", String.class, Collections.<String, String> emptyMap());
            response.getWriter().write(out.toString());
        } catch (ExportException e) {
            log.error("error while exporting model", e);
        } catch (MissingExporterException e) {
            log.error("no exporter 'jackson' defined", e);
        }
    }

}

