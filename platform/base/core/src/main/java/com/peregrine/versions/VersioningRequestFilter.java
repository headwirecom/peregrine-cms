package com.peregrine.versions;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolver;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.peregrine.commons.Chars.EQ;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
        final SlingHttpServletRequest slingRequest = unwrap(request);
        final String label = Optional.ofNullable(slingRequest)
                .map(VersioningRequestFilter::extractLabel)
                .orElse(null);
        if (isNotBlank(label)) {
            switchResolver(slingRequest, label);
        }

        chain.doFilter(request, response);
    }

    private static SlingHttpServletRequest unwrap(final ServletRequest request) {
        ServletRequest result = request;
        while (result instanceof SlingHttpServletRequestWrapper) {
            result = ((SlingHttpServletRequestWrapper) result).getSlingRequest();
        }

        return result instanceof SlingHttpServletRequest
                ? (SlingHttpServletRequest) result
                : null;
    }

    private static String extractLabel(final SlingHttpServletRequest request) {
        Optional<String> result = Optional.ofNullable(request.getCookie(LABEL_PROPERTY))
                .map(Cookie::getValue);
        if (result.isEmpty()) {
            result = Optional.ofNullable(request.getAttribute(LABEL_PROPERTY))
                    .map(Object::toString);
        }

        return result.orElseGet(() -> request.getHeader(LABEL_PROPERTY));
    }

    private void switchResolver(final SlingHttpServletRequest request, final String label) throws ServletException {
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
            throw new ServletException("Error switching ResourceResolver");
        }
    }

    @Override
    public void init(final FilterConfig config) { }

    @Override
    public void destroy() { }

}
