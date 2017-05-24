package com.peregrine.admin.servlets;

import com.peregrine.admin.transform.ImageTransformationConfiguration;
import com.peregrine.admin.transform.ImageContext;
import com.peregrine.admin.transform.ImageTransformation;
import com.peregrine.admin.transform.ImageTransformation.DisabledTransformationException;
import com.peregrine.admin.transform.ImageTransformation.TransformationException;
import com.peregrine.admin.transform.ImageTransformationConfigurationProvider;
import com.peregrine.admin.transform.ImageTransformationProvider;
import com.peregrine.admin.transform.OperationContext;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=rendition provider servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES +"=per/Asset"
        }
)
@SuppressWarnings("serial")
/**
 * This servlet provides renditions of Peregrine Assets (per:Asset)
 * and creates them if they are not available yet
 */
public class RenditionsServlet extends SlingSafeMethodsServlet {

    public static final String JCR_CONTENT = "jcr:content";
    private final Logger log = LoggerFactory.getLogger(RenditionsServlet.class);

    @Reference
    private ImageTransformationConfigurationProvider imageTransformationConfigurationProvider;
    @Reference
    private ImageTransformationProvider imageTransformationProvider;
    @Reference
    MimeTypeService mimeTypeService;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {
        Resource resource = request.getResource();
        Resource jcrContent = resource.getChild(JCR_CONTENT);
        if(jcrContent == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Given Resource has no Content");
            return;
        }
        // Check if there is a suffix
        String suffix = request.getRequestPathInfo().getSuffix();
        String sourceMimeType = jcrContent.getValueMap().get("jcr:mimeType", "");
        if(sourceMimeType.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Given Resource has no Mime Type");
            return;
        }
        if(suffix != null && suffix.length() > 0) {
            // Get final Rendition Name and Mime Type
            String renditionName = suffix;
            int index = renditionName.indexOf('/');
            if(index >= 0) {
                renditionName = renditionName.substring(index + 1);
            }
            String targetMimeType = mimeTypeService.getMimeType(renditionName);

            List<ImageTransformationConfiguration> imageTransformationConfigurationList =
                imageTransformationConfigurationProvider.getImageTransformationConfigurations(renditionName);
            if(imageTransformationConfigurationList != null) {
                // Obtain renditions folder and the rendition. If rendition is not found then take the
                // source asset and transform it
                Resource renditions = createRenditionsFolder(request, resource);
                Resource rendition = renditions != null ? renditions.getChild(renditionName) : null;
                if(rendition == null) {
                    try {
                        InputStream sourceStream = getDataStream(jcrContent);
                        if(sourceStream != null) {
                            ImageContext imageContext = transform(renditionName, sourceMimeType, sourceStream, targetMimeType, imageTransformationConfigurationList, false);
                            createAndSaveRenditionBinary(request, renditions, renditionName, targetMimeType, imageContext.getImageStream());
                            rendition = renditions.getChild(renditionName);
                        } else {
                            log.error("Resource: '{}' does not contain a data element", resource.getName());
                        }
                    } catch(TransformationException e) {
                        log.error("Transformation failed, image ignore", e);
                    } catch(RepositoryException e) {
                        log.error("Failed to create Rendition Node for Resource: '{}', rendition name: '{}'", resource, renditionName);
                    }
                }
                InputStream sourceStream = rendition != null ? getDataStream(rendition) : null;
                if(sourceStream == null) {
                    // Rendition was not found and could not be created therefore load and thumbnail the broken image
                    String imagePath = "/etc/felibs/admin/images/broken-image.svg";
                    Resource brokenImageResource = request.getResourceResolver().getResource(imagePath);
                    if(brokenImageResource != null) {
                        try {
                            InputStream brokenImageStream = getDataStream(brokenImageResource);
                            imageTransformationConfigurationList =
                                imageTransformationConfigurationProvider.getImageTransformationConfigurations("thumbnail.png");
                            ImageContext imageContext = transform(renditionName, "image/svg+xml", brokenImageStream, "image/png", imageTransformationConfigurationList, true);
                            sourceStream = imageContext.getImageStream();
                        } catch(TransformationException e) {
                            log.error("Transformation failed, image ignore", e);
                        }
                    }
                }
                if(sourceStream != null) {
                    // Got a output stream -> send it back
                    streamToResponse(response, sourceStream, targetMimeType);
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Failed to load or transform the broken image");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("No Rendition Transformation found for: " + renditionName);
                return;
            }
        } else {
            // This was not a request for a rendition but just the original asset -> load and send it back
            InputStream sourceStream = getDataStream(jcrContent);
            if(sourceStream != null) {
                streamToResponse(response, sourceStream, sourceMimeType);
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("No Data Stream found for requested resource");
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("Could not handle rendition or original asset");
    }

    /**
     * Takes the given source data streams and transforms it into the desired rendition
     * @param renditionName Name of the Rendition (node name)
     * @param sourceMimeType Mime Type of the Source
     * @param sourceStream Data Stream of the Source
     * @param targetMimeType Desired Target Mime Type
     * @param imageTransformationConfigurationList List of Image Transformation Configuration
     * @param noCrop If true then a thumbnail will not crop the image and hence the image might be smaller or shorter
     * @return Image Context that contains the final Data Stream
     * @throws TransformationException If the Transformation failed
     */
    private ImageContext transform(
        String renditionName, String sourceMimeType, InputStream sourceStream, String targetMimeType,
        List<ImageTransformationConfiguration> imageTransformationConfigurationList, boolean noCrop
    )
        throws TransformationException
    {
        ImageContext imageContext = new ImageContext(sourceMimeType, targetMimeType, sourceStream);

        for(ImageTransformationConfiguration imageTransformationConfiguration : imageTransformationConfigurationList) {
            ImageTransformation imageTransformation = imageTransformationProvider.getImageTransformation(imageTransformationConfiguration.getTransformationName());
            if(imageTransformation != null) {
                Map<String, String> parameters = null;
                if(noCrop) {
                    parameters = new HashMap<>(imageTransformationConfiguration.getParameters());
                    parameters.put("noCrop", "true");
                } else {
                    parameters = imageTransformationConfiguration.getParameters();
                }
                OperationContext operationContext = new OperationContext(renditionName, parameters);
                try {
                    imageTransformation.transform(imageContext, operationContext);
                } catch(DisabledTransformationException e) {
                    log.error("Transformation disabled and hence ignored", e);
                }
            }
        }
        return imageContext;
    }

    /**
     * Obtains the Data Stream of the given resource
     * @param resource Resource containing the data. If this is not the jcr:content node then this node will be obtained first
     * @return Input Stream if resource, properties and data property was found otherwise <code>null</code>
     */
    private InputStream getDataStream(Resource resource) {
        InputStream answer = null;
        if(resource != null && !JCR_CONTENT.equals(resource.getName())) {
            resource = resource.getChild(JCR_CONTENT);
        }
        if(resource != null) {
            ValueMap properties = resource != null ? resource.getValueMap() : null;
            answer = properties != null ? properties.get("jcr:data", InputStream.class) : null;
        }
        return answer;
    }

    /**
     * Creates the rendition resource, sets the mime type and add the data
     * @param request Request to obtain the session
     * @param renditions Parent Folder (renditions) where this resource is created underneath
     * @param renditionName Name of the Rendition
     * @param mimeType Mime Type of the Rendition
     * @param inputStream Content Data to be added to the resource
     * @throws RepositoryException If node could not be created, data added or session saved
     */
    private void createAndSaveRenditionBinary(SlingHttpServletRequest request, Resource renditions, String renditionName, String mimeType, InputStream inputStream)
        throws RepositoryException
    {
        Session session = request.getResourceResolver().adaptTo(Session.class);
        Node renditionsNode = renditions.adaptTo(Node.class);
        Node renditionNode = renditionsNode.addNode(renditionName, "nt:file");
        Node jcrContent = renditionNode.addNode("jcr:content", "nt:resource");
        Binary data = session.getValueFactory().createBinary(inputStream);
        jcrContent.setProperty("jcr:data", data);
        jcrContent.setProperty("jcr:mimeType", mimeType);
        session.save();
    }

    /**
     * Creates a renditions folder under the given resource if not already there and
     * then returns it
     *
     * @param request Servlet Request
     * @param parent Resource where the folder is added to
     * @return Renditions folder if found otherwise <code>null</code>
     */
    private Resource createRenditionsFolder(SlingHttpServletRequest request, Resource parent) {
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource renditions = parent.getChild("renditions");
        if(renditions == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("jcr:primaryType", "sling:Folder");
            try {
                renditions = resourceResolver.create(parent, "renditions", properties);
            } catch(PersistenceException e) {
                log.error("Failed to create 'renditions' folder in resource: " + parent.getPath());
            }
        }
        return renditions;
    }

    /**
     * Streams the given Input Stream of the given resource to the
     * HTTP Servlet Response. The given source of data and the response output
     * stream is closed after this operation
     *
     * @param response Servlet Response to write to
     * @param sourceStream Source of the data
     * @param mimeType Mime Type of the data to make sure the data is displayed correctly
     * @throws IOException If the opening of the response output stream or the copying of the data failed
     */
    private void streamToResponse(HttpServletResponse response, InputStream sourceStream, String mimeType)
        throws IOException
    {
        if(sourceStream != null) {
            OutputStream os = null;
            try {
                response.setContentType(mimeType);
                os = response.getOutputStream();
                IOUtils.copy(sourceStream, os);
            } finally {
                IOUtils.closeQuietly(sourceStream);
                IOUtils.closeQuietly(os);
            }
        }
    }
}

