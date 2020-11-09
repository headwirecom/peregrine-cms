package com.peregrine.admin.replication.versions;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolver;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.peregrine.commons.Chars.EQ;

@Component(
    service = { Filter.class },
    property = {
            Constants.SERVICE_DESCRIPTION + EQ + "Release Resolver Filter",
            EngineConstants.SLING_FILTER_SCOPE + EQ + EngineConstants.FILTER_SCOPE_REQUEST
    }
)
public final class VersioningRequestFilter implements Filter {

    private final String versionLabel = VersioningConstants.PUBLISHED;

    @Reference
    private ServletResolver servletResolver;

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain
    ) throws IOException, ServletException {
        final Optional<SlingHttpServletRequest> slingRequestOptional = Optional.of(request)
                .map(this::unwrap)
                .filter(this::isMarkedForPublished);
        if (slingRequestOptional.isPresent()) {
            switchResolver(slingRequestOptional.get());
        }

        chain.doFilter(request, response);
    }

    private boolean isMarkedForPublished(final SlingHttpServletRequest request) {
        return Optional.ofNullable(request.getCookie(VersioningConstants.PEREGRINE_RENDERER_VERSION))
                .map(Cookie::getValue)
                .filter(versionLabel::equals)
                .isPresent();
    }

    private SlingHttpServletRequest unwrap(final ServletRequest request) {
        ServletRequest result = request;
        while (result instanceof SlingHttpServletRequestWrapper) {
            result = ((SlingHttpServletRequestWrapper) result).getSlingRequest();
        }

        return result instanceof SlingHttpServletRequest
                ? (SlingHttpServletRequest) result
                : null;
    }

    private void switchResolver(final SlingHttpServletRequest request) throws ServletException {
        final ResourceResolver oldResolver = request.getResourceResolver();
        try {
            final Method getRequestData = request.getClass().getMethod("getRequestData");
            final Object requestData = getRequestData.invoke(request);
            final Class<?> requestDataClass = requestData.getClass();
            final Method initResource = requestDataClass.getMethod("initResource", ResourceResolver.class);
            final ResourceResolver resolver = new VersioningResourceResolver(oldResolver, versionLabel);
            final Object resource = initResource.invoke(requestData, resolver);
            final Method initServlet = requestDataClass
                    .getMethod("initServlet", Resource.class, ServletResolver.class);
            initServlet.invoke(requestData, resource, servletResolver);
        } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException | RepositoryException e) {
            throw new ServletException("Error switching ResourceResolver");
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) { }

    @Override
    public void destroy() { }

}
