package com.peregrine.nodejs.j2v8.impl;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.peregrine.nodejs.j2v8.J2V8WebExecution;
import com.peregrine.nodejs.j2v8.ScriptException;
import com.peregrine.render.RenderService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.List;

import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * Created by Andreas Schaefer on 4/6/17.
 */
@Component(
    service = J2V8WebExecution.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Sling Node J2V8 Configuration",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
    }
)
public class J2V8WebExecutionService
    extends AbstractJ2V8ExecutionService
    implements J2V8WebExecution
{
    public static final String SLINGNODE_HTTPOUT = "slingnode$httpout";
    public static final String SLINGNODE_GET_REQUEST = "slingnode$getRequest";
    public static final String SLINGNODE_GET_RESPONSE = "slingnode$getResponse";
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    @Reference
    private RenderService renderService;

    @Override
    protected ResourceResolverFactory getResourceResolverFactory() {
        return resourceResolverFactory;
    }
    @Override
    protected RenderService getRenderService() { return renderService; }

    /**
     * Creates a Node Wrapper and adds additional methods
     * Beside the AbstractJ2V8ExecutionService.createAndInitialize() it will also
     * add:
     * - slingnode$httpout Appends the first parameter as output to the Servlet Response
     * - slingnode$getRequest Returns Servlet Request
     * - slingnode$getResponse Returns Servlet Response
     *
     * @param request Servlet Request
     * @param response Servlet Response
     * @return Node Wrapper
     */
    public NodeWrapper createAndInitialize(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        NodeWrapper answer = super.createAndInitialize();

        JavaCallback outCallback = new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                if(!response.isCommitted()) {
                    try {
                        log.trace("Write to output: '{}'", parameters.get(0));
                        response.getWriter().write(parameters.get(0).toString());
                    } catch(IOException e) {
                        log.warn("Failed to obtain Response Writer", e);
                    }
                }
                return null;
            }
        };
        answer.getRuntime().registerJavaMethod(outCallback, SLINGNODE_HTTPOUT);
        log.trace("Registered: '{}'", SLINGNODE_HTTPOUT);

        answer.getRuntime().registerJavaMethod(
            new JavaCallback() {
                public Object invoke(V8Object receiver, V8Array parameters) {
                    return request;
                }
            }, SLINGNODE_GET_REQUEST
        );

        answer.getRuntime().registerJavaMethod(
            new JavaCallback() {
                public Object invoke(V8Object receiver, V8Array parameters) {
                    return response;
                }
            }, SLINGNODE_GET_RESPONSE
        );

        return answer;
    }


    @Override
    public void executeScript(String jcrPath, SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws ScriptException
    {
        executeScript(jcrPath, request, response, null);
    }

    @Override
    public void executeScript(
        String jcrPath, SlingHttpServletRequest request, SlingHttpServletResponse response, List<String> arguments
    )
        throws ScriptException
    {
        NodeWrapper wrapper = createAndInitialize(request, response);
        executeScript(jcrPath, wrapper, arguments);
        wrapper.release();
    }
}
