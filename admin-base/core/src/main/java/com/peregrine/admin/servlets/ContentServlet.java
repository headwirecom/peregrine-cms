package com.peregrine.admin.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
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
                Constants.SERVICE_DESCRIPTION + "=content servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES +"=api/admin/content"
        }
)
@SuppressWarnings("serial")
public class ContentServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ContentServlet.class);

    @Reference
    ModelFactory modelFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        String suffix = request.getRequestPathInfo().getSuffix();

        if(suffix.endsWith(".data.json")) {
            suffix = suffix.substring(0, suffix.indexOf(".data.json"));
        }
        Resource res = request.getResourceResolver().getResource(suffix);
        RequestDispatcherOptions rdOtions = new RequestDispatcherOptions(
                RequestDispatcherOptions.OPT_REPLACE_SELECTORS + "=data"
        );
        request.getRequestDispatcher(res, rdOtions).forward(request, response);

    }

}

