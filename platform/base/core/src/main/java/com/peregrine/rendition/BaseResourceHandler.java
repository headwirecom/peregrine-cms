package com.peregrine.rendition;

import com.peregrine.transform.ImageContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;

/**
 *
 */
public interface BaseResourceHandler {

    /**
     * Creates a Rendition of an Asset
     * @param resource Asset Resource
     * @param renditionName Name of the Rendition
     * @param sourceMimeType Mime Type of the Asset (Source)
     * @return Image Context of the newly created Rendition
     * @throws HandlerException If the creation failed because resource is null, resource is not an asset,
     *                          rendition name is not provided or mime type is not provided
     */
    public ImageContext createRendition(Resource resource, String renditionName, String sourceMimeType) throws HandlerException;

    /**
     * Updates the Modification Properties of the given Node
     * @param resourceResolver Resource Resolver to find the Resource from the Node
     * @param node Node to be updated
     */
    public void updateModification(ResourceResolver resourceResolver, Node node);

    /**
     * Updates the Modification Properties of the given Reosurce
     * @param resource Resource to update its moditication properties
     */
    public void updateModification(Resource resource);

    /** Exception for the Base Resource Handler **/
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
