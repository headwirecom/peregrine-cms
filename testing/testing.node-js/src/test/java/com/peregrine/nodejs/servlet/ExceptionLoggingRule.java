package com.peregrine.nodejs.servlet;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;

//import org.junit.rules.TestWatchman;
//import org.junit.runners.model.FrameworkMethod;

/**
 * This class is here to be used as a Test Rule to LOGGER any exception emitting from a Test
 * so that NPEs or alike are not swallowed and can be easily found in the LOGGER files.
 */
public class ExceptionLoggingRule extends TestWatcher {

    private Logger logger;

    public ExceptionLoggingRule(Logger logger) {
        this.logger = logger;
    }

    /**
     * Invoked when a test fails
     */
    protected void failed(Throwable e, Description description) {
        logger.error("Test failed with unexpected exception on Method: '{}'", description.getMethodName(), e);
    }
}
