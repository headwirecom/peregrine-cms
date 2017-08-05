package com.peregrine.nodejs.j2v8;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.List;

/**
 * Created by schaefa on 4/6/17.
 */
@Component(
    service = J2V8WebExecution.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Sling Node J2V8 Configuration",
        Constants.SERVICE_VENDOR + "=headwire.com Inc"
    }
)
public class J2V8WebExecutionService
    extends AbstractJ2V8ExecutionService
    implements J2V8WebExecution
{
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    protected ResourceResolverFactory getResourceResolverFactory() {
        return resourceResolverFactory;
    }

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
        answer.getRuntime().registerJavaMethod(outCallback, "slingnode$httpout");

        answer.getRuntime().registerJavaMethod(
            new JavaCallback() {
                public Object invoke(V8Object receiver, V8Array parameters) {
                    return request;
                }
            },
            "slingnode$getRequest"
        );

        answer.getRuntime().registerJavaMethod(
            new JavaCallback() {
                public Object invoke(V8Object receiver, V8Array parameters) {
                    return response;
                }
            },
            "slingnode$getResponse"
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
    }
}
