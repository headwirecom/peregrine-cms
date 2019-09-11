package com.peregrine.commons.json;

import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerUtil.convertToMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class JSonFormatterTest {
    private static final Logger logger = LoggerFactory.getLogger(JSonFormatterTest.class.getName());

    @Test
    public void testErrorResponseFormatting() throws IOException {
        ErrorResponse errorResponse = new ErrorResponse()
            .setErrorCode(123)
            .setErrorMessage("test")
            .setException(new Exception("test-exception"));
        Map errorResponseMap = convertToMap(errorResponse.getContent());
        assertEquals("Unexpected Error Message", "test",errorResponseMap.get("message"));
        List exceptionLines = (List) errorResponseMap.get("exception");
        logger.info("Exceptions: '{}'", exceptionLines);
        assertNotNull("Exception Array not found", exceptionLines);
        assertFalse("Exception Array must not be empty", exceptionLines.isEmpty());
        String firstLine = (String) exceptionLines.get(0);
        assertEquals("Unexpected first line", Exception.class.getName() + ": " + "test-exception", firstLine);
    }
}
