package com.peregrine.transform.operation;

import com.peregrine.transform.OperationContext;
import com.peregrine.transform.operation.GenericImageTransformation.ParameterHandler;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GenericImageTransformationTest {

    @Test
    public void testNoPlaceholder() throws Exception {
        String parameter = "--test";
        ParameterHandler handler = new ParameterHandler(parameter);
        OperationContext operationContext = new OperationContext("test", null);
        List<String> resolution = handler.resolve(operationContext);
        assertEquals("Unexpected Numbers of Resolutions", 1, resolution.size());
        assertEquals("Wrong parameter resolution", "--test", resolution.get(0));
    }

    @Test
    public void testSimplePlaceholder() throws Exception {
        String parameter = "{{test}}";
        ParameterHandler handler = new ParameterHandler(parameter);
        Map<String,String> operationParameters = new HashMap<>();
        operationParameters.put("test", "my-test");
        OperationContext operationContext = new OperationContext("test", operationParameters);
        List<String> resolution = handler.resolve(operationContext);
        assertEquals("Unexpected Numbers of Resolutions", 1, resolution.size());
        assertEquals("Wrong parameter resolution", "my-test", resolution.get(0));
    }

    @Test
    public void testParameterAndPlaceholder() throws Exception {
        String parameter = "{{--aTest||test}}";
        ParameterHandler handler = new ParameterHandler(parameter);
        Map<String,String> operationParameters = new HashMap<>();
        operationParameters.put("test", "a-test");
        OperationContext operationContext = new OperationContext("test", operationParameters);
        List<String> resolution = handler.resolve(operationContext);
        assertEquals("Unexpected Numbers of Resolutions", 2, resolution.size());
        assertEquals("Wrong 1st parameter resolution", "--aTest", resolution.get(0));
        assertEquals("Wrong 2nd parameter resolution", "a-test", resolution.get(1));
    }

    @Test
    public void testParameterAndPlaceholderInOne() throws Exception {
        String parameter = "--test-me={{test}}";
        ParameterHandler handler = new ParameterHandler(parameter);
        Map<String,String> operationParameters = new HashMap<>();
        operationParameters.put("test", "two-in-one-test");
        OperationContext operationContext = new OperationContext("test", operationParameters);
        List<String> resolution = handler.resolve(operationContext);
        assertEquals("Unexpected Numbers of Resolutions", 1, resolution.size());
        assertEquals("Wrong 1st parameter resolution", "--test-me=two-in-one-test", resolution.get(0));
    }

    @Test
    public void testNoBeforeParameterInPlaceholder() throws Exception {
        String parameter = "{{||test}}";
        try {
            ParameterHandler handler = new ParameterHandler(parameter);
        } catch (IllegalArgumentException e) {
            assertEquals("Wrong Exception Message", "Parameter: " + parameter + " is not valid (no parameter before ||)", e.getMessage());
        }
    }

    @Test
    public void testNoAfterParameterInPlaceholder() throws Exception {
        String parameter = "{{--test||}}";
        try {
            ParameterHandler handler = new ParameterHandler(parameter);
        } catch (IllegalArgumentException e) {
            assertEquals("Wrong Exception Message", "Parameter: " + parameter + " is not valid (no placeholder after ||)", e.getMessage());
        }
    }

    @Test
    public void testNoEndInPlaceholder() throws Exception {
        String parameter = "{{--test||";
        try {
            ParameterHandler handler = new ParameterHandler(parameter);
        } catch (IllegalArgumentException e) {
            assertEquals("Wrong Exception Message", "Parameter: " + parameter + " is not valid (no end }})", e.getMessage());
        }
    }

    @Test
    public void testNoStartInPlaceholder() throws Exception {
        String parameter = "--test||}}";
        try {
            ParameterHandler handler = new ParameterHandler(parameter);
        } catch (IllegalArgumentException e) {
            assertEquals("Wrong Exception Message", "Parameter: " + parameter + " is not valid (no start {{)", e.getMessage());
        }
    }

    @Test
    public void testEndBeforeStartInPlaceholder() throws Exception {
        String parameter = "}}--test||{{";
        try {
            ParameterHandler handler = new ParameterHandler(parameter);
        } catch (IllegalArgumentException e) {
            assertEquals("Wrong Exception Message", "Parameter: " + parameter + " is not valid (end }} before start {{)", e.getMessage());
        }
    }

    @Test
    public void testSplitPlaceholderWithPrefix() throws Exception {
        String parameter = "--a-test{{--b-test||a-value}}";
        try {
            ParameterHandler handler = new ParameterHandler(parameter);
        } catch (IllegalArgumentException e) {
            assertEquals("Wrong Exception Message", "Parameter: " + parameter + " is not valid (split placeholder must not have a prefix)", e.getMessage());
        }
    }

    @Test
    public void testSplitPlaceholderWithSuffix() throws Exception {
        String parameter = "{{--b-test||a-value}}a-test";
        try {
            ParameterHandler handler = new ParameterHandler(parameter);
        } catch (IllegalArgumentException e) {
            assertEquals("Wrong Exception Message", "Parameter: " + parameter + " is not valid (split placeholder must not have a suffix)", e.getMessage());
        }
    }
}