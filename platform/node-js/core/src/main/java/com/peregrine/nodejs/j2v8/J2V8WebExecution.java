package com.peregrine.nodejs.j2v8;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import java.util.List;

/**
 * Executes a J2V8 Script based on a Servlet calls
 * Created by Andreas Schaefer on 4/6/17.
 */
public interface J2V8WebExecution {

    /**
     * Executes a script w/o arguments
     * @param jcrPath JCR Path to the script
     * @param request Sling Servlet Request
     * @param response Sling Servlet Response
     * @throws ScriptException If the execution failed
     */
    public void executeScript(String jcrPath, SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws ScriptException;

    /**
     * Executes a script with arguments
     * @param jcrPath JCR Path to the script
     * @param request Sling Servlet Request
     * @param response Sling Servlet Response
     * @param arguments List of arguments for the script
     * @throws ScriptException If the execution failed
     */
    public void executeScript(
        String jcrPath, SlingHttpServletRequest request, SlingHttpServletResponse response, List<String> arguments
    )
        throws ScriptException;

}
