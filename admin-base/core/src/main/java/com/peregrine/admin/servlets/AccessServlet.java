package com.peregrine.admin.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=access servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES +"=api/admin/access"
        }
)
@SuppressWarnings("serial")
public class AccessServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(AccessServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        response.sendRedirect("/system/sling/info.sessionInfo.json");
    }

}

