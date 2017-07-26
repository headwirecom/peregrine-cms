package com.peregrine.rendition;

//import com.drew.imaging.ImageMetadataReader;
//import com.drew.imaging.ImageProcessingException;
//import com.drew.metadata.Directory;
//import com.drew.metadata.Metadata;
//import com.drew.metadata.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.adaption.PerAsset;
import com.peregrine.adaption.PerPage;
//import com.peregrine.admin.replication.ImageMetadataSelector;
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
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.commons.util.PerConstants.JCR_LAST_MODIFIED_BY;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getResource;

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

//    @Reference
//    ResourceRelocation resourceRelocation;
    @Reference
    MimeTypeService mimeTypeService;
    @Reference
    private ImageTransformationConfigurationProvider imageTransformationConfigurationProvider;
    @Reference
    private ImageTransformationProvider imageTransformationProvider;

//    private List<ImageMetadataSelector> imageMetadataSelectors = new ArrayList<>();
//    @Reference(
//        cardinality = ReferenceCardinality.MULTIPLE,
//        policy = ReferencePolicy.DYNAMIC
//    )
//    void addImageMetadataSelector(ImageMetadataSelector selector)    { imageMetadataSelectors.add(selector); }
//    void removeImageMetadataSelector(ImageMetadataSelector selector) { imageMetadataSelectors.remove(selector); }


    private final Logger logger = LoggerFactory.getLogger(getClass());


//    @Override
//    public Resource createAssetFromStream(Resource parent, String assetName, String contentType, InputStream inputStream) throws ManagementException {
//        Resource answer = null;
//        if(parent == null) {
//            throw new ManagementException("Parent Resource must be provided to create Asset");
//        }
//        if(assetName == null || assetName.isEmpty()) {
//            throw new ManagementException("Asset Name must be provided to create Asset");
//        }
//        if(contentType == null || contentType.isEmpty()) {
//            throw new ManagementException("Content Type must be provided to create Asset");
//        }
//        if(inputStream == null) {
//            throw new ManagementException("Input Stream must be provided to create Asset");
//        }
//        try {
//            Node parentNode = parent.adaptTo(Node.class);
//            Node newAsset = parentNode.addNode(assetName, ASSET_PRIMARY_TYPE);
//            Node content = newAsset.addNode(JCR_CONTENT, ASSET_CONTENT_TYPE);
//            Binary data = parentNode.getSession().getValueFactory().createBinary(inputStream);
//            content.setProperty(JCR_DATA, data);
//            content.setProperty(JCR_MIME_TYPE, contentType);
//            updateModification(parent.getResourceResolver(), newAsset);
//
//            answer = parent.getResourceResolver().getResource(newAsset.getPath());
//            PerAsset perAsset = answer.adaptTo(PerAsset.class);
//            try {
//                Metadata metadata = ImageMetadataReader.readMetadata(perAsset.getRenditionStream((Resource) null));
//                for(Directory directory : metadata.getDirectories()) {
//                    String directoryName = directory.getName();
//                    ImageMetadataSelector selector = null;
//                    for(ImageMetadataSelector item : imageMetadataSelectors) {
//                        String temp = item.acceptCategory(directoryName);
//                        if(temp != null) {
//                            selector = item;
//                            directoryName = temp;
//                        }
//                    }
//                    boolean asJson = selector != null && selector.asJsonProperty();
//                    String json = "{";
//                    for(Tag tag : directory.getTags()) {
//                        String name = tag.getTagName();
//                        String tagName = selector != null ? selector.acceptTag(name) : name;
//                        if(tagName != null) {
//                            logger.debug("Add Tag, Category: '{}', Tag Name: '{}', Value: '{}'", new Object[]{directoryName, tagName, tag.getDescription()});
//                            if(asJson) {
//                                json += "\"" + tagName + "\":\"" + tag.getDescription() + "\",";
//                            } else {
//                                perAsset.addTag(directoryName, tagName, tag.getDescription());
//                            }
//                        }
//                    }
//                    if(asJson) {
//                        if(json.length() > 1) {
//                            json = json.substring(0, json.length() - 1);
//                            json += "}";
//                            perAsset.addTag(directoryName, "raw_tags", json);
//                        }
//                    }
//                }
//            } catch(ImageProcessingException e) {
//                e.printStackTrace();
//            }
//        } catch(RepositoryException e) {
//            throw new ManagementException("Failed to Create Asset Node in Parent: " + parent.getPath() + ", name: " + assetName, e);
//        } catch(IOException e) {
//            throw new ManagementException("Failed to Create Rendition in Parent: " + parent.getPath() + ", name: " + assetName, e);
//        }
//        return answer;
//    }

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

//    @Override
//    public Resource updateResource(ResourceResolver resourceResolver, String path, String jsonContent) throws ManagementException {
//        Resource answer = null;
//        try {
//            answer = getResource(resourceResolver, path);
//            if(answer == null) {
//                throw new ManagementException("Resource not found, Path: " + path);
//            }
//            if(jsonContent == null || jsonContent.isEmpty()) {
//                throw new ManagementException("No Content provided, Path: " + path);
//            }
//            Map content = convertToMap(jsonContent);
//            //AS TODO: Check if we could add some guards here to avoid misplaced updates (JCR Primary Type / Sling Resource Type)
//            updateResourceTree(answer, content);
//        } catch(IOException e) {
//            throw new ManagementException("Failed to parse Json Content: " + jsonContent);
//        }
//        return answer;
//    }
//
//    private void updateResourceTree(Resource resource, Map<String, Object> properties) throws ManagementException {
//        ModifiableValueMap updateProperties = getModifiableProperties(resource, false);
//        for(Entry<String, Object> entry: properties.entrySet()) {
//            String name = entry.getKey();
//            Object value = entry.getValue();
//            if(value instanceof Map) {
//                Resource child = resource.getChild(name);
//                if(child == null) {
//                    throw new ManagementException("Property: '" + name + "' is a map but not child resource found with that name");
//                }
//                updateResourceTree(child, (Map) value);
//            } else {
//                updateProperties.put(name, value);
//            }
//        }
//        updateModification(resource);
//    }

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

//    public static Map convertToMap(String json) throws IOException {
//        Map answer = new LinkedHashMap();
//        if(json != null) {
//            ObjectMapper mapper = new ObjectMapper();
//            answer = mapper.readValue(json, LinkedHashMap.class);
//        }
//        return answer;
//    }
//
//    private Node createPageOrTemplate(Resource parent, String name, String templateComponent, String templatePath) throws RepositoryException {
//        Node parentNode = parent.adaptTo(Node.class);
//        Node newPage = null;
//        newPage = parentNode.addNode(name, PAGE_PRIMARY_TYPE);
//        Node content = newPage.addNode(JCR_CONTENT);
//        content.setPrimaryType(PAGE_CONTENT_TYPE);
//        content.setProperty(SLING_RESOURCE_TYPE, templateComponent);
//        content.setProperty(JCR_TITLE, name);
//        if(templatePath != null) {
//            content.setProperty(TEMPLATE, templatePath);
//        }
//        updateModification(parent.getResourceResolver(), newPage);
//        return newPage;
//    }
}
