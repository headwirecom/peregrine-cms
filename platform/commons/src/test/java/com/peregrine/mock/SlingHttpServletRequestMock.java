package com.peregrine.mock;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.mockito.Mockito;

import java.util.*;

import static com.peregrine.commons.Chars.DOT;
import static com.peregrine.commons.util.PerConstants.HTML;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SlingHttpServletRequestMock extends SlingHttpServletRequestWrapper {

    private final SlingHttpServletRequest mock;
    private final RequestPathInfo requestPathInfo = Mockito.mock(RequestPathInfo.class);
    private final Map<String, String[]> parameters = new HashMap<>();

    public SlingHttpServletRequestMock(final SlingHttpServletRequest mock) {
        super(mock);
        this.mock = mock;
        when(mock.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getExtension()).thenReturn(HTML);
    }

    public SlingHttpServletRequestMock(final String name) {
        this(mock(SlingHttpServletRequest.class, name));
    }

    public void bind(final Resource resource) {
        when(mock.getResource()).thenReturn(resource);
        final ResourceResolver resourceResolver = resource.getResourceResolver();
        when(mock.getResourceResolver()).thenReturn(resourceResolver);
        final String path = resource.getPath();
        when(requestPathInfo.getResourcePath()).thenReturn(path);
    }

    public void setExtension(final String extension) {
        when(requestPathInfo.getExtension()).thenReturn(extension);
    }

    public void setSelectors(final String... selectors) {
        when(requestPathInfo.getSelectors()).thenReturn(selectors);
        when(requestPathInfo.getSelectorString()).thenReturn(StringUtils.join(selectors, DOT));
    }

    public void setSelectorsString(final String selectorsString) {
        when(requestPathInfo.getSelectors()).thenReturn(selectorsString.split("\\."));
        when(requestPathInfo.getSelectorString()).thenReturn(selectorsString);
    }

    @Override
    public String getParameter(final String name) {
        return Optional.ofNullable(name)
                .map(this::getParameterValues)
                .filter(a -> a.length > 0)
                .map(a -> a[0])
                .orElse(null);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new Vector(parameters.keySet()).elements();
    }

    @Override
    public String[] getParameterValues(final String name) {
        return parameters.get(name);
    }

    public String[] putParameter(final String key, final String[] value) {
        return parameters.put(key, value);
    }

    public String[] putParameter(final String key, final String value) {
        return putParameter(key, new String[]{ value });
    }

    public String[] putParameter(final String key, final Object value) {
        return putParameter(key, String.valueOf(value));
    }

}
