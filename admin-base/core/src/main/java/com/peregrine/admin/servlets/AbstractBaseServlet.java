package com.peregrine.admin.servlets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
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
import java.util.Stack;

import static com.peregrine.admin.servlets.ServletHelper.obtainParameters;
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

    private void doRequest(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
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
        if("direct".equals(out.getType())) {
            out.handleDirect(request, response);
        } else {
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
    }

    abstract Response handleRequest(Request request) throws IOException;

    public static class Request {
        private SlingHttpServletRequest request;
        private SlingHttpServletResponse response;
        private Map<String, String> parameters = new HashMap<>();

        public Request(SlingHttpServletRequest request, SlingHttpServletResponse response) {
            this.request = request;
            this.response = response;
            this.parameters = obtainParameters(request);
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

        public Resource getResourceByPath(String path) { return request.getResourceResolver().getResource(path); }

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

        public void handleDirect(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
            throw new UnsupportedOperationException("Handle Direct is not supported");
        }

        public abstract String getMimeType();
    }

    public static class JsonResponse
        extends Response
    {
        private enum STATE { object, array };

        private JsonGenerator json;
        private StringWriter writer;
        private Stack<STATE> states = new Stack<>();

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
            states.push(STATE.object);
        }

        public JsonResponse writeAttribute(String name, boolean value) throws IOException {
            json.writeBooleanField(name, value);
            return this;
        }

        public JsonResponse writeAttribute(String name, int value) throws IOException {
            json.writeNumberField(name, value);
            return this;
        }

        public JsonResponse writeAttribute(String name, String value) throws IOException {
            json.writeStringField(name, value);
            return this;
        }

        public JsonResponse writeAttributeRaw(String name, String value) throws IOException {
            json.writeFieldName(name);
            json.writeRawValue(value);
            return this;
        }

        public JsonResponse writeArray(String name) throws IOException {
            json.writeArrayFieldStart(name);
            states.push(STATE.array);
            return this;
        }

        public JsonResponse writeObject() throws IOException {
            json.writeStartObject();
            states.push(STATE.object);
            return this;
        }

        public JsonResponse writeClose() throws IOException {
            STATE last = states.pop();
            switch(last) {
                case object:
                    json.writeEndObject();
                    break;
                case array:
                    json.writeEndArray();
                    break;
                default:
                    // Nothing to do here
            }
            return this;
        }

        public JsonResponse writeCloseAll() throws IOException {
            while(!states.empty()) {
                writeClose();
            }
            json.close();
            return this;
        }

        public String getContent() throws IOException {
            writeCloseAll();
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
            return (ErrorResponse) writeAttribute("code", code);
        }

        public ErrorResponse setErrorMessage(String message) throws IOException {
            return (ErrorResponse) writeAttribute("message", message);
        }

        public ErrorResponse setRequestPath(String path) throws IOException {
            return (ErrorResponse) writeAttribute("path", path);
        }

        public ErrorResponse setCustom(String fieldName, String value) throws IOException {
            return (ErrorResponse) writeAttribute(fieldName, value);
        }

        public ErrorResponse setException(Exception e) throws IOException {
            StringWriter out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));
            return (ErrorResponse) writeAttribute("exception", out.toString());
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

    public static class RedirectResponse
        extends Response
    {
        private String redirectTo;

        public RedirectResponse(String redirectTo) {
            super("direct");
            if(redirectTo == null || redirectTo.isEmpty()) {
                throw new IllegalArgumentException("Redirect To path must be provided");
            }
            this.redirectTo = redirectTo;
        }

        @Override
        public void handleDirect(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
            response.sendRedirect(redirectTo);
        }

        @Override
        public String getMimeType() {
            return null;
        }
    }

    public static class ForwardResponse
        extends Response
    {
        private Resource resource;
        private RequestDispatcherOptions requestDispatcherOptions;

        public ForwardResponse(Resource resource, RequestDispatcherOptions requestDispatcherOptions) {
            super("direct");
            if(resource == null) {
                throw new IllegalArgumentException("Redirect Resource must be provided");
            }
            this.resource = resource;
            this.requestDispatcherOptions = requestDispatcherOptions;
        }

        @Override
        public void handleDirect(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
            request.getRequestDispatcher(resource, requestDispatcherOptions).forward(request, response);
        }

        @Override
        public String getMimeType() {
            return null;
        }
    }
}
