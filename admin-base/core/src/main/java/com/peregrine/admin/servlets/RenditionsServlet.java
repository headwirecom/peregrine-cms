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
import java.util.ArrayList;
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

    private final Logger log = LoggerFactory.getLogger(RenditionsServlet.class);

    @Reference
    private ImageTransformationConfigurationProvider imageTransformationConfigurationProvider;
    @Reference
    private ImageTransformationProvider imageTransformationProvider;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {
        try {
            Resource resource = request.getResource();
            Node resourceNode = resource.adaptTo(Node.class);
            Node resourceJcrContentNode = resourceNode.getNode("jcr:content");
            // Check if there is a suffix
            String suffix = request.getRequestPathInfo().getSuffix();
            String sourceImageType = "png";
            int index = resource.getName().lastIndexOf('.');
            if(index > 0 && index < resource.getName().length() - 1) {
                sourceImageType = resource.getName().substring(index + 1);
            }
            String targetImageType = "png";
            if(suffix != null && suffix.length() > 0) {
                // Check if we support that renditions
                String renditionName = suffix;
                index = renditionName.indexOf('/');
                if(index >= 0) {
                    renditionName = renditionName.substring(index + 1);
                }
                List<ImageTransformationConfiguration> imageTransformationConfigurationList =
                    imageTransformationConfigurationProvider.getImageTransformationConfigurations(renditionName);
                //AS TODO: Handle if rendition is not supported
                if(imageTransformationConfigurationList != null) {
                    // Check if the file exists
                    ResourceResolver resourceResolver = request.getResourceResolver();
                    Resource renditions = resource.getChild("renditions");
                    if(renditions == null) {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("jcr:primaryType", "sling:Folder");
                        try {
                            renditions = resourceResolver.create(resource, "renditions", properties);
                        } catch(PersistenceException e) {
                            log.error("Failed to create 'renditions' folder in resource: " + resource.getPath());
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            e.printStackTrace(response.getWriter());
                        }
                    }
                    if(renditions != null) {
                        Resource rendition = renditions.getChild(renditionName);
                        if(rendition == null) {
                            try {
                                Session session = request.getResourceResolver().adaptTo(Session.class);
                                Node renditionsNode = renditions.adaptTo(Node.class);
                                Node renditionNode = renditionsNode.addNode(renditionName, "nt:file");
                                Node jcrContent = renditionNode.addNode("jcr:content", "nt:resource");
                                Binary source = resourceJcrContentNode.getProperty("jcr:data").getBinary();
                                // Obtain the Image Type
                                index = renditionName.lastIndexOf('.');
                                if(index > 0 && index < renditionName.length() - 3) {
                                    targetImageType = renditionName.substring(index + 1);
                                }
                                ImageContext imageContext = new ImageContext(sourceImageType, targetImageType, source.getStream());

                                for(ImageTransformationConfiguration imageTransformationConfiguration: imageTransformationConfigurationList) {
                                    ImageTransformation imageTransformation = imageTransformationProvider.getImageTransformation(imageTransformationConfiguration.getTransformationName());
                                    if(imageTransformation != null) {
                                        Map<String, String> parameters = imageTransformationConfiguration.getParameters();
                                        OperationContext operationContext = new OperationContext(renditionName, parameters);
                                        try {
                                            imageTransformation.transform(imageContext, operationContext);
                                        } catch(DisabledTransformationException e) {
                                            log.error("Transformation disabled and hence ignored", e);
                                        }
                                    }
                                }
                                Binary data = session.getValueFactory().createBinary(imageContext.getImageStream());
                                jcrContent.setProperty("jcr:data", data);
                                jcrContent.setProperty("jcr:mimeType", "image/" + targetImageType);
                                session.save();
                                rendition = renditions.getChild(renditionName);
                            } catch(TransformationException e) {
                                log.error("Transformation failed, image ignore", e);
                            } catch(RepositoryException e) {
                                log.error("Failed to create Rendition Node for Resource: '{}', rendition name: '{}'", resource, renditionName);
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                e.printStackTrace(response.getWriter());
                            }
                        }
                        InputStream sourceStream = null;
                        if(rendition != null) {
                            Resource jcrContent = rendition.getChild("jcr:content");
                            ValueMap properties = jcrContent != null ? jcrContent.getValueMap() : null;
                            sourceStream = properties != null ? properties.get("jcr:data", InputStream.class) : null;
                        }
                        if(sourceStream == null) {
                            // Try to load and thumbnail the broken-image image
                            String imagePath = "/etc/felibs/admin/images/broken-image.svg";
                            Resource brokenImageResource = renditions.getChild(imagePath);
                            if(brokenImageResource != null) {
                                try {
                                    Resource jcrContent = brokenImageResource.getChild("jcr:content");
                                    ValueMap properties = jcrContent != null ? jcrContent.getValueMap() : null;
                                    InputStream is = properties != null ? properties.get("jcr:data", InputStream.class) : null;
                                    ImageContext imageContext = new ImageContext("svg", "png", is);

                                    imageTransformationConfigurationList =
                                        imageTransformationConfigurationProvider.getImageTransformationConfigurations("thumbnail.png");
                                    for(ImageTransformationConfiguration imageTransformationConfiguration: imageTransformationConfigurationList) {
                                        ImageTransformation imageTransformation = imageTransformationProvider.getImageTransformation(imageTransformationConfiguration.getTransformationName());
                                        if(imageTransformation != null) {
                                            Map<String, String> parameters = new HashMap<>(imageTransformationConfiguration.getParameters());
                                            parameters.put("noCrop", "true");
                                            OperationContext operationContext = new OperationContext(renditionName, parameters);
                                            try {
                                                imageTransformation.transform(imageContext, operationContext);
                                            } catch(DisabledTransformationException e) {
                                                log.error("Transformation disabled and hence ignored", e);
                                            }
                                        }
                                    }
                                    sourceStream = imageContext.getImageStream();
                                } catch(TransformationException e) {
                                    log.error("Transformation failed, image ignore", e);
//                                } catch(RepositoryException e) {
//                                    log.error("Failed to create Rendition Node for Resource: '{}', rendition name: '{}'", resource, renditionName);
//                                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                                    e.printStackTrace(response.getWriter());
                                }
                            }
                        }
                        if(sourceStream != null) {
                            response.setContentType("image/" + targetImageType);
                            OutputStream os = response.getOutputStream();
                            IOUtils.copy(sourceStream, os);
                            IOUtils.closeQuietly(sourceStream);
                            IOUtils.closeQuietly(os);
                            return;
                        }
                    }
                }
            } else {
                Binary source = resourceJcrContentNode.getProperty("jcr:data").getBinary();
                InputStream sourceStream = source.getStream();
                if(sourceStream != null) {
                    response.setContentType("image/" + targetImageType);
                    OutputStream os = response.getOutputStream();
                    IOUtils.copy(sourceStream, os);
                    IOUtils.closeQuietly(sourceStream);
                    IOUtils.closeQuietly(os);
                    return;
                }

            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //AS TODO: set response body
        } catch(RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace(response.getWriter());
        }
    }

}

