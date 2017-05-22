package com.peregrine.admin.servlets;

import com.peregrine.admin.transform.ImageContext;
import com.peregrine.admin.transform.ImageTransformation;
import com.peregrine.admin.transform.ImageTransformation.DisabledTransformationException;
import com.peregrine.admin.transform.ImageTransformation.TransformationException;
import com.peregrine.admin.transform.ImageTransformationFactory;
import com.peregrine.admin.transform.OperationContext;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
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
@Designate(
    ocd = RenditionsConfiguration.class
)
@SuppressWarnings("serial")
/**
 * This servlet provides renditions of Peregrine Assets (per:Asset)
 * and creates them if they are not available yet
 */
public class RenditionsServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(RenditionsServlet.class);

    @Reference
    private ImageTransformationFactory imageTransformationFactory;

    private List<RenditionType> renditionTypeList = new ArrayList<>();

    @Activate
    private void activate(final RenditionsConfiguration configuration) {
        log.debug("activate");
        configure(configuration);
    }

    @Modified
    private void modified(final RenditionsConfiguration configuration) {
        log.debug("modified");
        configure(configuration);
    }

    @Deactivate
    protected void deactivate() {
        log.info("deactivate");
    }

    private void configure(final RenditionsConfiguration configuration) {
        String[] renditions = configuration.renditions();
        for(String rendition: renditions) {
            renditionTypeList.add(
                new RenditionType(rendition)
            );
        }
    }

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
            if(suffix != null && suffix.length() > 0) {
                // Check if we support that renditions
                RenditionType renditionType = null;
                for(RenditionType type : renditionTypeList) {
                    if(suffix.endsWith(type.getName())) {
                        renditionType = type;
                        break;
                    }
                }
                //AS TODO: Handle if rendition is not supported
                if(renditionType != null) {
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
                        Resource rendition = renditions.getChild(renditionType.getName());
                        if(rendition == null) {
                            try {
                                Session session = request.getResourceResolver().adaptTo(Session.class);
                                Node renditionsNode = renditions.adaptTo(Node.class);
                                Node renditionNode = renditionsNode.addNode(renditionType.getName(), "nt:file");
                                Node jcrContent = renditionNode.addNode("jcr:content", "nt:resource");
                                Binary source = resourceJcrContentNode.getProperty("jcr:data").getBinary();

                                ImageTransformation imageTransformation = imageTransformationFactory.getImageTransformation(renditionType.getTransformationName());
                                if(imageTransformation != null) {
                                    // Obtain the Image Type
                                    int index = resource.getName().lastIndexOf('.');
                                    String imageType = "png";
                                    if(index > 0 && index < resource.getName().length() - 3) {
                                        imageType = resource.getName().substring(index + 1);
                                    }
                                    ImageContext imageContext = new ImageContext(
                                        imageType,
                                        source.getStream()
                                    );
                                    Map<String, String> parameters = new HashMap<>();
                                    String parameter = renditionType.getParameter("height");
                                    if(parameter == null || parameter.isEmpty()) {
                                        throw new IllegalArgumentException("Height for Rendition Type: '" + renditionType.getName() + "' is empty" );
                                    }
                                    parameters.put("height", parameter);
                                    parameter = renditionType.getParameter("width");
                                    if(parameter == null || parameter.isEmpty()) {
                                        throw new IllegalArgumentException("Width for Rendition Type: '" + renditionType.getName() + "' is empty" );
                                    }
                                    parameters.put("width", parameter);
                                    OperationContext operationContext = new OperationContext(renditionType.getName(), parameters);
                                    try {
                                        imageTransformation.transform(imageContext, operationContext);
                                        Binary data = session.getValueFactory().createBinary(imageContext.getImageStream());
                                        jcrContent.setProperty("jcr:data", data);
                                        jcrContent.setProperty("jcr:mimeType", "application/octet-stream");
                                        session.save();
                                        rendition = renditions.getChild(renditionType.getName());
                                    } catch(DisabledTransformationException e) {
                                        log.error("Transformation disabled and hence ignored", e);
                                    } catch(TransformationException e) {
                                        log.error("Transformation Failed", e);
                                    }
                                }
                            } catch(RepositoryException e) {
                                log.error("Failed to create Rendition Node for Resource: '{}', rendition name: '{}'", resource, renditionType.getName());
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                e.printStackTrace(response.getWriter());
                            }
                        }
                        if(rendition != null) {
                            resource = rendition;
                        }
                    }
                }
            }
            if(resource != null) {
                // Read image as is and send back
                Resource jcrContent = resource.getChild("jcr:content");
                InputStream is = jcrContent != null ? jcrContent.getValueMap().get("jcr:data", InputStream.class) : null;
                if(is != null) {
                    response.setContentType("application/octet-stream");
                    OutputStream os = response.getOutputStream();
                    IOUtils.copy(is, os);
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(os);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    //AS TODO: set response body
                }
            }
        } catch(RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace(response.getWriter());
        }
    }

    public static class RenditionType {
        private String name;
        private String transformationName;
        private Map<String, String> parameters = new HashMap<>();

        public RenditionType(String format) {
            String[] tokens = format.split("\\|");
            if(tokens.length == 0) {
                throw new IllegalArgumentException("Rendition Type format has no name, format: '" + format + "'. " + getFormat());
            }
            this.name = tokens[0];
            if(this.name == null || this.name.isEmpty()) {
                throw new IllegalArgumentException("Rendition Type format's name is not provided, format: '" + format + "'. " + getFormat());
            }
            if(tokens.length == 1) {
                throw new IllegalArgumentException("Rendition Type format has no transformation name, format: '" + format + "'. " + getFormat());
            }
            String temp = tokens[1];
            this.transformationName = tokens[1];
            if(temp == null || temp.isEmpty()) {
                throw new IllegalArgumentException("Rendition Type format's transformation name is not provided, format: '" + format + "'. " + getFormat());
            } else {
                int index = temp.indexOf('=');
                if(index <= 0 || index >= temp.length() - 1) {
                    throw new IllegalArgumentException("Rendition Type format's transformation name is not of type transformation=value , format: '" + format + "'. " + getFormat());
                } else {
                    String key = temp.substring(0, index);
                    String name = temp.substring(index + 1);
                    if(!"transformation".equals(key)) {
                        throw new IllegalArgumentException("Rendition Type format's transformation name does not start with 'transformation' , format: '" + format + "'. " + getFormat());
                    } else if(name.isEmpty()) {
                        throw new IllegalArgumentException("Rendition Type format's transformation name value is not provided , format: '" + format + "'. " + getFormat());
                    } else {
                        this.transformationName = name;
                    }
                }
            }
            for(int i = 2; i < tokens.length; i++) {
                String value = tokens[i];
                if(value == null || value.isEmpty()) {
                    throw new IllegalArgumentException("Rendition Type format's contains empty token, format: '" + format + "' on position: " + (i + 1) + "). " + getFormat());
                }
                int index = value.indexOf('=');
                if(index <= 0 || index >= value.length() - 1) {
                    throw new IllegalArgumentException("Rendition Type format's token is not of type key=value , format: '" + format + "' on position: " + (i + 1) + "). " + getFormat());
                }
                parameters.put(value.substring(0, index), value.substring(index + 1));
            }
        }

        public String getName() {
            return name;
        }

        public String getTransformationName() {
            return transformationName;
        }

        public String getParameter(String name) {
            return parameters.get(name);
        }

        private String getFormat() {
            return "Expected Format: <name>|transformation=<transformation name>|<a | separated list of <parameter>=<value>>";
        }
    }
}

