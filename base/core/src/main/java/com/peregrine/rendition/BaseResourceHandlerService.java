package com.peregrine.rendition;

import com.peregrine.adaption.PerAsset;
import com.peregrine.adaption.PerPage;
import com.peregrine.commons.util.PerConstants;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.transform.ImageContext;
import com.peregrine.transform.ImageTransformation;
import com.peregrine.transform.ImageTransformation.TransformationException;
import com.peregrine.transform.ImageTransformationConfiguration;
import com.peregrine.transform.ImageTransformationConfigurationProvider;
import com.peregrine.transform.ImageTransformationProvider;
import com.peregrine.transform.OperationContext;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED_BY;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;

/**
 * Created by schaefa on 7/6/17.
 */
@Component(
    service = BaseResourceHandler.class,
    immediate = true
)
public class BaseResourceHandlerService
    implements BaseResourceHandler
{
    public static final String ETC_FELIBS_ADMIN_IMAGES_BROKEN_IMAGE_SVG = "/etc/felibs/admin/images/broken-image.svg";

    @Reference
    MimeTypeService mimeTypeService;
    @Reference
    private ImageTransformationConfigurationProvider imageTransformationConfigurationProvider;
    @Reference
    private ImageTransformationProvider imageTransformationProvider;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ImageContext createRendition(Resource resource, String renditionName, String sourceMimeType) throws HandlerException {
        if(resource == null) {
            throw new HandlerException("No Asset Resource provided");
        }
        PerAsset asset = resource.adaptTo(PerAsset.class);
        if(asset == null) {
            throw new HandlerException("Resource: " + resource.getPath() + " could not be adapted to an Asset");
        }
        if(renditionName == null || renditionName.isEmpty()) {
            throw new HandlerException("No Rendition Name provided");
        }
        if(sourceMimeType == null || sourceMimeType.isEmpty()) {
            throw new HandlerException("No Source Mime Type provided");
        }
        int index = renditionName.indexOf('/');
        if(index >= 0) {
            renditionName = renditionName.substring(index + 1);
        }
        ImageContext answer = null;

        String targetMimeType = mimeTypeService.getMimeType(renditionName);

        List<ImageTransformationConfiguration> imageTransformationConfigurationList =
            imageTransformationConfigurationProvider.getImageTransformationConfigurations(renditionName);
        if(imageTransformationConfigurationList != null) {
            InputStream assetRenditionStream = asset.getRenditionStream(renditionName);
            if(assetRenditionStream != null) {
                answer = new ImageContext(sourceMimeType, targetMimeType, assetRenditionStream);
            }
            if(answer == null) {
                try {
                    InputStream sourceStream = asset.getRenditionStream((Resource) null);
                    if(sourceStream != null) {
                        ImageContext imageContext = transform(renditionName, sourceMimeType, sourceStream, targetMimeType, imageTransformationConfigurationList, false);
                        asset.addRendition(renditionName, imageContext.getImageStream(), targetMimeType);
                        updateModification(asset.getResource());
                        answer.resetImageStream(asset.getRenditionStream(renditionName));
                    } else {
                        logger.error("Resource: '{}' does not contain a data element", resource.getName());
                    }
                } catch(TransformationException e) {
                    logger.error("Transformation failed, image ignore", e);
                } catch(RepositoryException e) {
                    logger.error("Failed to create Rendition Node for Resource: '{}', rendition name: '{}'", resource, renditionName);
                } catch(PersistenceException e) {
                    logger.error("Failed to save Rendition Node for Resource: '{}', rendition name: '{}'", resource, renditionName);
                }
            }
            if(answer == null) {
                // Rendition was not found and could not be created therefore load and thumbnail the broken image
                String imagePath = ETC_FELIBS_ADMIN_IMAGES_BROKEN_IMAGE_SVG;
                Resource brokenImageResource = resource.getResourceResolver().getResource(imagePath);
                if(brokenImageResource != null) {
                    try {
                        InputStream brokenImageStream = getDataStream(brokenImageResource);
                        imageTransformationConfigurationList = imageTransformationConfigurationProvider.getImageTransformationConfigurations("thumbnail.png");
                        answer = transform(renditionName, "image/svg+xml", brokenImageStream, "image/png", imageTransformationConfigurationList, true);
                    } catch(TransformationException e) {
                        logger.error("Transformation failed, image ignore", e);
                    }
                }
            }
        }
        return answer;
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
                // Disabled Transformations will stop the rendition creation as it does create incomplete or non-renditioned images
                imageTransformation.transform(imageContext, operationContext);
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
        if(resource != null && !PerConstants.JCR_CONTENT.equals(resource.getName())) {
            resource = resource.getChild(PerConstants.JCR_CONTENT);
        }
        if(resource != null) {
            ValueMap properties = resource != null ? resource.getValueMap() : null;
            answer = properties != null ? properties.get(PerConstants.JCR_DATA, InputStream.class) : null;
        }
        return answer;
    }

    @Override
    public void updateModification(ResourceResolver resourceResolver, Node node) {
        if(resourceResolver != null && node != null) {
            try {
                Resource resource = resourceResolver.getResource(node.getPath());
                updateModification(resource);
            } catch(RepositoryException e) {
                // Ignore
            }
        }
    }

    /** @param resource Adds / Update the Last Modified and Last Modified By property on this resource **/
    @Override
    public void updateModification(Resource resource) {
        if(resource != null) {
            String user = resource.getResourceResolver().getUserID();
            Calendar now = Calendar.getInstance();
            ModifiableValueMap properties = getModifiableProperties(resource, false);
            properties.put(JCR_LAST_MODIFIED_BY, user);
            properties.put(JCR_LAST_MODIFIED, now);
            PerPage page = resource.adaptTo(PerPage.class);
            if(page != null) {
                page.markAsModified();
            } else {
                Resource jcrContent = PerUtil.getResource(resource, JCR_CONTENT);
                if(jcrContent != null) {
                    properties = getModifiableProperties(jcrContent, false);
                    properties.put(JCR_LAST_MODIFIED_BY, user);
                    properties.put(JCR_LAST_MODIFIED, now);
                }
            }
        }
    }
}
