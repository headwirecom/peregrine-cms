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
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.TEMPLATES;
import static com.peregrine.commons.util.PerConstants.TYPE;
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
    public void testRequestForComponentsNoResult() throws Exception {
        String requestPath = "/perapi/admin/restrictedSearch";
        String contentPath = "/content/test/restricted/search";
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
        String contentPath = "/content/test/restricted/search";
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
            "      \"name\" : \"search\"," +
            "      \"path\" : \"/content/test/restricted/search\"," +
            "      \"nodeType\" : \"per:Component\"" +
            "    }" +
            "  ]" +
            "}";
        compareJsonTexts(response.getContent(), expectedJson);
    }

//    @Test
//    public void testRequestWrongTypeFailure() throws Exception {
//        String requestPath = "/perapi/admin/restrictedSearch";
//        String contentPath = "/content/test/restricted/search";
//        String type = "wrong-component";
//        setupCreation(requestPath, PATH, contentPath);
//        mockRequest.getRequestMock().putProperty(TYPE, type);
//
//        RestrictedSearchServlet servlet = new RestrictedSearchServlet();
//        setupServlet(servlet);
//
//        Response response = servlet.handleRequest(mockRequest);
//        checkErrorResponse(response, UNKNOWN_TYPE + type);
//    }
//
//    @Test
//    public void testRequestQueryFailure() throws Exception {
//        String requestPath = "/perapi/admin/restrictedSearch";
//        String contentPath = "/content/test/restricted/search";
//        String type = "wrong-component";
//        setupCreation(requestPath, PATH, contentPath);
//        mockRequest.getRequestMock().putProperty(TYPE, COMPONENTS);
//
//        RestrictedSearchServlet servlet = new RestrictedSearchServlet();
//        setupServlet(servlet);
//        Session mockSession = mock(Session.class);
//        when(mockResourceResolver.adaptTo(eq(Session.class))).thenReturn(mockSession);
//        when(mockSession.getWorkspace()).thenThrow(new RuntimeException("test-failure"));
//
//        Response response = servlet.handleRequest(mockRequest);
//        checkErrorResponse(response, UNABLE_TO_GET_QUERY_MANAGER);
//    }
}