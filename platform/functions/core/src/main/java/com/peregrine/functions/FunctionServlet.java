package com.peregrine.functions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes the function whose container node was requested:
 * /content/[tenant]/functions/[name][.ext][/suffix][?query].
 *
 * The node shape is dictated by the version-label machinery: the requested
 * CONTAINER carries the stable sling:resourceType (per/functions/function),
 * its jcr:content child carries everything mutable - the source string and
 * the methods gate - so a labelled request (the public host sends
 * x-per-version-label: Published) reads the FROZEN payload while the author
 * instance reads the draft. The read below goes through getValueMap(), the
 * only version-aware access path; adaptTo(InputStream/Node) would silently
 * return the draft.
 *
 * Binding with no extension restriction makes this servlet own every
 * rendering of the container (.json included), so the node itself never
 * leaks through the default renderers.
 *
 * The methods gate is a versioned property: the METHODS the published
 * function answers were the ones published with it.
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=per/functions/function",
        "sling.servlet.methods=GET",
        "sling.servlet.methods=HEAD",
        "sling.servlet.methods=POST",
        "sling.servlet.methods=PUT",
        "sling.servlet.methods=DELETE"
    }
)
public class FunctionServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionServlet.class);
    private static final String JCR_CONTENT = "jcr:content";
    private static final String SOURCE = "source";
    private static final String METHODS = "methods";
    private static final String[] DEFAULT_METHODS = { "GET", "HEAD" };
    private static final int MAX_BODY_BYTES = 1_048_576;

    @Reference
    private FunctionRunner runner;

    @Reference
    private FunctionTenantRegistry tenants;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        handle(request, response);
    }

    @Override
    protected void doPut(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        handle(request, response);
    }

    @Override
    protected void doDelete(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        handle(request, response);
    }

    private void handle(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        final Resource resource = request.getResource();
        final Resource content = resource.getChild(JCR_CONTENT);
        if (content == null) {
            response.sendError(404);
            return;
        }
        final Map<String, Object> payload = content.getValueMap();
        final String source = (String) payload.get(SOURCE);
        if (source == null || source.isBlank()) {
            response.sendError(404);
            return;
        }

        final String method = request.getMethod().toUpperCase();
        final List<String> allowed = allowedMethods(payload);
        if (!allowed.contains(method) && !("HEAD".equals(method) && allowed.contains("GET"))) {
            response.setHeader("Allow", String.join(", ", allowed));
            response.sendError(405);
            return;
        }

        final String path = resource.getPath();
        final String[] segments = path.split("/");
        // /content/<tenant>/functions/<name>
        final String tenant = segments.length > 2 ? segments[2] : "";
        final String name = resource.getName();

        try {
            final FunctionRunner.Outcome outcome = runner.run(
                    source,
                    requestFacts(request, tenant, name),
                    tenants.forTenantAndFunction(tenant, name),
                    tenant + "." + name);
            response.setStatus(outcome.status);
            response.setCharacterEncoding("utf-8");
            outcome.headers.forEach(response::setHeader);
            if (!"HEAD".equals(method)) {
                response.getWriter().write(outcome.body);
            }
        } catch (FunctionRunner.FunctionException e) {
            LOG.warn("function {}/{} failed: {}", tenant, name, e.getMessage());
            response.sendError(e.suggestedStatus, e.getMessage());
        }
    }

    private static List<String> allowedMethods(Map<String, Object> payload) {
        final Object raw = payload.get(METHODS);
        if (raw instanceof String[] values && values.length > 0) {
            return Arrays.asList(values);
        }
        if (raw instanceof String value && !value.isBlank()) {
            return Arrays.stream(value.toUpperCase().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
        return Arrays.asList(DEFAULT_METHODS);
    }

    private static Map<String, Object> requestFacts(SlingHttpServletRequest request, String tenant, String name)
            throws IOException {
        final Map<String, Object> query = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> query.put(k, v.length > 0 ? v[0] : ""));
        final Map<String, Object> headers = new HashMap<>();
        for (final String header : new String[] { "accept", "content-type", "user-agent",
                "x-forwarded-for", "x-forwarded-proto", "x-forwarded-host" }) {
            final String value = request.getHeader(header);
            if (value != null) {
                headers.put(header, value);
            }
        }
        final Map<String, Object> facts = new HashMap<>();
        facts.put("method", request.getMethod().toUpperCase());
        facts.put("path", request.getResource().getPath());
        facts.put("tenant", tenant);
        facts.put("name", name);
        facts.put("selectors", String.join(".", request.getRequestPathInfo().getSelectors()));
        facts.put("extension", nullToEmpty(request.getRequestPathInfo().getExtension()));
        facts.put("suffix", nullToEmpty(request.getRequestPathInfo().getSuffix()));
        facts.put("query", ProxyObject.fromMap(query));
        facts.put("headers", ProxyObject.fromMap(headers));
        if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod())) {
            facts.put("body", readBody(request));
        }
        return facts;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String readBody(SlingHttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream()) {
            final byte[] bytes = is.readNBytes(MAX_BODY_BYTES + 1);
            if (bytes.length > MAX_BODY_BYTES) {
                throw new FunctionRunner.FunctionException(413, "request body exceeds " + MAX_BODY_BYTES + " bytes");
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

}
