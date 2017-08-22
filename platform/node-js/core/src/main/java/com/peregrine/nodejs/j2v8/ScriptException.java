package com.peregrine.nodejs.j2v8;

/**
 * Created by schaefa on 4/17/17.
 */
public class ScriptException extends Exception {
    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
