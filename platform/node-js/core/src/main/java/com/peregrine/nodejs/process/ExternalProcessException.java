package com.peregrine.nodejs.process;

import java.util.List;

/**
 * Created by schaefa on 4/4/17.
 */
public class ExternalProcessException
    extends Exception
{
    private List<String> command = null;
    private ProcessContext processContext;

    public ExternalProcessException(String message) {
        super(message);
    }

    public ExternalProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public List<String> getCommand() {
        return command;
    }

    public ExternalProcessException setCommand(List<String> command) {
        this.command = command;
        return this;
    }

    public ProcessContext getProcessContext() {
        return processContext;
    }

    public ExternalProcessException setProcessContext(ProcessContext processContext) {
        this.processContext = processContext;
        return this;
    }
}
