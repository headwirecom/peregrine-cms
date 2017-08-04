package com.peregrine.nodejs.script.servlet;

import com.peregrine.nodejs.j2v8.J2V8ProcessExecution;
import com.peregrine.nodejs.process.ExternalProcessException;
import com.peregrine.nodejs.process.ProcessContext;
import com.peregrine.nodejs.process.ProcessRunner;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.peregrine.nodejs.script.servlet.ScriptCaller.EXECUTE_SCRIPT_WITH_J2V8;
import static com.peregrine.nodejs.script.servlet.ScriptCaller.EXECUTE_SCRIPT_WITH_NODE_JS;

/**
 * Rest API Servlet to serve the Sling Node API
 */
@Component(
    service = { Servlet.class, ScriptCaller.class },
    property = {
        Constants.SERVICE_DESCRIPTION + "=Sling Node Script Calling Servlet",
        Constants.SERVICE_VENDOR + "=headwire.com Inc",
        "sling.servlet.paths=" + EXECUTE_SCRIPT_WITH_NODE_JS,
        "sling.servlet.paths=" + EXECUTE_SCRIPT_WITH_J2V8
    }
)
@SuppressWarnings("serial")
public class ScriptCallerServlet
    extends SlingAllMethodsServlet
    implements ScriptCaller
{

    private final Logger log = LoggerFactory.getLogger(ScriptCallerServlet.class);

    private ProcessRunner processRunner = new ProcessRunner();

    @Reference
    private J2V8ProcessExecution executor;

    @Override
    protected void doGet(
        SlingHttpServletRequest request,
        SlingHttpServletResponse response
    ) throws ServletException,
        IOException
    {
        log.trace("Example Servlet called");
        response.setContentType("text/plain");
        String path = request.getParameter("path");
        if(path == null || path.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Parameter 'path' must be provided");
            return;
        };
        String arguments = request.getParameter("arguments");
        String[] tokens = arguments != null ? arguments.split(",") : null;
        List<String> argumentList = tokens != null ? new ArrayList<String>(Arrays.asList(tokens)) : null;
        log.trace("Arguments Token: {}", argumentList);
        if(EXECUTE_SCRIPT_WITH_J2V8.equals(request.getPathInfo())) {
//            if(executor != null) {
                try {
                    processRunner.executeWithJ2V8(executor, path, argumentList);
//                    executor.executeScript(path, request, response, argumentList);
                } catch(ExternalProcessException e) {
                    log.error("Execution of JCR Script with j2v8 failed. Path: '{}'", path, e);
                }
//            }
        } else
        if(EXECUTE_SCRIPT_WITH_NODE_JS.equals(request.getPathInfo())) {
            // Read JS file from Resource
            String script = "";
            Resource jsResource = request.getResourceResolver().resolve(path + "/jcr:content");
            if(jsResource != null) {
                InputStream is = null;
                try {
                    is = jsResource.adaptTo(InputStream.class);
                    if(is != null) {
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(is, writer, Charset.defaultCharset());
                        script = writer.toString();
                        log.trace("Script loaded: '{}'", script);
                    } else {
                        log.error("Resource: '{}' could not be adapted to a Reader", jsResource);
                    }
                } finally {
                    IOUtils.closeQuietly(is);
                }
            } else {
                log.error("Resource: '{}' was not found", path);
            }
            if(script != null && !script.isEmpty()) {
                List<String> command = new ArrayList<String>(Arrays.asList("node", "-e", script));
                ProcessContext result = null;
                try {
                    result = processRunner.execute(command);
                    log.trace("Exit Code: '{}'", result.getExitCode());
                    log.trace("Output: '{}'", result.getOutput());
                    log.trace("Error: '{}'", result.getError());
                    if(result.getExitCode() == 0) {
                        response.getWriter().write(result.getOutput());
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write(result.getError());
                    }
                } catch(ExternalProcessException e) {
                    log.error("Execution of command: '{}' failed", command, e);
                } finally {
                    if(result != null) { result.tearDown(); }
                }
            } else {
                log.error("Script was not defined");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Unknown request: " + request.getPathInfo());
        }
    }
}

