package com.peregrine.commons.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.slf4j.Logger;

/**
 * Base Class for Unit and Integration Tests
 *
 * It provides a way to log the start and end of a Test Method
 * as well as logging exceptions that exit a test method
 *
 * Created by Andreas Schaefer on 6/2/16.
 */
public abstract class AbstractTest {

    @Rule
    public TestName name = new TestName();
    // Log unexpected exceptions in the log files for easy debugging / discovery (especially in Jenkins)
    @Rule
    public ExceptionLoggingRule exceptionLoggingRule = new ExceptionLoggingRule();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

/*
    Add these two methods to your class if you want to log the start and end of the test class

    @BeforeClass
    public static void setUpClass() throws Exception {
        LOGGER.info("\n\n---------------------- Start Test Class --------------\n\n");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        LOGGER.info("\n\n---------------------- End Test Class --------------\n\n");
    }

*/

    public abstract Logger getLogger();

    @Before
    public void setup() {
        getLogger().info("\n\nStart Test Case: '{}.{}' --------------\n\n", getClass().getSimpleName(), name.getMethodName());
    }

    @After
    public void tearDown() {
        getLogger().info("\n\nEnd Test Case: '{}.{}' --------------\n\n", getClass().getSimpleName(), name.getMethodName());
    }
}
