package com.peregrine.commons.servlets;

import com.peregrine.commons.test.AbstractTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class ServletHelperTest
    extends AbstractTest
{
    private static final Logger logger = LoggerFactory.getLogger(ServletHelperTest.class.getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Test
    public void testComponentPathToName() throws Exception {
        String componentPath = "/apps/it/components/myComponent";
        String expected = "it-components-myComponent";
        String answer = ServletHelper.componentPathToName(componentPath);
        assertEquals("Unexpected Component Name", expected, answer);
    }

    @Test
    public void testComponentNameToPath() throws Exception {
        String componentName = "it-components-myComponent";
        String expected = "it/components/myComponent";
        String answer = ServletHelper.componentNameToPath(componentName);
        assertEquals("Unexpected Component Path", expected, answer);
    }
}