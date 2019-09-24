package com.peregrine.nodejs.script.servlet;

import com.peregrine.nodejs.j2v8.J2V8WebExecution;
import com.peregrine.nodejs.process.ProcessRunner;
import com.peregrine.process.ExternalProcessException;
import com.peregrine.process.ProcessContext;
import com.peregrine.render.RenderService;
import com.peregrine.render.RenderService.RenderException;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.EQUAL;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.nodejs.script.servlet.ScriptCaller.EXECUTE_SCRIPT_WITH_J2V8;
import static com.peregrine.nodejs.script.servlet.ScriptCaller.EXECUTE_SCRIPT_WITH_NODE_JS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.replace;

/**
 * Rest API Servlet to serve the Sling Node API
 */
@Component(
    service = { Servlet.class, ScriptCaller.class },
    property = {
        SERVICE_DESCRIPTION + EQUAL + PER_PREFIX + "Sling Node Script Calling Servlet",
        SERVICE_VENDOR + EQUAL + PER_VENDOR,
        SLING_SERVLET_PATHS + EQUAL + EXECUTE_SCRIPT_WITH_NODE_JS,
        SLING_SERVLET_PATHS + EQUAL + EXECUTE_SCRIPT_WITH_J2V8
    }
)
@SuppressWarnings("serial")
public class ScriptCallerServlet
    extends SlingAllMethodsServlet
    implements ScriptCaller
{

    private final Logger log = LoggerFactory.getLogger(ScriptCallerServlet.class);

    private ProcessRunner processRunner = new ProcessRunner();

    private J2V8WebExecution executor;

    @Reference RenderService renderService;

    @Reference(
        cardinality = ReferenceCardinality.OPTIONAL,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    void bindJ2V8ProcessExecution(J2V8WebExecution executor) {
        log.trace("Bind J2V8 Process Execution: '{}'", executor);
        this.executor = executor;
    }
    void unbindJ2V8ProcessExecution(J2V8WebExecution executor) {
        log.trace("Unbind J2V8 Process Execution: '{}'", executor);
        this.executor = null;
    }

    @Override
    protected void doGet(
        SlingHttpServletRequest request,
        SlingHttpServletResponse response
    ) throws IOException
    {
        log.trace("Example Servlet called");
        response.setContentType("text/plain");
        String path = request.getParameter("path");
        path = replace(path, "\n", EMPTY);
        path = replace(path, "\t", EMPTY);
        if(isEmpty(path)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Parameter 'path' must be provided");
            return;
        }

        String arguments = request.getParameter("arguments");
        String[] tokens = arguments != null ? arguments.split(",") : null;
        List<String> argumentList = tokens != null ? new ArrayList<>(Arrays.asList(tokens)) : null;
        log.trace("Arguments Token: {}", argumentList);
        if(EXECUTE_SCRIPT_WITH_J2V8.equals(request.getPathInfo())) {
            if(executor != null) {
                try {
                    processRunner.executeWithJ2V8(executor, path, request, response);
                } catch(ExternalProcessException e) {
                    log.error("Execution of JCR Script with j2v8 failed. Path: '{}'", path, e);
                }
            } else {
                log.error("J2V8 Executor is not installed here");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("J2V8 Executor is not installed for: " + EXECUTE_SCRIPT_WITH_J2V8);
            }
        } else
        if(EXECUTE_SCRIPT_WITH_NODE_JS.equals(request.getPathInfo())) {
            // Read JS file from Resource
            String script = "";
            Resource jsResource = getResource(request.getResourceResolver(), path + SLASH + JCR_CONTENT);
            log.trace("JS Resource (path: '{}'): '{}'", path, jsResource);
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
                // Resource not found -> check if there is an extension and if then render the page internally
                // and use the response as script
                int lastSlash = path.lastIndexOf('/');
                int extensionDot = path.indexOf('.', lastSlash);
                int length = path.length();
                if(extensionDot > 0 && extensionDot < length - 1) {
                    String resourcePath = path.substring(0, extensionDot);
                    String extension = path.substring(extensionDot + 1);
                    jsResource = request.getResourceResolver().resolve(resourcePath);
                    try {
                        if(!(jsResource instanceof NonExistingResource)) {
                            script = renderService.renderInternally(jsResource, extension);
                            log.trace("Script loaded: '{}'", script);
                        } else {
                            String checkedPath = resourcePath.replaceAll("[\n|\r|\t]", "_");
                            log.error("Resource with path: '{}' could not be found", checkedPath);
                        }
                    } catch(RenderException e) {
                        log.error("Failed to internally render resource: '{}' with extension: '{}'", jsResource.getPath(), extension);
                    }
                }
                String checkedPath = path.replaceAll("[\n|\r|\t]", "_");
                log.error("Resource: '{}' was not found", checkedPath);
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
            String encodedPath = org.owasp.encoder.Encode.forHtml(request.getPathInfo());
            response.getWriter().write("Unknown request: " + encodedPath);
        }
    }
}

