package com.peregrine.admin.servlets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static com.peregrine.admin.servlets.ServletHelper.convertSuffixToParams;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * Created by schaefa on 6/20/17.
 */
public abstract class AbstractBaseServlet
    extends SlingAllMethodsServlet
{

//    public static enum ResponseType { JSON, TEXT, ERROR };

    final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean allowAll = false;

    public AbstractBaseServlet() {
    }

    public AbstractBaseServlet(boolean allowAll) {
        this.allowAll = allowAll;
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }

    private void doRequest(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        Response out = null;
        try {
            out = handleRequest(new Request(request, response));
            if(out == null) {
                out = new ErrorResponse().setHttpErrorCode(SC_INTERNAL_SERVER_ERROR).setErrorCode(-123).setErrorMessage("Servlet did not return a Response");
            }
        } catch(IOException e) {
            out = new ErrorResponse().setHttpErrorCode(SC_INTERNAL_SERVER_ERROR).setErrorCode(-124).setErrorMessage("Failed with IO exception").setException(e);
        } catch(RuntimeException e) {
            out = new ErrorResponse().setHttpErrorCode(SC_INTERNAL_SERVER_ERROR).setErrorCode(-125).setErrorMessage("Failed with runtime exception").setException(e);
        } catch(Error e) {
            // Do not swallow errors as the system needs to handle that -> log and rethrow
            logger.debug("Servlet Request failed with Error", e);
            throw e;
        }
        response.setContentType(out.getMimeType());
        String output = out.getContent();
        if("error".equals(out.getType())) {
            ErrorResponse error = (ErrorResponse) out;
            response.setStatus(error.getHttpErrorCode());
        }
        if(output == null) {
            out.writeTo(response.getOutputStream());
        } else {
            logger.trace("Servlet Response: '{}'", output);
            response.getWriter().write(output);
        }
        response.flushBuffer();
    }

    abstract Response handleRequest(Request request) throws IOException;

    public static class Request {
        private SlingHttpServletRequest request;
        private SlingHttpServletResponse response;
        private Map<String, String> parameters = new HashMap<>();

        public Request(SlingHttpServletRequest request, SlingHttpServletResponse response) {
            this.request = request;
            this.response = response;
            this.parameters = convertSuffixToParams(request);
        }

        public SlingHttpServletRequest getRequest() {
            return request;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public String getParameter(String name) {
            return getParameter(name, null);
        }

        public String getParameter(String name, String defaultValue) {
            String answer = parameters.get(name);
            return answer == null ? defaultValue : answer;
        }

        public ResourceResolver getResourceResolver() {
            return request.getResourceResolver();
        }

        public Resource getResource() { return request.getResource(); }

        public String getSuffix() { return request.getRequestPathInfo().getSuffix(); }
    }

    public static abstract class Response {
        private String type;

        public Response(String type) {
            this.type = type;
        }

        public String getType() { return type; }

        public String getContent() throws IOException {
            return null;
        }

        public void writeTo(OutputStream outputStream) throws IOException {
            throw new UnsupportedOperationException("Write To is not supported");
        }

        public abstract String getMimeType();
    }

    public static class JsonResponse
        extends Response
    {
        private JsonGenerator json;
        private StringWriter writer;

        public JsonResponse() throws IOException {
            this("json");
        }

        public JsonResponse(String type) throws IOException {
            super(type);
            init();
        }

        private void init() throws IOException {
            JsonFactory jf = new JsonFactory();
            writer = new StringWriter();
            json = jf.createGenerator(writer);
            json.useDefaultPrettyPrinter();
            json.writeStartObject();
        }

        public JsonGenerator getJson() {
            return json;
        }

        public String getContent() throws IOException {
            json.writeEndObject();
            json.close();
            return writer.toString();
        }

        @Override
        public String getMimeType() {
            return "application/json";
        }
    }

    public static class ErrorResponse
        extends JsonResponse {

        private int httpErrorCode = SC_BAD_REQUEST;

        public ErrorResponse() throws IOException {
            super("error");
        }

        public int getHttpErrorCode() {
            return httpErrorCode;
        }

        public ErrorResponse setHttpErrorCode(int httpErrorCode) {
            this.httpErrorCode = httpErrorCode;
            return this;
        }

        public ErrorResponse setErrorCode(int code) throws IOException {
            getJson().writeNumberField("code", code);
            return this;
        }

        public ErrorResponse setErrorMessage(String message) throws IOException {
            getJson().writeStringField("message", message);
            return this;
        }

        public ErrorResponse setRequestPath(String path) throws IOException {
            getJson().writeStringField("path", path);
            return this;
        }

        public ErrorResponse setCustom(String fieldName, String value) throws IOException {
            getJson().writeStringField(fieldName, value);
            return this;
        }

        public ErrorResponse setException(Exception e) throws IOException {
            StringWriter out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));
            getJson().writeStringField("exception", out.toString());
            return this;
        }
    }

    public static class TextResponse
        extends Response {

        private StringBuffer content;
        private String mimeType = "plain/text";

        public TextResponse(String type) {
            super(type);
        }

        public TextResponse(String type, String mimeType) {
            super(type);
            setMimeType(mimeType);
        }

        public TextResponse write(String text) {
            content.append(text);
            return this;
        }

        public TextResponse setMimeType(String mimeType) {
            if(mimeType != null && !mimeType.isEmpty()) {
                this.mimeType = mimeType;
            }
            return this;
        }

        @Override
        public String getContent() {
            return content.toString();
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }
}
