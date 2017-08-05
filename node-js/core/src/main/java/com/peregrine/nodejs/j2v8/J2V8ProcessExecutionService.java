package com.peregrine.nodejs.j2v8;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.peregrine.nodejs.process.ProcessContext;
import com.peregrine.nodejs.process.ProcessContextTracker;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Created by schaefa on 4/6/17.
 */
@Component(
    service = J2V8ProcessExecution.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Sling Node J2V8 Process Based Execution",
        Constants.SERVICE_VENDOR + "=headwire.com Inc"
    }
)
public class J2V8ProcessExecutionService
    extends AbstractJ2V8ExecutionService
    implements J2V8ProcessExecution
{
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    protected ResourceResolverFactory getResourceResolverFactory() {
        return resourceResolverFactory;
    }

    public NodeWrapper createAndInitialize(final ProcessContext processContext) {
        NodeWrapper answer = super.createAndInitialize();
        final ProcessContextTracker processContextTracker = (ProcessContextTracker) processContext;

        JavaCallback outCallback = new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                log.info("Output added: '{}'", parameters.getString(0));
                processContextTracker.appendOutput(parameters.getString(0));
                return null;
            }
        };
        answer.getRuntime().registerJavaMethod(outCallback, "slingnode$processOutput");

        JavaCallback errorCallback = new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                log.info("Error added: '{}'", parameters.getString(0));
                processContextTracker.appendError(parameters.getString(0));
                return null;
            }
        };
        answer.getRuntime().registerJavaMethod(errorCallback, "slingnode$processError");

        return answer;
    }


    @Override
    public void executeScript(String jcrPath, ProcessContextTracker processContext)
        throws ScriptException
    {
        executeScript(jcrPath, processContext, null);
    }

    @Override
    public void executeScript(
        String jcrPath, ProcessContextTracker processContext, List<String> arguments
    )
        throws ScriptException
    {
        NodeWrapper wrapper = createAndInitialize(processContext);
        super.executeScript(jcrPath, wrapper, arguments);
    }
}
