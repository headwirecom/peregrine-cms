package com.peregrine.graphiql;

import com.peregrine.commons.servlets.AbstractBaseServlet.TextResponse;
import com.peregrine.intra.IntraSlingCaller;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.Chars.EQ;
import static com.peregrine.commons.util.PerConstants.DATA_JSON_EXTENSION;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.JSON_MIME_TYPE;

@Component(
    immediate = true,
    service = { Filter.class },
    property = {
            Constants.SERVICE_DESCRIPTION + EQ + "GraphiQL Filter",
            Constants.SERVICE_RANKING + ":Integer" + EQ + Integer.MAX_VALUE,
            "sling.filter.scope=request"
//            EngineConstants.SLING_FILTER_SCOPE + EQ + EngineConstants.FILTER_SCOPE_REQUEST
    }
)
public final class GraphiQLRequestFilter implements Filter {

    private static final String CONTENT_PREFIX = "/content/";
    private static final String GRAPHIQL_SUFFIX = "/graphiql.html";
//    private static final String GRAPHIQL_PATH = "/content/graphiql/index.html";
    private static final String GRAPHIQL_PATH = "/content/graphiql/index";

    private static final Logger logger = LoggerFactory.getLogger(GraphiQLRequestFilter.class);

    public GraphiQLRequestFilter() {
        logger.error("Instance Created");
    }

    @Reference
    @SuppressWarnings("unused")
    private IntraSlingCaller intraSlingCaller;

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain

    ) throws IOException, ServletException {
        if (!(request instanceof SlingHttpServletRequest)) {
            throw new ServletException("Request is not a Apache Sling HTTP request.");
        }
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        String requestPath = slingRequest.getPathInfo();
        logger.error("Handle Request, path: '{}'", requestPath);
        if (requestPath.startsWith(CONTENT_PREFIX) && requestPath.endsWith(GRAPHIQL_SUFFIX)) {
            int index = requestPath.indexOf('/', CONTENT_PREFIX.length());
            String tenant = requestPath.substring(CONTENT_PREFIX.length(), index);
            if (tenant.length() > 0) {
//                request.getRequestDispatcher(GRAPHIQL_PATH + "/content/" + tenant).forward(
//                    request, response
//                );
//                request.getRequestDispatcher(GRAPHIQL_PATH).forward(
//                    request, response
//                );
                try {
                    Map<String, Object> parameters = new HashMap<>() {{
                        put("tenant", tenant);
                    }};
                    slingRequest.setAttribute("tenant", tenant);
                    byte[] internalResponse = intraSlingCaller.call(
                        intraSlingCaller.createContext()
                            .setResourceResolver(slingRequest.getResourceResolver())
                            .setPath(GRAPHIQL_PATH)
                            .setExtension("html")
                            .setParameterMap(parameters)
                    );
                    String temp = new String(internalResponse);
                    response.getWriter().print(temp);
                    response.setContentType("text/html;charset=utf-8");
                    return;
                } catch(IntraSlingCaller.CallException e) {
                    logger.warn("Internal call failed", e);
                }
            }
        }
        logger.error("Continue with regular Chain");
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

    @Override
    public void init(final FilterConfig config) {
        logger.error("Initialize GraphiQL Filter, config: '{}'", config);
    }

    @Override
    public void destroy() {
        logger.error("Destroy GraphiQL Filter");
    }

}
