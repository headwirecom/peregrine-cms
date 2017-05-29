package com.peregrine.nodetypes.merge;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Created by rr on 5/29/2017.
 */
public class RenderContext
{
    private final SlingHttpServletRequest request;

    public RenderContext(SlingHttpServletRequest request) {
        this.request = request;
    }

    public SlingHttpServletRequest getRequest() {
        return request;
    }
}
