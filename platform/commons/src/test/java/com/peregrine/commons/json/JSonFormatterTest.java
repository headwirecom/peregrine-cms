package com.peregrine.commons.json;

import com.peregrine.commons.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.commons.servlets.AbstractBaseServlet.JsonResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
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

    @Test
    public void testMergeJsonResponse() throws IOException {
        JsonResponse source = new JsonResponse();
        source.writeAttribute("attribute-1", "value-1");
        source.writeAttribute("attribute-2", "value-1");
        source.writeObject("sub");
        source.writeAttribute("sub-attribute-1", "sub-value-1");
        source.writeAttribute("sub-attribute-2", "sub-value-2");
        source.writeAttribute("sub-attribute-3", "sub-value-3");
        source.writeCloseAll();
        JsonResponse target = new JsonResponse();
        target.writeResponse(source);
        target.writeResponse(source);
        logger.info("Merged Source into Target:\n\n'{}'", target.getContent());
    }

    @Test
    public void testAdvancedMergeJsonResponse() throws IOException {
        JsonResponse source = new JsonResponse();
        source.writeAttribute("attribute-1", "value-1");
        source.writeObject("sub");
        source.writeAttribute("sub-attribute-1", "sub-value-1");
        source.writeAttribute("sub-number-1", 123);
        source.writeObject("sub-2");
        source.writeAttribute("sub-2-attribute-1", "sub-2-value-1");
        source.writeAttribute("sub-2-boolean-1", true);
        source.writeAttribute("sub-2-boolean-2", false);
        source.writeClose();
        source.writeArray("array");
        source.writeString("array-1");
        source.writeObject();
        source.writeAttribute("sub-array-attribute-1", "sub-array-value-1");
        source.writeAttribute("sub-array-attribute-1", 234);
        source.writeClose();
        source.writeString("array-3");
        source.writeCloseAll();
        JsonResponse target = new JsonResponse();
        target.writeResponse(source);
        target.writeResponse(source);
        logger.info("Merged Advanced Source into Target:\n\n'{}'", target.getContent());
    }
}
