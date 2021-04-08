package com.peregrine.render;

import org.apache.sling.api.resource.Resource;

public interface RenderService {

    /**
     * Renders the given resource inside this sling instance and returns its byte stream
     * @param resource Resource to be rendered
     * @param extension Extension of the rendering request
     * @return Byte Array of the rendered resource
     * @throws RenderException If the rendering failed
     */
    byte[] renderRawInternally(Resource resource, String extension) throws RenderException;

    /**
     * Renders the given resource inside this sling instance and returns its byte stream
     * @param resource Resource to be rendered
     * @param extension Extension of the rendering request
     * @return String content of the rendered resource
     * @throws RenderException If the rendering failed
     */
    String renderInternally(Resource resource, String extension) throws RenderException;

    class RenderException
        extends Exception
    {
        public RenderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}