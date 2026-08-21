package com.peregrine.functions;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

/**
 * A function's jcr:content payload node is never rendered: without this, a
 * request for .../functions/name/jcr:content.json would fall to the default
 * GET servlet and stream the source. Function source is public-grade by
 * principle (secrets live in the tenant's OSGi config, never in source), but
 * there is no reason to hand it out either - authors read it through the
 * console's authenticated /perapi calls.
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=per/functions/source",
        "sling.servlet.methods=GET",
        "sling.servlet.methods=HEAD",
        "sling.servlet.methods=POST",
        "sling.servlet.methods=PUT",
        "sling.servlet.methods=DELETE"
    }
)
public class SourceGuardServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.sendError(404);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.sendError(404);
    }

    @Override
    protected void doPut(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.sendError(404);
    }

    @Override
    protected void doDelete(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.sendError(404);
    }

}
