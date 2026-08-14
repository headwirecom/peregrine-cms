package com.peregrine.admin.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates every request to {@code /perapi/admin/*} against the declared API
 * catalog ({@code /api-catalog.json}, embedded in this bundle and generated
 * from the servlet source).
 *
 * <p>By design this starts in <b>log-only</b> mode: it evaluates each request,
 * logs any violation with the endpoint, parameter and reason, and always lets
 * the request through. That surfaces every legitimate call the catalog does
 * not yet model - without the risk of rejecting real traffic - so the catalog
 * can be proven against production before {@code enforce} is switched on.
 *
 * <p>The single highest-value control is the content-root restriction on the
 * path/suffix of destructive operations: today those paths reach anywhere in
 * the repository (verified: {@code nodes.json/etc} returns the {@code /etc}
 * subtree). This filter reports every such path that falls outside the
 * allowed roots.
 */
@Component(
    immediate = true,
    service = { Filter.class },
    property = {
        Constants.SERVICE_DESCRIPTION + "=Admin API validation filter (log-only by default)",
        // run late, just before the servlet, so the suffix and parameters are resolved
        Constants.SERVICE_RANKING + ":Integer=-10000",
        "sling.filter.scope=request",
        "sling.filter.pattern=/perapi/admin/.*"
    }
)
@Designate(ocd = AdminApiValidationFilter.Config.class)
public final class AdminApiValidationFilter implements Filter {

    @ObjectClassDefinition(name = "Peregrine Admin API Validation Filter")
    public @interface Config {
        @AttributeDefinition(name = "Enforce",
            description = "When false (default) violations are only logged and the request passes. "
                + "When true, a violating request is rejected with 400.")
        boolean enforce() default false;

        @AttributeDefinition(name = "Allowed roots",
            description = "Absolute JCR roots a path/suffix parameter may address.")
        String[] allowedRoots() default { "/content", "/apps", "/conf", "/etc" };
    }

    private static final Logger LOG = LoggerFactory.getLogger(AdminApiValidationFilter.class);
    private static final String PREFIX = "/perapi/admin/";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private boolean enforce;
    private List<String> allowedRoots = Collections.emptyList();
    // normalized endpoint path (/admin/<name>.json) -> its catalog node
    private Map<String, JsonNode> byPath = new HashMap<>();

    @Activate
    void activate(final Config config) {
        this.enforce = config.enforce();
        this.allowedRoots = Arrays.asList(config.allowedRoots());
        loadCatalog();
        LOG.info("AdminApiValidationFilter active: enforce={}, {} endpoints, roots={}",
            enforce, byPath.size(), allowedRoots);
    }

    private void loadCatalog() {
        byPath = new HashMap<>();
        try (InputStream in = getClass().getResourceAsStream("/api-catalog.json")) {
            if (in == null) {
                LOG.warn("api-catalog.json not found on the bundle classpath; filter is inert");
                return;
            }
            JsonNode root = MAPPER.readTree(in);
            JsonNode endpoints = root.get("endpoints");
            if (endpoints == null || !endpoints.isArray()) return;
            for (JsonNode ep : endpoints) {
                String path = text(ep, "path");
                if (path != null) byPath.put(path, ep);
            }
        } catch (IOException e) {
            LOG.error("could not load api-catalog.json; filter is inert", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof SlingHttpServletRequest) {
            try {
                List<String> violations = evaluate((SlingHttpServletRequest) request);
                if (!violations.isEmpty()) {
                    String path = ((SlingHttpServletRequest) request).getPathInfo();
                    LOG.warn("admin-api {} {} violations: {}",
                        ((SlingHttpServletRequest) request).getMethod(), path, violations);
                    if (enforce) {
                        ((org.apache.sling.api.SlingHttpServletResponse) response)
                            .sendError(400, "request does not satisfy the admin API contract");
                        return;
                    }
                }
            } catch (Exception e) {
                // validation must NEVER be the reason a legitimate request fails
                LOG.debug("validation skipped due to error", e);
            }
        }
        chain.doFilter(request, response);
    }

    /** @return the list of human-readable violation reasons (empty = clean). */
    private List<String> evaluate(SlingHttpServletRequest req) {
        List<String> out = new ArrayList<>();
        String info = req.getPathInfo();               // /perapi/admin/deleteNode.json/content/...
        if (info == null || !info.startsWith(PREFIX)) return out;

        // normalize to the catalog key: /admin/<name>.json
        String rest = info.substring(PREFIX.length());  // deleteNode.json/content/...
        int slash = rest.indexOf('/');
        String head = slash < 0 ? rest : rest.substring(0, slash);
        if (!head.endsWith(".json")) return out;         // non-servlet resource
        String key = "/admin/" + head;
        JsonNode ep = byPath.get(key);
        if (ep == null) {
            out.add("endpoint not in catalog: " + key);
            return out;
        }

        // method
        List<String> methods = strings(ep.get("methods"));
        if (!methods.isEmpty() && !methods.contains(req.getMethod())) {
            out.add("method " + req.getMethod() + " not in " + methods);
        }

        boolean destructive = ep.path("destructive").asBoolean(false);
        String suffix = req.getRequestPathInfo().getSuffix();

        // suffix / path parameter
        JsonNode sfx = ep.get("suffix");
        if (sfx != null && sfx.path("required").asBoolean(false)) {
            if (suffix == null || suffix.isEmpty()) {
                out.add("required suffix (path) missing");
            } else if (destructive && !underAllowedRoot(suffix)) {
                out.add("path '" + suffix + "' outside allowed roots " + allowedRoots
                    + " on a destructive operation");
            }
        }

        // path-typed form/query parameters that also address the repository
        for (JsonNode p : ep.path("params")) {
            String pin = text(p, "in");
            if ("suffix".equals(pin)) continue;          // handled above
            String name = text(p, "name");
            if (name == null) continue;
            boolean required = p.path("required").asBoolean(false);
            String value = req.getParameter(name);
            if (required && (value == null || value.isEmpty())) {
                out.add("required parameter '" + name + "' missing");
                continue;
            }
            if (value == null) continue;
            String kind = text(p, "kind");
            String type = text(p, "type");
            if (("contentPath".equals(kind) || "anyPath".equals(kind) || "path".equals(type))
                    && destructive && value.startsWith("/") && !underAllowedRoot(value)) {
                out.add("parameter '" + name + "'='" + value + "' outside allowed roots "
                    + allowedRoots);
            }
            // the catalog's "pattern" is either a real regex (starts with ^)
            // or the name of a node-name validator - the catalog recommends
            // promoting these server-side, which is what this does
            String pattern = text(p, "pattern");
            String reason = checkPattern(pattern, value);
            if (reason != null) out.add("parameter '" + name + "'='" + value + "' " + reason);
        }
        return out;
    }

    // JCR-illegal characters, per NodeNameValidationService#isValidNodeName
    private static final Pattern JCR_ILLEGAL = Pattern.compile("[/:\\[\\]|*?%\"\\\\\\r\\n\\t]");

    /** @return null if the value satisfies the pattern/validator, else the reason it does not. */
    private String checkPattern(String pattern, String value) {
        if (pattern == null) return null;
        if (pattern.startsWith("^")) {                     // a genuine regex
            try {
                return Pattern.compile(pattern).matcher(value).matches()
                    ? null : "does not match " + pattern;
            } catch (Exception e) { return null; }
        }
        // otherwise the pattern names a node-name validator (isValidNodeName /
        // isValidPageName / isValidSiteName). These are the server-side rules
        // the catalog recommends enforcing here.
        boolean node = JCR_ILLEGAL.matcher(value).find()
            || value.isEmpty() || value.startsWith(" ")
            || value.equals(".") || value.equals("..");
        if (node) return "is not a valid node name";
        if (pattern.startsWith("isValidPageName") || pattern.startsWith("isValidSiteName")) {
            if (value.contains(".")) return "must not contain '.'";
        }
        if (pattern.startsWith("isValidSiteName")) {
            if (value.contains("-")) return "site name must not contain '-'";
        }
        return null;
    }

    private boolean underAllowedRoot(String path) {
        for (String root : allowedRoots) {
            if (path.equals(root) || path.startsWith(root + "/")) return true;
        }
        return false;
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n == null ? null : n.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
    private static List<String> strings(JsonNode arr) {
        List<String> out = new ArrayList<>();
        if (arr != null && arr.isArray()) arr.forEach(x -> out.add(x.asText()));
        return out;
    }

    @Override public void init(FilterConfig filterConfig) { }
    @Override public void destroy() { }
}
