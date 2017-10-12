package com.peregrine.nodejs.j2v8.impl;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.peregrine.nodejs.j2v8.J2V8ProcessExecution;
import com.peregrine.nodejs.j2v8.ScriptException;
import com.peregrine.nodejs.process.ProcessContext;
import com.peregrine.nodejs.process.ProcessContextTracker;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    service = J2V8ProcessExecution.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Sling Node J2V8 Process Based Execution",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
    }
)
public class J2V8ProcessExecutionService
    extends AbstractJ2V8ExecutionService
    implements J2V8ProcessExecution
{
    public static final String SLINGNODE_PROCESS_OUTPUT = "slingnode$processOutput";
    public static final String SLINGNODE_PROCESS_ERROR = "slingnode$processError";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    protected ResourceResolverFactory getResourceResolverFactory() {
        return resourceResolverFactory;
    }

    @Activate
    public void activate() {
        logger.trace("Activate J2V8ProcessExecutionService");
    }

    /**
     * Creates a Node Wrapper and adds additional methods
     * Beside the AbstractJ2V8ExecutionService.createAndInitialize() it will also
     * add:
     * - slingnode$processOutput Appends the first parameter as output to the process context
     * - slingnode$processError Append the first parameter as error to the process context
     *
     * @param processContext Process Context
     * @return Node Wrapper
     */
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
        answer.getRuntime().registerJavaMethod(outCallback, SLINGNODE_PROCESS_OUTPUT);
        log.trace("Registered 'slingnode$processOutput' with Method: '{}'", outCallback);

        JavaCallback errorCallback = new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                log.info("Error added: '{}'", parameters.getString(0));
                processContextTracker.appendError(parameters.getString(0));
                return null;
            }
        };
        answer.getRuntime().registerJavaMethod(errorCallback, SLINGNODE_PROCESS_ERROR);
        log.trace("Registered 'slingnode$processError' with Method: '{}'", errorCallback);

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
