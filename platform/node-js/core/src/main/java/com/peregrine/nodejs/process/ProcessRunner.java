package com.peregrine.nodejs.process;

import com.peregrine.nodejs.j2v8.J2V8ProcessExecution;
import com.peregrine.nodejs.j2v8.J2V8WebExecution;
import com.peregrine.nodejs.j2v8.ScriptException;
import com.peregrine.process.ExternalProcessException;
import com.peregrine.process.ProcessContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import java.util.List;

/**
 * Executes external processes or J2V8 scripts
 *
 * Created by Andreas Schaefer on 4/6/17.
 */
public class ProcessRunner
    extends com.peregrine.process.ProcessRunner
{

    /**
     * Executes a script inside Sling as J2V8
     * @param executor J2V8 Process Executor
     * @param scriptJcrPath JCR Path to the script
     * @param command Arguments
     * @return Context of the Process Execution
     * @throws ExternalProcessException If the Execution failed
     */
    public ProcessContext executeWithJ2V8(J2V8ProcessExecution executor, String scriptJcrPath, List<String> command)
        throws ExternalProcessException
    {
        ProcessContextTracker answer = new ProcessContextTracker();
        if(executor != null) {
            try {
                executor.executeScript(
                    scriptJcrPath,
                    answer,
                    command
                );
            } catch(ScriptException e) {
                throw new ExternalProcessException("Failed to List Packages", e).setCommand(command).setProcessContext(answer);
            }
        }

        return answer;
    }

    /**
     * Executes a script inside Sling as J2V8
     * @param executor J2V8 Web Executor
     * @param scriptJcrPath JCR Path to the script
     * @param request Servlet Request
     * @param response Servlet Response
     * @return Context of the Process Execution
     * @throws ExternalProcessException If the Execution failed
     */
    public ProcessContext executeWithJ2V8(J2V8WebExecution executor, String scriptJcrPath, SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ExternalProcessException
    {
        ProcessContextTracker answer = new ProcessContextTracker();
        if(executor != null) {
            try {
                executor.executeScript(
                        scriptJcrPath,
                        request,
                        response
                );
            } catch(ScriptException e) {
                throw new ExternalProcessException("Failed to List Packages", e).setProcessContext(answer);
            }
        }

        return answer;
    }
}
