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

public class BaseHelper implements Use {

    // GLOBAL OBJECTS (from Use API Binding)
    protected Node currentNode;
    protected Session currentSession;
    protected Logger log;
    protected PrintWriter out;
    protected ValueMap properties;
    protected BufferedReader reader;
    protected SlingHttpServletRequest request;
    protected ResourceResolver resolver;
    protected Resource resource;
    protected SlingHttpServletResponse response;
    protected SlingScriptHelper sling;

    // Peregrine API
    protected Object model;
    protected Resource siteRoot;
    protected PerPage currentPage;
    protected String pagePath;

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
        Resource pageRes = resolver.getResource(pagePath);
        if (Objects.nonNull(pageRes)){
            currentPage = pageRes.adaptTo(PerPage.class);
        }
        if (Objects.nonNull(currentPage)){
            siteRoot = currentPage.getSiteResource();
        }
        try {
            model = sling.getService(ModelFactory.class).getModelFromResource(resource);
        } catch(Throwable t) {
            model = sling.getService(ModelFactory.class).getModelFromRequest(request);
        }
    }


}
