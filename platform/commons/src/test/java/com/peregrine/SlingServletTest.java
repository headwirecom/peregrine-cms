package com.peregrine;

import org.apache.sling.api.SlingHttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.peregrine.mock.MockTools.fullName;
import static org.mockito.Mockito.*;

public class SlingServletTest extends SlingResourcesTest {

    protected final SlingHttpServletResponse response = mock(SlingHttpServletResponse.class, fullName(this, "Response"));
    protected final StringWriter writer = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(writer);

    protected int status;
    protected String contentType;
    protected String characterEncoding;

    public SlingServletTest() {
        try {
            when(response.getWriter()).thenReturn(printWriter);
        } catch (final IOException e) {
        }

        doAnswer(invocation -> {
            status = (Integer) invocation.getArguments()[0];
            return null;
        }).when(response).setStatus(anyInt());
        doAnswer(invocation -> {
            contentType = (String) invocation.getArguments()[0];
            return null;
        }).when(response).setContentType(anyString());
        doAnswer(invocation -> {
            characterEncoding = (String) invocation.getArguments()[0];
            return null;
        }).when(response).setCharacterEncoding(anyString());
    }
}
