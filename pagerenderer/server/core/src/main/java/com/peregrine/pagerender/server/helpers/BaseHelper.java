package com.peregrine.pagerender.server.helpers;

import com.peregrine.adaption.PerPage;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.script.Bindings;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Objects;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;

public class BaseHelper implements Use {

    // GLOBAL OBJECTS (from Use API Binding)
    private Node currentNode;
    private Session currentSession;
    private Logger log;
    private PrintWriter out;
    private ValueMap properties;
    private BufferedReader reader;
    private SlingHttpServletRequest request;
    private ResourceResolver resolver;
    private Resource resource;
    private SlingHttpServletResponse response;
    private SlingScriptHelper sling;

    // Peregrine API
    private Object model;
    private Resource siteRoot;
    private PerPage currentPage;
    private String pagePath;

    // Constants
    public static String CURRENT_NODE = "currentNode";
    public static String CURRENT_SESSION = "currentSession";
    public static String LOG = "log";
    public static String OUT = "out";
    public static String PROPERTIES = "properties";
    public static String READER = "reader";
    public static String REQUEST = "request";
    public static String RESOLVER = "resolver";
    public static String RESOURCE = "resource";
    public static String RESPONSE = "response";
    public static String SLING = "sling";

    public void init(Bindings bindings) {
        // initialize all global bindings (minus caconfig)
        currentNode = (Node) bindings.get(CURRENT_NODE);
        currentSession = (Session) bindings.get(CURRENT_SESSION);
        log = (Logger) bindings.get(LOG);
        out = (PrintWriter) bindings.get(OUT);
        properties = (ValueMap) bindings.get(PROPERTIES);
        reader = (BufferedReader) bindings.get(READER);
        request = (SlingHttpServletRequest) bindings.get(REQUEST);
        resolver = (ResourceResolver) bindings.get(RESOLVER);
        resource = (Resource) bindings.get(RESOURCE);
        response = (SlingHttpServletResponse) bindings.get(RESPONSE);
        sling = (SlingScriptHelper) bindings.get(SLING);

        // initialize peregrine api
        pagePath = request.getRequestPathInfo().getResourcePath();
        pagePath = pagePath.substring(0, pagePath.indexOf(JCR_CONTENT));
        Resource pageRes = resolver.getResource(pagePath);
        if (Objects.nonNull(pageRes)){
            currentPage = pageRes.adaptTo(PerPage.class);
        }
        if (Objects.nonNull(currentPage)){
            siteRoot = currentPage.getSiteResource();
        }
        try {
            model = sling.getService(ModelFactory.class).getModelFromResource(resource);
            if (Objects.isNull(model)) {
                model = sling.getService(ModelFactory.class).getModelFromRequest(request);
            }            
        } catch(Throwable t) {
            log.warn("could not get model for "+resource.getPath());
        }
    }

    public PrintWriter getOut() {
        return out;
    }

    public ValueMap getProperties() {
        return properties;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public SlingHttpServletRequest getRequest() {
        return request;
    }

    public ResourceResolver getResolver() {
        return resolver;
    }

    public Resource getResource() {
        return resource;
    }

    public SlingHttpServletResponse getResponse() {
        return response;
    }

    public SlingScriptHelper getSling() {
        return sling;
    }

    public Object getModel() {
        return model;
    }

    public Resource getSiteRoot() {
        return siteRoot;
    }

    public PerPage getCurrentPage() {
        return currentPage;
    }

    public String getPagePath() {
        return pagePath;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public Logger getLog() {
        return log;
    }
}
