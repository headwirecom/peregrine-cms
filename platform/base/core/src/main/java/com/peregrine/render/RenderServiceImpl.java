package com.peregrine.render;

import com.peregrine.intra.IntraSlingCaller;
import com.peregrine.versions.VersioningResourceResolver;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.peregrine.commons.util.PerConstants.PUBLISHED_LABEL;

/**
 * This class calls a resource internally and
 * returns the rendered response
 */
@Component(
    service = RenderService.class,
    immediate = true
)
public class RenderServiceImpl
    implements RenderService
{
    private static final String FAILED_TO_RENDER_RESOURCE = "Failed to render resource: ";

    @Reference
    @SuppressWarnings("unused")
    private IntraSlingCaller intraSlingCaller;

    public byte[] renderRawInternally(Resource resource, String extension) throws RenderException {
        try {
            final var initialResolver = resource.getResourceResolver();
            final var targetResolver = new VersioningResourceResolver(initialResolver, PUBLISHED_LABEL);
            return intraSlingCaller.call(
                    intraSlingCaller.createContext()
                            .setResourceResolver(targetResolver)
                            .setPath(resource.getPath())
                            .setExtension(extension)
            );
        } catch(IntraSlingCaller.CallException e) {
            throw new RenderException(FAILED_TO_RENDER_RESOURCE + e.getMessage(), e);
        }
    }

    public String renderInternally(Resource resource, String extension) throws RenderException {
        byte[] response = renderRawInternally(resource, extension);
        return new String(response);
    }

}
