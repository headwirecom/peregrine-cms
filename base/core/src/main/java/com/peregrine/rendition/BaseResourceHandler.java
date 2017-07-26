package com.peregrine.rendition;

import com.peregrine.transform.ImageContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;

public interface BaseResourceHandler {

    public ImageContext createRendition(Resource resource, String renditionName, String sourceMimeType) throws HandlerException;

    public void updateModification(ResourceResolver resourceResolver, Node node);

    public void updateModification(Resource resource);

    public class HandlerException
        extends Exception
    {
        public HandlerException(String message) {
            super(message);
        }

        public HandlerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
