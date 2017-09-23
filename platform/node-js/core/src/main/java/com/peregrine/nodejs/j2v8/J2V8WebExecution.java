package com.peregrine.nodejs.j2v8;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import java.util.List;

/**
 * Created by Andreas Schaefer on 4/6/17.
 */
public interface J2V8WebExecution {

    public void executeScript(String jcrPath, SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws ScriptException;

    public void executeScript(
        String jcrPath, SlingHttpServletRequest request, SlingHttpServletResponse response, List<String> arguments
    )
        throws ScriptException;

}
