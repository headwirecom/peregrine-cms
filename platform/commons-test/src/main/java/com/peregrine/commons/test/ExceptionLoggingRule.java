package com.peregrine.commons.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This class is here to be used as a Test Rule to log any exception emitting from a Test
 * so that NPEs or alike are not swallowed and can be easily found in the log files.
 *
 * See here: http://stackoverflow.com/questions/7503277/best-way-of-logging-exceptions-when-tests-fail-e-g-using-a-junit-rule
 */
public class ExceptionLoggingRule implements TestRule {
    public Statement apply(Statement base, Description description) {
        return statement(base);
    }

    private Statement statement(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (Exception e) {
                    System.out.println("caught an exception");
                    e.printStackTrace(System.out);
                    throw e;
                }
            }
        };
    }
}
