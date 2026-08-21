package com.peregrine.functions;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

/**
 * Owns every rendering of a function's payload node (per/functions/source)
 * and of the functions folder (per/functions), so neither ever falls to the
 * default renderers: without this, jcr:content.json would stream the source
 * to anyone, and a depth request like functions.2.json would render the
 * children inline past the per-resource servlets.
 *
 * For an AUTHENTICATED session the payload node answers a small JSON of the
 * editable facts - that is how the console reads a function (readNode is a
 * rendering proxy and would execute it; the raw default renderer is exactly
 * what this servlet exists to prevent). Anonymous and labelled requests get
 * 404, and every write method is refused - edits go through
 * /perapi/admin/updateResource.json, which addresses the node as a suffix
 * and never resolves to this servlet.
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=per/functions/source",
        "sling.servlet.resourceTypes=per/functions",
        "sling.servlet.methods=GET",
        "sling.servlet.methods=HEAD",
        "sling.servlet.methods=POST",
        "sling.servlet.methods=PUT",
        "sling.servlet.methods=DELETE"
    }
)
public class SourceGuardServlet extends SlingAllMethodsServlet {

    private static final String SOURCE_TYPE = "per/functions/source";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        final String user = request.getResourceResolver().getUserID();
        final boolean authenticated = user != null && !"anonymous".equals(user);
        if (!authenticated || !SOURCE_TYPE.equals(request.getResource().getResourceType())) {
            response.sendError(404);
            return;
        }
        final ValueMap payload = request.getResource().getValueMap();
        final Calendar modified = payload.get("jcr:lastModified", Calendar.class);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(json(
                payload.get("source", ""),
                methodsOf(payload),
                modified == null ? 0L : modified.getTimeInMillis()));
    }

    private static String methodsOf(ValueMap payload) {
        final Object raw = payload.get("methods");
        if (raw instanceof String[] values) {
            return String.join(",", values);
        }
        return raw == null ? "" : raw.toString();
    }

    private static String json(String source, String methods, long modified) {
        return "{\"source\":" + quote(source)
                + ",\"methods\":" + quote(methods)
                + ",\"lastModified\":" + modified + "}";
    }

    private static String quote(String value) {
        final StringBuilder sb = new StringBuilder(value.length() + 16).append('"');
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
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
