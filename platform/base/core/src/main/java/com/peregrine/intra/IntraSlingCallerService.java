package com.peregrine.intra;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlethelpers.MockRequestPathInfo;
import org.apache.sling.servlethelpers.MockSlingHttpServletRequest;
import org.apache.sling.servlethelpers.MockSlingHttpServletResponse;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_GET;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = IntraSlingCaller.class,
    immediate = true,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Intra-Sling Caller",
        SERVICE_VENDOR + EQUALS + PER_VENDOR
    }
)
public class IntraSlingCallerService
    implements IntraSlingCaller
{
    private static final String UNSUPPORTED_ENCODING_WHILE_CREATING_THE_CALLER_RESPONSE = "Unsupported Encoding while creating the Caller Response";
    private static final String FAILED_TO_CALL_RESOURCE = "Failed to call resource, context: ";
    private static final String CALLING_REQUEST_FAILED = "Calling Request: '%s' failed with status: '%s'";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    @SuppressWarnings("unused")
    private SlingRequestProcessor requestProcessor;

    @Override
    public CallerContext createContext() {
        return new CallerContextImpl();
    }

    @Override
    public byte[] call(CallerContext callerContext) throws CallException {
        try {
            logger.trace("Intra Sling Caller Context: '{}'", callerContext);
            MockSlingHttpServletRequest req = new MockSlingHttpServletRequest(callerContext.getResourceResolver());
            req.setMethod(callerContext.getMethod());
            req.setResource(callerContext.getResource());
            MockRequestPathInfo pathInfo = (MockRequestPathInfo) req.getRequestPathInfo();
            pathInfo.setResourcePath(callerContext.getPath());
            pathInfo.setSelectorString(callerContext.getSelectors());
            pathInfo.setExtension(callerContext.getExtension());
            pathInfo.setSuffix(callerContext.getSuffix());
            req.setParameterMap(callerContext.getParameterMap());
            MockSlingHttpServletResponse resp = new MockSlingHttpServletResponse();
            resp.setCharacterEncoding("utf-8");
            requestProcessor.processRequest(req, resp, callerContext.getResourceResolver());
            logger.trace("Response Status: '{}'", resp.getStatus());
            //AS TODO: do we need to support redirects (301 / 302)
            if(resp.getStatus() != 200) {
                String content = resp.getOutput().toString();
                logger.error("Request of: '{}' failed (status: {}). Output : '{}'", req.getRequestURI(), resp.getStatus(), content);
                throw new CallException(String.format(CALLING_REQUEST_FAILED, req.getRequestURI(), resp.getStatus()));
            } else {
                return resp.getOutput();
            }
        } catch(UnsupportedEncodingException e) {
            throw new CallException(UNSUPPORTED_ENCODING_WHILE_CREATING_THE_CALLER_RESPONSE, e);
        } catch(ServletException | IOException e) {
            throw new CallException(FAILED_TO_CALL_RESOURCE + callerContext, e);
        }
    }

    public static class CallerContextImpl implements CallerContext {
        Resource resource;
        ResourceResolver resourceResolver;
        String method = METHOD_GET,
            path,
            selectors,
            extension,
            suffix;
        Map<String,Object> parameterMap = new HashMap<>();

        @Override
        public Resource getResource() { return resource; }

        @Override
        public ResourceResolver getResourceResolver() {
            return resourceResolver;
        }

        @Override
        public String getMethod() { return method; }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getSelectors() {
            return selectors;
        }

        @Override
        public String getExtension() {
            return extension;
        }

        @Override
        public String getSuffix() {
            return suffix;
        }

        @Override
        public Map<String, Object> getParameterMap() { return parameterMap; }

        @Override
        public CallerContext setResource(Resource resource) {
            this.resource = resource;
            return this;
        }

        @Override
        public CallerContext setResourceResolver(ResourceResolver resourceResolver) {
            this.resourceResolver = resourceResolver;
            return this;
        }

        @Override
        public CallerContext setMethod(String method) {
            this.method = method;
            return this;
        }

        @Override
        public CallerContext setPath(String path) {
            this.path = path;
            return this;
        }

        @Override
        public CallerContext setSelectors(String selectors) {
            this.selectors = selectors;
            return this;
        }

        @Override
        public CallerContext setExtension(String extension) {
            this.extension = extension;
            return this;
        }

        @Override
        public CallerContext setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        @Override
        public CallerContext setParameterMap(Map<String, Object> parameterMap) {
            if(parameterMap == null) { parameterMap = new HashMap<>(); }
            this.parameterMap = parameterMap;
            return this;
        }

        @Override
        public String toString() {
            return "CallerContextImpl{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", selectors='" + selectors + '\'' +
                ", extension='" + extension + '\'' +
                ", suffix='" + suffix + '\'' +
                ", parameterMap='" + parameterMap + '\'' +
                '}';
        }
    }
}
