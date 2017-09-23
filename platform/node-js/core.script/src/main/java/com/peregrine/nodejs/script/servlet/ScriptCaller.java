package com.peregrine.nodejs.script.servlet;

/**
 * Created by Andreas Schaefer on 4/10/17.
 */
public interface ScriptCaller {
    public static final String EXECUTE_SCRIPT_WITH_NODE_JS = "/api/nodejs/execute/node";
    public static final String EXECUTE_SCRIPT_WITH_J2V8 = "/api/nodejs/execute/j2v8";
}
