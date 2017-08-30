package com.peregrine.nodejs.j2v8.impl;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.peregrine.nodejs.j2v8.ScriptException;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schaefa on 4/6/17.
 */

public abstract class AbstractJ2V8ExecutionService {

    public static final String SUB_SERVICE_NAME = "nodejs-service";

    private static final String ROOT_SCRIPT =
        "try {\n" +
            "%1$s\n" +
            "var __snode$scriptToExecute = '%2$s'\n" +
            "%3$s" +
        "    var rx = require(__snode$scriptToExecute)\n" +
        "}\n" +
        "catch(ex) {\n" +
        "  console.error(ex);\n" +
        "}";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected abstract ResourceResolverFactory getResourceResolverFactory();

    public NodeWrapper createAndInitialize() {
        NodeJS node = NodeJS.createNodeJS();
        V8 runtime = node.getRuntime();
        final ResourceCache cache = new ResourceCache();
        NodeWrapper answer = new NodeWrapper()
            .setCache(cache)
            .setNode(node)
            .setRuntime(runtime);

        JavaCallback slingNodeRequest = new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                return "{ \"test\": \"test\" }";
            }
        };
        node.getRuntime().registerJavaMethod(slingNodeRequest, "slingnode$request");

        JavaCallback logCallback = new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                log.info(parameters.getString(0));
                return null;
            }
        };
        runtime.registerJavaMethod(logCallback, "slingnode$javalog");

        JavaCallback checkJcrPathCallback = new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                boolean check = false;
                try {
                    String jcrPath = parameters.getString(0);
                    check = !ResourceUtil.isNonExistingResource(getScriptResource(jcrPath, cache));
                    log.trace("Check for JCR, path: '{}', answer: '{}'", jcrPath, check);
                } catch(ScriptException e) {
                    log.error("Failed to obtain script at: '{}'", parameters, e);
                }
                return check;
            }
        };
        node.getRuntime().registerJavaMethod(checkJcrPathCallback, "slingnode$checkJcrPath");

        JavaCallback slingNodeReadFromJCR = new JavaCallback() {
            public Object invoke(V8Object receiver, V8Array parameters) {
                log.trace("Execute JCR Script called. Receiver: '{}', parameters: '{}'", receiver, parameters);
                String script = null;
                try {
                    String jcrPath = parameters.getString(0);
                    log.trace("path in jcr: '{}'", jcrPath);
                    script = readStringContent(jcrPath, cache);
                    return script;
                } catch(ScriptException e) {
                    log.error("Failed to read script at: '{}'", parameters, e);
                }
                return script;
            }
        };
        node.getRuntime().registerJavaMethod(slingNodeReadFromJCR, "slingnode$readFromJCR");

        return answer;
    }

    /**
     * Executes a given script based on the given NodeJS. At the end
     * of the script the Node JS instance is released
     *
     * @param jcrPath Path to the script
     * @param wrapper Wrapper instance that contain the NodeJS, runtime and Resource Cache
     * @param arguments List of arguments to handed over to the script
     *
     * @throws ScriptException if things go source
     */
    public void executeScript(
        String jcrPath, NodeWrapper wrapper, List<String> arguments
    )
        throws ScriptException
    {
        File nodeScript = null;
        try {
            String preScript = readStringContent("/apps/nodejs/scripts/slingnode.js", wrapper.getCache());
            // If no argument are provided we need to clear them as the default arguments here are
            // a path to the temporary script file and an empty {}
            String argumentsLine = "process.argv = [ ";
            if(arguments != null && !arguments.isEmpty()) {
                boolean first = true;
                for(String argument: arguments) {
                    // Ignore null or empty arguments
                    if(argument != null || !argument.trim().isEmpty()) {
                        if(first) {
                            // First argument does not need a leading separator character
                            first = false;
                        } else {
                            argumentsLine += ", ";
                        }
                        argumentsLine += "'" + argument + "'";
                    }
                }
            }
            argumentsLine += " ]\n";
            log.trace("Argument Line: '{}'", argumentsLine);
            String script = String.format(
                ROOT_SCRIPT,
                preScript,
                jcrPath,
                argumentsLine
            );
            log.trace("Script loaded: '\n{}\n'", script);
            nodeScript = createTemporaryScriptFile(script, "runner");
            wrapper.getNode().exec(nodeScript);
            while(wrapper.getNode().isRunning()) {
                wrapper.getNode().handleMessage();
            }
        } catch(IOException ioe) {
            new ScriptException("Unabled to write temporary file for script: " + jcrPath, ioe);
        }
        finally {
            if(wrapper.getNode() != null) { wrapper.getNode().release(); }
            if(nodeScript != null) { nodeScript.delete(); }
        }
    }

    private static File createTemporaryScriptFile(final String script, final String name) throws IOException {
        File tempFile = File.createTempFile(name, ".js.tmp", new File("."));
        tempFile.deleteOnExit();
        PrintWriter writer = new PrintWriter(tempFile, "UTF-8");
        try {
            writer.print(script);
        } finally {
            writer.close();
        }
        return tempFile;
    }

    private Resource getScriptResource(String jcrPath, ResourceCache cache)
        throws ScriptException
    {
        Resource answer = null;
        if(cache.containsEntry(jcrPath)) {
            answer = cache.getEntry(jcrPath);
            log.trace("Cache contains Path: '{}', returns: '{}'", jcrPath, answer.getPath());
        } else {
            ResourceResolver resourceResolver = null;
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(ResourceResolverFactory.SUBSERVICE, SUB_SERVICE_NAME);
                resourceResolver = getResourceResolverFactory().getServiceResourceResolver(params);
                answer = resourceResolver.resolve(jcrPath + "/jcr:content");
                log.trace("Cache found resource, Path: '{}', returns: '{}', is valid: '{}'", new Object[] {jcrPath, answer.getPath(), !ResourceUtil.isNonExistingResource(answer)});
            } catch(LoginException e) {
                throw new ScriptException("Could obtain Resource Resolver", e);
            }
        }
        return answer;
    }

    private String readStringContent(String jcrPath, ResourceCache cache)
        throws ScriptException
    {
        String answer = null;
        try {
            Resource resource = null;
            if(cache.containsEntry(jcrPath)) {
                resource = cache.getEntry(jcrPath);
            } else {
                resource = getScriptResource(jcrPath, cache);
            }
            log.trace("Found resource to read, Path: '{}', returns: '{}', is valid: '{}'", new Object[] {jcrPath, resource.getPath(), !ResourceUtil.isNonExistingResource(resource)});
            if(!ResourceUtil.isNonExistingResource(resource)) {
                InputStream is = null;
                try {
                    is = resource.adaptTo(InputStream.class);
                    log.trace("Got Input Stream: '{}'", is);
                    if(is != null) {
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(is, writer, Charset.defaultCharset());
                        answer = writer.toString();
                        log.trace("Script loaded: '{}'", answer);
                    } else {
                        log.error("Resource: '{}' is not a file to be read", resource);
                    }
                } finally {
                    IOUtils.closeQuietly(is);
                }
            } else {
                log.error("Resource: '{}' was not found", jcrPath);
            }
        } catch(IOException e) {
            throw new ScriptException("Could not read file: " + jcrPath, e);
        }
        return answer;
    }

    protected static class NodeWrapper {
        private NodeJS node;
        private V8 runtime;
        private ResourceCache cache;

        public NodeJS getNode() {
            return node;
        }

        public NodeWrapper setNode(NodeJS node) {
            this.node = node;
            return this;
        }

        public V8 getRuntime() {
            return runtime;
        }

        public NodeWrapper setRuntime(V8 runtime) {
            this.runtime = runtime;
            return this;
        }

        public ResourceCache getCache() {
            return cache;
        }

        public NodeWrapper setCache(ResourceCache cache) {
            this.cache = cache;
            return this;
        }
    }

    private static class ResourceCache {
        private Map<String, Resource> cache = new HashMap<>();

        public boolean containsEntry(String name) {
            return cache.containsKey(name);
        }

        public Resource getEntry(String name) {
            return cache.get(name);
        }

        public void putEntry(String name, Resource resource) {
            cache.put(name, resource);
        }
    }
}
