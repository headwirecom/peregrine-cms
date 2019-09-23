package com.peregrine.admin.servlets;

import com.peregrine.commons.servlets.AbstractBaseServlet.Response;
import com.peregrine.commons.test.mock.ResourceMock;
import com.peregrine.replication.Reference;
import com.peregrine.replication.ReferenceLister;
import org.apache.sling.api.resource.Resource;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.admin.servlets.ReferencedByListerServlet.GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE;
import static com.peregrine.admin.util.AdminConstants.SOURCE_NAME;
import static com.peregrine.admin.util.AdminConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.PATH;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReferencedListerServletTest
    extends AbstractServletTest
{
    private static final Logger logger = LoggerFactory.getLogger(MoveNodeToServletTest.class.getName());

    @Override
    public Logger getLogger () {
        return logger;
    }

    @Test
    public void testRequest() throws Exception {
        String requestPath = "/perapi/admin/referenceLister";
        String contentPath = "/content/test/reference/lister";
        setupCreation(requestPath, PATH, contentPath);


        ReferenceListerServlet servlet = new ReferenceListerServlet();
        setupServlet(servlet);
        ReferenceLister mockReferenceLister = mock(ReferenceLister.class);
        Whitebox.setInternalState(servlet, "referenceLister", mockReferenceLister);
        List<Resource> references = new ArrayList<>();
        ResourceMock referenceMock = new ResourceMock()
            .setPath("/content/test/referenced/by");
        references.add(referenceMock);
        when(mockReferenceLister.getReferenceList(eq(true), eq(mockParentResource), eq(true))).thenReturn(references);

        Response response = servlet.handleRequest(mockRequest);
        checkJsonResponse(response, SOURCE_NAME, "lister", SOURCE_PATH, contentPath);
    }

    @Test
    public void testRequestFailure() throws Exception {
        String requestPath = "/perapi/admin/referenceLister";
        String contentPath = "/content/test/reference/lister";
        setupCreation(requestPath, PATH, contentPath);

        when(mockResourceResolver.getResource(eq(contentPath))).thenReturn(null);

        ReferenceListerServlet servlet = new ReferenceListerServlet();
        setupServlet(servlet);

        Response response = servlet.handleRequest(mockRequest);
        checkErrorResponse(response, GIVEN_PATH_DOES_NOT_YIELD_A_RESOURCE);
    }
}