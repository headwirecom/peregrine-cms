package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.intra.IntraSlingCaller;
import com.peregrine.intra.IntraSlingCaller.CallException;
import com.peregrine.intra.IntraSlingCaller.CallerContext;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.admin.servlets.NodesServlet.NO_PATH_PROVIDED;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.DATA_JSON_EXTENSION;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.JSON_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodesServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(MoveNodeToServletTest.class.getName());

    @Override
    public Logger getLogger () {
    return logger;
}

    @Test
    public void testRequest() throws Exception {
        String requestPath = "/perapi/admin/node";
        String contentPath = "/content/test/nodes/path";
        setupCreation(requestPath, PATH, contentPath);
        String[] segments = contentPath.split("/");
        ResourceMock parent = null;
        String path = "";
        for(String segment: segments) {
            if(isNotEmpty(segment)) {
                path += SLASH + segment;
                ResourceMock temp = new ResourceMock()
                    .setPath(path)
                    .setParent(parent)
                    .setResourceResolver(mockResourceResolver);
                if(parent != null) { parent.addChild(temp); }
                parent = temp;
            }
        }

        NodesServlet servlet = new NodesServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        checkJsonResponse(response, "name", "content");
    }

    @Test
    public void testRequestFailure() throws Exception {
        String requestPath = "/perapi/admin/node";
        setupCreation(requestPath);

        NodesServlet servlet = new NodesServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        checkErrorResponse(response, NO_PATH_PROVIDED);
    }

    @Test
    public void testRequestAsset() throws Exception {
        String requestPath = "/perapi/admin/node";
        String contentPath = "/content/test/nodes/path";
        setupCreation(requestPath, PATH, contentPath);
        String[] segments = contentPath.split("/");
        ResourceMock parent = null;
        String path = "";
        for(String segment: segments) {
            if(isNotEmpty(segment)) {
                path += SLASH + segment;
                ResourceMock temp = new ResourceMock()
                    .setPath(path)
                    .setParent(parent)
                    .setResourceResolver(mockResourceResolver);
                if(parent != null) { parent.addChild(temp); }
                parent = temp;
            }
        }
        path += "/asset";
        ResourceMock temp = new ResourceMock()
            .setPath(path)
            .setParent(parent)
            .setResourceResolver(mockResourceResolver);
        parent.addChild(temp);
        parent = temp;
        temp.putProperty(NAME, "asset-test");
        temp.putProperty(PATH, path);
        temp.putProperty(JCR_PRIMARY_TYPE, ASSET_PRIMARY_TYPE);
        path += SLASH + JCR_CONTENT;
        temp = new ResourceMock()
            .setPath(path)
            .setParent(parent)
            .setResourceResolver(mockResourceResolver);
        temp.putProperty(JCR_MIME_TYPE, "test/test");
        parent.addChild(temp);

        NodesServlet servlet = new NodesServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        checkJsonResponse(response, "name", "content");
    }

    @Test
    public void testRequestPage() throws Exception {
        String requestPath = "/perapi/admin/node";
        String contentPath = "/content/test/nodes/path";
        setupCreation(requestPath, PATH, contentPath);
        String[] segments = contentPath.split("/");
        ResourceMock parent = null;
        String path = "";
        for(String segment: segments) {
            if(isNotEmpty(segment)) {
                path += SLASH + segment;
                ResourceMock temp = new ResourceMock()
                    .setPath(path)
                    .setParent(parent)
                    .setResourceResolver(mockResourceResolver);
                if(parent != null) { parent.addChild(temp); }
                parent = temp;
            }
        }
        path += "/page";
        ResourceMock temp = new ResourceMock()
            .setPath(path)
            .setParent(parent)
            .setResourceResolver(mockResourceResolver);
        parent.addChild(temp);
        parent = temp;
        temp.putProperty(NAME, "page-test");
        temp.putProperty(PATH, path);
        temp.putProperty(JCR_PRIMARY_TYPE, PAGE_PRIMARY_TYPE);
        path += SLASH + JCR_CONTENT;
        temp = new ResourceMock()
            .setPath(path)
            .setParent(parent)
            .setResourceResolver(mockResourceResolver);
        temp.putProperty(JCR_TITLE, "page-test-title");
        temp.setResourceType("my/test/component");
        parent.addChild(temp);

        NodesServlet servlet = new NodesServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        checkJsonResponse(response, "name", "content");
    }
}