package com.peregrine.nodejs.j2v8;

import com.peregrine.nodejs.process.ProcessContextTracker;

import java.util.List;

/**
 * Created by Andreas Schaefer on 4/6/17.
 */
public interface J2V8ProcessExecution {

    public void executeScript(String jcrPath, ProcessContextTracker processContext)
        throws ScriptException;

    public void executeScript(String jcrPath, ProcessContextTracker processContext, List<String> arguments)
        throws ScriptException;
}
