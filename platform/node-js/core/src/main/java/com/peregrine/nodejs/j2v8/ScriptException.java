package com.peregrine.nodejs.j2v8;

/**
 * Marker Exception for J2V8 Script Execution Failures
 *
 * Created by Andreas Schaefer on 4/17/17.
 */
public class ScriptException extends Exception {
    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
