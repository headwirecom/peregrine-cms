package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.replication.ReferenceLister;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.peregrine.admin.servlets.ReferencedByListerServlet.GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE;
import static com.peregrine.admin.servlets.RestrictedSearchServlet.GROUP;
import static com.peregrine.admin.servlets.RestrictedSearchServlet.TEMPLATE_COMPONENT;
import static com.peregrine.admin.servlets.RestrictedSearchServlet.THUMBNAIL;
import static com.peregrine.admin.servlets.RestrictedSearchServlet.THUMBNAIL_PNG;
import static com.peregrine.admin.servlets.RestrictedSearchServlet.UNABLE_TO_GET_QUERY_MANAGER;
import static com.peregrine.admin.servlets.RestrictedSearchServlet.UNKNOWN_TYPE;
import static com.peregrine.admin.util.AdminConstants.CURRENT;
import static com.peregrine.admin.util.AdminConstants.MORE;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.test.TestUtil.compareJsonTexts;
import static com.peregrine.commons.util.PerConstants.COMPONENTS;
import static com.peregrine.commons.util.PerConstants.COMPONENT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.TEMPLATES;
import static com.peregrine.commons.util.PerConstants.TITLE;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerConstants.VARIATIONS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestrictedSearchServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(MoveNodeToServletTest.class.getName());

    @Override
    public Logger getLogger () {
        return logger;
    }

    @Test
    public void testRequestForObjectsNoResult() throws Exception {
        String requestPath = "/perapi/admin/restrictedSearch";
        String contentPath = "/content/test/restricted/search/objects";
        setupCreation(requestPath, PATH, contentPath);
        mockRequest.getRequestMock().putProperty(TYPE, OBJECTS);

        Session mockSession = mock(Session.class);
        when(mockResourceResolver.adaptTo(eq(Session.class))).thenReturn(mockSession);
        Workspace mockWorkspace = mock(Workspace.class);
        when(mockSession.getWorkspace()).thenReturn(mockWorkspace);
        QueryManager mockQueryManager = mock(QueryManager.class);
        when(mockWorkspace.getQueryManager()).thenReturn(mockQueryManager);
        Query mockQuery = mock(Query.class);
        when(mockQueryManager.createQuery(anyString(), anyString())).thenReturn(mockQuery);
        QueryResult mockQueryResult = mock(QueryResult.class);
        when(mockQuery.execute()).thenReturn(mockQueryResult);
        NodeIterator mockNodeIterator = mock(NodeIterator.class);
        when(mockQueryResult.getNodes()).thenReturn(mockNodeIterator);
        when(mockNodeIterator.getSize()).thenReturn(0L);
        when(mockNodeIterator.hasNext()).thenReturn(false);

        RestrictedSearchServlet servlet = new RestrictedSearchServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        checkJsonResponse(response, CURRENT, "1", MORE, "false");
    }

    @Test
    public void testRequestForTemplatesWithResults() throws Exception {
        String requestPath = "/perapi/admin/restrictedSearch";
        String contentPath = "/content/test/restricted/search/templates";
        setupCreation(requestPath, PATH, contentPath);
        mockRequest.getRequestMock().putProperty(TYPE, TEMPLATES);

        Session mockSession = mock(Session.class);
        when(mockResourceResolver.adaptTo(eq(Session.class))).thenReturn(mockSession);
        Workspace mockWorkspace = mock(Workspace.class);
        when(mockSession.getWorkspace()).thenReturn(mockWorkspace);
        QueryManager mockQueryManager = mock(QueryManager.class);
        when(mockWorkspace.getQueryManager()).thenReturn(mockQueryManager);
        Query mockQuery = mock(Query.class);
        when(mockQueryManager.createQuery(anyString(), anyString())).thenReturn(mockQuery);
        QueryResult mockQueryResult = mock(QueryResult.class);
        when(mockQuery.execute()).thenReturn(mockQueryResult);
        NodeIterator mockNodeIterator = mock(NodeIterator.class);
        when(mockQueryResult.getNodes()).thenReturn(mockNodeIterator);
        when(mockNodeIterator.getSize()).thenReturn(1L);
        when(mockNodeIterator.hasNext()).thenReturn(true, false);
        when(mockNodeIterator.nextNode()).thenReturn(mockParentNode);
        NodeType mockPrimaryNodeType = mock(NodeType.class);
        when(mockParentNode.getPrimaryNodeType()).thenReturn(mockPrimaryNodeType);
        when(mockPrimaryNodeType.toString()).thenReturn(COMPONENT_PRIMARY_TYPE);
        ResourceMock contentNodeMock = new ResourceMock("child")
            .setPath(contentPath + SLASH + "child")
            .setParent(mockParentResource);
        when(mockParentNode.hasNode(eq(JCR_CONTENT))).thenReturn(true);
        when(mockParentNode.getNode(eq(JCR_CONTENT))).thenReturn(contentNodeMock.getNode());

        /*
            NOTE: I ran into an issue debugging this with IntelliJ. For whatever reason that failed
                  with an NPE during the Json processing. When I was running it in IntelliJ it was
                  fine as well as on the command line.
         */
        RestrictedSearchServlet servlet = new RestrictedSearchServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        String expectedJson = "{" +
            "  \"current\" : 1," +
            "  \"more\" : false," +
            "  \"data\" : [" +
            "    {" +
            "      \"name\" : \"templates\"," +
            "      \"path\" : \"" + contentPath + "\"," +
            "      \"nodeType\" : \"per:Component\"" +
            "    }" +
            "  ]" +
            "}";
        compareJsonTexts(response.getContent(), expectedJson);
    }

    @Test
    public void testRequestForComponentsWithFullResults() throws Exception {
        String requestPath = "/perapi/admin/restrictedSearch";
        String contentPath = "/content/test/restricted/search/components";
        setupCreation(requestPath, PATH, contentPath);
        mockRequest.getRequestMock().putProperty(TYPE, COMPONENTS);

        Session mockSession = mock(Session.class);
        when(mockResourceResolver.adaptTo(eq(Session.class))).thenReturn(mockSession);
        Workspace mockWorkspace = mock(Workspace.class);
        when(mockSession.getWorkspace()).thenReturn(mockWorkspace);
        QueryManager mockQueryManager = mock(QueryManager.class);
        when(mockWorkspace.getQueryManager()).thenReturn(mockQueryManager);
        Query mockQuery = mock(Query.class);
        when(mockQueryManager.createQuery(anyString(), anyString())).thenReturn(mockQuery);
        QueryResult mockQueryResult = mock(QueryResult.class);
        when(mockQuery.execute()).thenReturn(mockQueryResult);
        NodeIterator mockNodeIterator = mock(NodeIterator.class);
        when(mockQueryResult.getNodes()).thenReturn(mockNodeIterator);
        when(mockNodeIterator.getSize()).thenReturn(1L);
        when(mockNodeIterator.hasNext()).thenReturn(true, false);
        when(mockNodeIterator.nextNode()).thenReturn(mockParentNode);
        NodeType mockPrimaryNodeType = mock(NodeType.class);
        when(mockParentNode.getPrimaryNodeType()).thenReturn(mockPrimaryNodeType);
        when(mockPrimaryNodeType.toString()).thenReturn(COMPONENT_PRIMARY_TYPE);
        String childPath = contentPath + SLASH + "child";
        ResourceMock contentNodeMock = new ResourceMock("child")
            .setPath(childPath)
            .setParent(mockParentResource);
        when(mockParentNode.hasNode(eq(JCR_CONTENT))).thenReturn(true);
        when(mockParentNode.getNode(eq(JCR_CONTENT))).thenReturn(contentNodeMock.getNode());
        setComponentNodeProperties(contentNodeMock, false);
        //TODO Add additional properties
        contentNodeMock.putProperty(VARIATIONS, true);
        ResourceMock variation1Mock = new ResourceMock("variation1")
            .setPath(childPath + SLASH + "variations1")
            .setParent(contentNodeMock);
        setComponentNodeProperties(variation1Mock, true);
        ResourceMock variation2Mock = new ResourceMock("variation2")
            .setPath(childPath + SLASH + "variations2")
            .setParent(contentNodeMock);
        setComponentNodeProperties(variation2Mock, true);


        /*
            NOTE: I ran into an issue debugging this with IntelliJ. For whatever reason that failed
                  with an NPE during the Json processing. When I was running it in IntelliJ it was
                  fine as well as on the command line.
         */
        RestrictedSearchServlet servlet = new RestrictedSearchServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        String expectedJson = "{" +
            "  \"current\" : 1," +
            "  \"more\" : false," +
            "  \"data\" : [" +
            "    {" +
            "      \"name\" : \"components\"," +
            "      \"path\" : \"" + contentPath + "\"," +
            "      \"variation\" : \"thumbnail.png\"," +
            "      \"variationPath\" : \"thumbnail.png\"," +
            "      \"nodeType\" : \"per:Component\"" +
            "    }," +
            "    {" +
            "      \"name\" : \"components\"," +
            "      \"path\" : \"" + contentPath + "\"," +
            "      \"variation\" : \"variations1\"," +
            "      \"variationPath\" : \"variation1\"," +
            "      \"title\" : \"variations1\"," +
            "      \"group\" : \"group\"," +
            "      \"nodeType\" : \"per:Component\"" +
            "    }," +
            "    {" +
            "      \"name\" : \"components\"," +
            "      \"path\" : \"" + contentPath + "\"," +
            "      \"variation\" : \"variations2\"," +
            "      \"variationPath\" : \"variation2\"," +
            "      \"title\" : \"variations2\"," +
            "      \"group\" : \"group\"," +
            "      \"nodeType\" : \"per:Component\"" +
            "    }" +
            "  ]" +
            "}";
        compareJsonTexts(response.getContent(), expectedJson, false);
    }

    private void setComponentNodeProperties(ResourceMock resource, boolean isVariation) {
        ResourceMock parent;
        ResourceMock thumbnailMock;
        if(isVariation) {
            parent = (ResourceMock) resource.getParent();
            String name = resource.getName();
            resource.putProperty(TITLE, name);
            resource.putProperty(GROUP, "group");
            String thumbnailName = THUMBNAIL + "-" + resource.getName().toLowerCase() + ".png";
            thumbnailMock = new ResourceMock(thumbnailName)
                .setParent(resource.getParent())
                .setPath(resource.getParent().getPath() + SLASH + thumbnailName);
        } else {
            parent = resource;
            thumbnailMock = new ResourceMock(THUMBNAIL_PNG)
                .setParent(resource)
                .setPath(resource.getPath() + SLASH + THUMBNAIL_PNG);
        }
        String name = parent.getName();
        parent.putProperty(TITLE, name);
        parent.putProperty(GROUP, "group");
        parent.putProperty(JCR_TITLE, name);
        parent.putProperty(TEMPLATE_COMPONENT, "test/template/" + name);
    }
}