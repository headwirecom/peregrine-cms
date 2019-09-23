package com.peregrine.commons.test.mock;

import com.peregrine.commons.servlets.AbstractBaseServlet.Request;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.commons.collections.IteratorUtils;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.DATA_JSON_EXTENSION;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PeregrineRequestMock
    extends Request
{
    private Node mockNode;
    private ResourceMock resourceMock;

    public static PeregrineRequestMock createInstance(String path, String...parameters) {
        return createInstance(path, null, null, false, parameters);
    }

    public static PeregrineRequestMock createInstance(String path, String selector, String suffix, boolean ignore, String...parameters) {
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

        if(selector != null) { when(mockRequestPathInfo.getSelectorString()).thenReturn(selector); }
        if(suffix != null) {
            when(mockRequestPathInfo.getSuffix()).thenReturn(suffix);
        }

        return new PeregrineRequestMock(mockSlingHttpServletRequest, mockSlingHttpServletResponse, path, suffix);
    }

    private PeregrineRequestMock(SlingHttpServletRequest request, SlingHttpServletResponse response, String path, String suffix) {
        super(request, response);

        ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
        when(request.getResourceResolver()).thenReturn(mockResourceResolver);
        resourceMock = new ResourceMock(path)
            .setPath(path)
            .setResourceResolver(mockResourceResolver);
        when(request.getResource()).thenReturn(resourceMock);
        mockNode = mock(Node.class);

        if(suffix != null) {
            if(suffix.endsWith(DATA_JSON_EXTENSION)) {
                suffix = suffix.substring(0, suffix.indexOf(DATA_JSON_EXTENSION));
            }
            ResourceMock suffixResourceMock = new ResourceMock()
                .setPath(suffix);
            when(mockResourceResolver.getResource(eq(suffix))).thenReturn(suffixResourceMock);
        }
    }

    public Node getNode() { return mockNode; }

    public ResourceMock getRequestMock() {
        return resourceMock;
    }
}
