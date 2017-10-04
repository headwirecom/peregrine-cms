package com.peregrine.nodejs.j2v8;

import com.peregrine.nodejs.process.ProcessContextTracker;

import java.util.List;

/**
 * Interface that allows to execute a Script using J2V8
 *
 * Created by Andreas Schaefer on 4/6/17.
 */
public interface J2V8ProcessExecution {

    /**
     * Executes a script w/o arguments
     * @param jcrPath JCR Path to the script
     * @param processContext Process Context Tracker
     * @throws ScriptException If the execution failed
     */
    public void executeScript(String jcrPath, ProcessContextTracker processContext)
        throws ScriptException;

    /**
     * Executes a script with arguments
     * @param jcrPath JCR Path to the script
     * @param processContext Process Context Tracker
     * @param arguments List of arguments for the script
     * @throws ScriptException If the execution failed
     */
    public void executeScript(String jcrPath, ProcessContextTracker processContext, List<String> arguments)
        throws ScriptException;
}
