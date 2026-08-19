package com.peregrine.versions;

import org.apache.sling.api.SlingJakartaHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolver;
import org.apache.sling.api.wrappers.SlingJakartaHttpServletRequestWrapper;
import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.peregrine.commons.Chars.EQ;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Serves a request from a version label instead of from the head of the
 * repository, so a public host can be pinned to published content only.
 *
 * REGISTERED AS A JAKARTA FILTER, and that is the whole point. Sling 14 hands
 * a javax filter a JakartaToJavaxRequestWrapper: it implements
 * SlingHttpServletRequest, so unwrapping and reading the label kept working,
 * but it is not the engine's own request object and has no getRequestData().
 * The reflection below therefore threw NoSuchMethodException on every labelled
 * request, and every one of them answered 500 - which took the published-only
 * mechanism, and the public-site setup that rests on it, with it.
 */
@Component(
    service = { Filter.class },
    property = {
            Constants.SERVICE_DESCRIPTION + EQ + "Release Resolver Filter",
            EngineConstants.SLING_FILTER_SCOPE + EQ + EngineConstants.FILTER_SCOPE_REQUEST
    }
)
public final class VersioningRequestFilter implements Filter {

    private static final String LABEL_PROPERTY = "x-per-version-label";

    @Reference
    private ServletResolver servletResolver;

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain
    ) throws IOException, ServletException {
        final SlingJakartaHttpServletRequest slingRequest = unwrap(request);
        final String label = Optional.ofNullable(slingRequest)
                .map(VersioningRequestFilter::extractLabel)
                .orElse(null);
        if (isNotBlank(label)) {
            switchResolver(slingRequest, label);
        }

        chain.doFilter(request, response);
    }

    private static SlingJakartaHttpServletRequest unwrap(final ServletRequest request) {
        ServletRequest result = request;
        while (result instanceof SlingJakartaHttpServletRequestWrapper) {
            result = ((SlingJakartaHttpServletRequestWrapper) result).getSlingRequest();
        }

        return result instanceof SlingJakartaHttpServletRequest
                ? (SlingJakartaHttpServletRequest) result
                : null;
    }

    private static String extractLabel(final SlingJakartaHttpServletRequest request) {
        Optional<String> result = Optional.ofNullable(request.getCookie(LABEL_PROPERTY))
                .map(Cookie::getValue);
        if (result.isEmpty()) {
            result = Optional.ofNullable(request.getAttribute(LABEL_PROPERTY))
                    .map(Object::toString);
        }

        return result.orElseGet(() -> request.getHeader(LABEL_PROPERTY));
    }

    private void switchResolver(final SlingJakartaHttpServletRequest request, final String label)
            throws ServletException {
        final ResourceResolver oldResolver = request.getResourceResolver();
        try {
            final Method getRequestData = request.getClass().getMethod("getRequestData");
            final Object requestData = getRequestData.invoke(request);
            final Class<?> requestDataClass = requestData.getClass();
            final Method initResource = requestDataClass.getMethod("initResource", ResourceResolver.class);
            final ResourceResolver resolver = new VersioningResourceResolver(oldResolver, label);
            final Object resource = initResource.invoke(requestData, resolver);
            final Method initServlet = requestDataClass
                    .getMethod("initServlet", Resource.class, ServletResolver.class);
            initServlet.invoke(requestData, resource, servletResolver);
        } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // name what could not be reached: the previous message said only
            // "Error switching ResourceResolver", which told nobody that the
            // request object was the wrong one
            throw new ServletException("Error switching ResourceResolver on a "
                    + request.getClass().getName(), e);
        }
    }

    @Override
    public void init(final FilterConfig config) { }

    @Override
    public void destroy() { }

}
