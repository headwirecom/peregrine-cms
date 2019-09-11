package com.peregrine.commons.test.mock;

import com.peregrine.commons.servlets.AbstractBaseServlet.Request;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.commons.collections.IteratorUtils;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PeregrineRequestMock
    extends Request
{
    private Node mockNode;

    public static PeregrineRequestMock createInstance(String path, String...parameters) {
        SlingHttpServletRequest mockSlingHttpServletRequest = mock(SlingHttpServletRequest.class, "SlingHttpServletRequest");
        SlingHttpServletResponse mockSlingHttpServletResponse = mock(SlingHttpServletResponse.class, "SlingHttpServletResponse");
        RequestPathInfo mockRequestPathInfo = mock(RequestPathInfo.class);
        when(mockSlingHttpServletRequest.getRequestPathInfo()).thenReturn(mockRequestPathInfo);
        if(parameters.length % 2 != 0) {
            throw new IllegalArgumentException("Parameters must be in pairs but counted: " + parameters.length);
        }
        List<String> parameterNames = new ArrayList<>();
        for(int i = 0; i < parameters.length / 2; i++) {
            parameterNames.add(parameters[2 * i]);
            when(mockSlingHttpServletRequest.getParameter(eq(parameters[2 * i]))).thenReturn(parameters[(2 * i) + 1]);
        }
        Enumeration<String> e = IteratorUtils.asEnumeration(parameterNames.iterator());
        when(mockSlingHttpServletRequest.getParameterNames()).thenReturn(e);

        return new PeregrineRequestMock(mockSlingHttpServletRequest, mockSlingHttpServletResponse, path, parameters);
    }

    private PeregrineRequestMock(SlingHttpServletRequest request, SlingHttpServletResponse response, String path, String...parameters) {
        super(request, response);

        ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
        when(request.getResourceResolver()).thenReturn(mockResourceResolver);
        Resource mockResource = mock(Resource.class);
        when(mockResourceResolver.getResource(eq(path))).thenReturn(mockResource);
        mockNode = mock(Node.class);
        when(mockResource.adaptTo(Node.class)).thenReturn(mockNode);
    }

    public Node getNode() { return mockNode; }
}
