package com.peregrine.render;

import com.peregrine.render.mock.MockRequestPathInfo;
import com.peregrine.render.mock.MockSlingHttpServletRequest;
import com.peregrine.render.mock.MockSlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.peregrine.commons.util.PerUtil.isEmpty;

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

    private static final String UNSUPPORTED_ENCODING_WHILE_CREATING_THE_RENDER_RESPONSE = "Unsupported Encoding while creating the Render Response";
    private static final String FAILED_TO_RENDER_RESOURCE = "Failed to render resource: ";
    private static final String RENDERING_REQUEST_FAILED = "Rendering Request: '%s' failed with status: '%s'";

    protected static final Logger LOGGER = LoggerFactory.getLogger(RenderServiceImpl.class);

    @Reference
    @SuppressWarnings("unused")
    private SlingRequestProcessor requestProcessor;

    public byte[] renderRawInternally(Resource resource, String extension) throws RenderException {
        MockSlingHttpServletResponse response = renderResource0(resource, extension);
        return response.getOutput();
    }

    public String renderInternally(Resource resource, String extension) throws RenderException {
        MockSlingHttpServletResponse response = renderResource0(resource, extension);
        return response.getOutputAsString();
    }

    private MockSlingHttpServletResponse renderResource0(Resource resource, String extension) throws RenderException {
        try {
            MockSlingHttpServletRequest req = new MockSlingHttpServletRequest(resource.getResourceResolver());
            MockRequestPathInfo mrpi = (MockRequestPathInfo) req.getRequestPathInfo();
            mrpi.setResourcePath(resource.getPath());
            if(isEmpty(extension)) {
                extension = "";
            }
            LOGGER.trace("Render Resource Request Extension: '{}'", extension);
            mrpi.setExtension(extension);
            String requestPath = resource.getPath() + "." + extension;
            LOGGER.trace("Render Resource Request Path: '{}'", mrpi);
            MockSlingHttpServletResponse resp = new MockSlingHttpServletResponse();
            requestProcessor.processRequest(req, resp, resource.getResourceResolver());
            LOGGER.trace("Response Status: '{}'", resp.getStatus());
            //AS TODO: do we need to support redirects (301 / 302)
            if(resp.getStatus() != 200) {
                String content = resp.getOutputAsString();
                LOGGER.error("Request of: '{}' failed (status: {}). Output : '{}'", requestPath, resp.getStatus(), content);
                throw new RenderException(String.format(RENDERING_REQUEST_FAILED, requestPath, resp.getStatus()));
            } else {
                return resp;
            }
        } catch(UnsupportedEncodingException e) {
            throw new RenderException(UNSUPPORTED_ENCODING_WHILE_CREATING_THE_RENDER_RESPONSE, e);
        } catch(ServletException | IOException e) {
            throw new RenderException(FAILED_TO_RENDER_RESOURCE + resource.getPath(), e);
        }
    }
}
