package com.peregrine.admin.resource;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.adaption.PerAsset;
import com.peregrine.admin.replication.ImageMetadataSelector;
import com.peregrine.admin.transform.ImageContext;
import com.peregrine.admin.transform.ImageTransformation;
import com.peregrine.admin.transform.ImageTransformation.TransformationException;
import com.peregrine.admin.transform.ImageTransformationConfiguration;
import com.peregrine.admin.transform.ImageTransformationConfigurationProvider;
import com.peregrine.admin.transform.ImageTransformationProvider;
import com.peregrine.admin.transform.OperationContext;
import com.peregrine.util.PerConstants;
import com.peregrine.util.PerUtil;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static com.peregrine.admin.servlets.RenditionsServlet.ETC_FELIBS_ADMIN_IMAGES_BROKEN_IMAGE_SVG;
import static com.peregrine.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_DATA;
import static com.peregrine.util.PerConstants.JCR_LAST_MODIFIED;
import static com.peregrine.util.PerConstants.JCR_LAST_MODIFIED_BY;
import static com.peregrine.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_TITLE;
import static com.peregrine.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.SLING_ORDERED_FOLDER;
import static com.peregrine.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.util.PerUtil.TEMPLATE;
import static com.peregrine.util.PerUtil.getModifiableProperties;
import static com.peregrine.util.PerUtil.getResource;

/**
 * Created by schaefa on 7/6/17.
 */
@Component(
    service = ResourceManagement.class,
    immediate = true
)
public class ResourceManagementService
    implements ResourceManagement
{
    public static final String TEMPLATE_COMPONENT = "example/components/page";

    @Reference
    ResourceRelocation resourceRelocation;
    @Reference
    MimeTypeService mimeTypeService;
    @Reference
    private ImageTransformationConfigurationProvider imageTransformationConfigurationProvider;
    @Reference
    private ImageTransformationProvider imageTransformationProvider;

    private List<ImageMetadataSelector> imageMetadataSelectors = new ArrayList<>();
    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC
    )
    void addImageMetadataSelector(ImageMetadataSelector selector)    { imageMetadataSelectors.add(selector); }
    void removeImageMetadataSelector(ImageMetadataSelector selector) { imageMetadataSelectors.remove(selector); }


    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Resource createFolder(ResourceResolver resourceResolver, String parentPath, String name) throws ManagementException {
        try {
            Resource parent = PerUtil.getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException("Could not find Folder Parent Resource. Path: " + parentPath + ", name: " + name);
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException("Folder Name is not provided. Path: " + parentPath);
            }
            Node parentNode =  parent.adaptTo(Node.class);
            Node newFolder = parentNode.addNode(name, SLING_ORDERED_FOLDER);
            newFolder.setProperty(JCR_TITLE, name);
            updateModification(resourceResolver, newFolder);
            newFolder.getSession().save();
            return resourceResolver.getResource(newFolder.getPath());
        } catch(RepositoryException e) {
            logger.debug("Failed to create Folder. Parent Path: '{}', Name: '{}'", parentPath, name);
            logger.error("Failed to create Folder", e);
            throw new ManagementException("Failed to handle folder node. Parent Path: " + parentPath + ", name: " + name, e);
        }
    }

    public Resource createObject(ResourceResolver resourceResolver, String parentPath, String name, String resourceType) throws ManagementException {
        try {
            Resource parent = PerUtil.getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException("Could not find Object Parent Resource. Path: " + parentPath + ", name: " + name);
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException("Object Name is not provided. Path: " + parentPath);
            }
            if(resourceType == null || resourceType.isEmpty()) {
                throw new ManagementException("Resource Type is not provided. Path: " + parentPath + ", name: " + name);
            }
            Node parentNode = parent.adaptTo(Node.class);
            Node newObject = parentNode.addNode(name, OBJECT_PRIMARY_TYPE);
            newObject.setProperty(SLING_RESOURCE_TYPE, resourceType);
            newObject.setProperty(JCR_TITLE, name);
            newObject.getSession().save();
            return resourceResolver.getResource(newObject.getPath());
        } catch(RepositoryException e) {
            logger.debug("Failed to create Object. Parent Path: '{}', Name: '{}'", parentPath, name);
            logger.error("Failed to create Object", e);
            throw new ManagementException("Failed to handle object node. Parent Path: " + parentPath + ", name: " + name, e);
        }
    }

    @Override
    public Resource createPage(ResourceResolver resourceResolver, String parentPath, String name, String templatePath) throws ManagementException {
        try {
            Resource parent = PerUtil.getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException("Could not find Page Parent Resource. Path: " + parentPath + ", name: " + name);
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException("Page Name is not provided. Path: " + parentPath);
            }
            Resource templateResource = PerUtil.getResource(resourceResolver, templatePath + "/" + JCR_CONTENT);
            if(templateResource == null) {
                throw new ManagementException("Could not find template with path: " + templatePath);
            }
            String templateComponent = templateResource.getValueMap().get(SLING_RESOURCE_TYPE, String.class);
            Node newPage = createPageOrTemplate(parent, name, templateComponent, templatePath);
            return resourceResolver.getResource(newPage.getPath());
        } catch(RepositoryException e) {
            logger.debug("Failed to create Page. Parent Path: '{}', Name: '{}', Template Path: '{}'", parentPath, name, templatePath);
            logger.error("Failed to create Page", e);
            throw new ManagementException("Failed to handle page node. Parent Path: " + parentPath + ", name: " + name, e);
        }
    }

    @Override
    public Resource createTemplate(ResourceResolver resourceResolver, String parentPath, String name) throws ManagementException {
        try {
            Resource parent = PerUtil.getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException("Could not find Template Parent Resource. Path: " + parentPath + ", name: " + name);
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException("Template Name is not provided. Path: " + parentPath);
            }
            Node newPage = createPageOrTemplate(parent, name, TEMPLATE_COMPONENT, null);
            return resourceResolver.getResource(newPage.getPath());
        } catch(RepositoryException e) {
            logger.debug("Failed to create Template. Parent Path: '{}', Name: '{}'", parentPath, name);
            logger.error("Failed to create Template", e);
            throw new ManagementException("Failed to handle template node. Parent Path: " + parentPath + ", name: " + name, e);
        }
    }

    @Override
    public DeletionResponse deleteResource(ResourceResolver resourceResolver, String path) throws ManagementException {
        return deleteResource(resourceResolver, path, null);
    }

    @Override
    public DeletionResponse deleteResource(ResourceResolver resourceResolver, String path, String primaryType) throws ManagementException {
        Resource resource = PerUtil.getResource(resourceResolver, path);
        if(resource == null) {
            throw new ManagementException("Could not find Resource for Deletion. Path: " + path);
        }
        try {
            String primaryTypeValue = resource.getValueMap().get(JCR_PRIMARY_TYPE, String.class);
            if(primaryType != null && !primaryType.isEmpty() && !primaryType.equals(primaryTypeValue)) {
                throw new ManagementException("Failed to Delete Resource: " + path + ". Expected Primary Type: " + primaryType + ", Actual Primary Type: " + primaryTypeValue);
            }
            Resource parent = resource.getParent();
            DeletionResponse response = new DeletionResponse()
                .setName(resource.getName())
                .setPath(resource.getPath())
                .setParentPath(parent != null ? parent.getPath() : "")
                .setType(resource.getValueMap().get(JCR_PRIMARY_TYPE, "not-found"));
            resourceResolver.delete(resource);
            resourceResolver.commit();
            return response;
        } catch (PersistenceException e) {
            throw new ManagementException("Failed to Delete Resource: " + path, e);
        }
    }

    @Override
    public Resource insertNode(Resource resource, Map<String, Object> properties, boolean addAsChild, boolean orderBefore) throws ManagementException {
        Resource answer = null;
        if(resource == null) {
            throw new ManagementException("To Insert a New Node the Reference Resource must be provided");
        }
        if(properties == null || properties.isEmpty()) {
            throw new ManagementException("To Insert a New Node the Node Properties must be provided");
        }
        try {
            Node node = resource.adaptTo(Node.class);
            Node newNode;
            if(addAsChild) {
                Resource firstChild = null;
                if(orderBefore) {
                    Iterator<Resource> i = resource.listChildren();
                    if(i.hasNext()) { firstChild = i.next(); }
                }
                newNode = createNode(node, properties);
                updateModification(resource.getResourceResolver(), newNode);
                if(firstChild != null) {
                    resourceRelocation.reorder(resource, newNode.getName(), firstChild.getName(), true);
                }
                answer = resource.getResourceResolver().getResource(newNode.getPath());
                updateModification(answer);
            } else {
                Node parent = node.getParent();
                newNode = createNode(parent, properties);
                updateModification(resource.getResourceResolver(), newNode);
                resourceRelocation.reorder(resource.getParent(), newNode.getName(), node.getName(), orderBefore);
                answer = resource.getResourceResolver().getResource(newNode.getPath());
                updateModification(answer);
            }
        } catch (RepositoryException e) {
            throw new ManagementException("Failed to insert node at: " + resource.getPath(), e);
        }
        return answer;
    }

    @Override
    public Resource moveNode(Resource fromResource, Resource toResource, boolean addAsChild, boolean orderBefore) throws ManagementException {
        Resource answer = null;
        if(fromResource == null) {
            throw new ManagementException("To Move a Node the Source Resource must be provided");
        }
        if(toResource == null) {
            throw new ManagementException("To Move a Node the Reference Resource must be provided");
        }
        try {
            answer = fromResource;
            if(addAsChild) {
                boolean sameParent = resourceRelocation.isChildOfParent(fromResource, toResource);
                if(!sameParent) {
                    //AS TODO: Shouldn't we try to update the references ?
                    // If not the same parent then just move as they are added at the end
                    answer = resourceRelocation.moveToNewParent(fromResource, toResource, false);
                }
                if(orderBefore || sameParent) {
                    // If we move to the front or if it is the same parent (move to the end)
                    // No Target Child Name means we move it the front for before and end for after
                    resourceRelocation.reorder(toResource, fromResource.getName(), null, orderBefore);
                }
                updateModification(answer);
            } else {
                // Both BEFORE and AFTER can be handled in one as the only difference is if added before or after
                // and if they are the same parent we means we only ORDER otherwise we MOVE first
                boolean sameParent = resourceRelocation.hasSameParent(fromResource, toResource);
                if(!sameParent) {
                    answer = resourceRelocation.moveToNewParent(fromResource, toResource.getParent(), false);
                }
                resourceRelocation.reorder(toResource.getParent(), fromResource.getName(), toResource.getName(), orderBefore);
                updateModification(answer);
            }
        } catch (Exception e) {
            logger.error("problems while moving", e);
            throw new ManagementException("Failed to Move Resource. From: '" + fromResource.getPath() + "' to: '" + toResource.getPath() + "'", e);
        }
        return answer;
    }


    @Override
    public Resource rename(Resource fromResource, String newName) throws ManagementException {
        Resource answer = null;
        if(fromResource == null) {
            throw new ManagementException("To Move a Node the Source Resource must be provided");
        }
        if(newName == null || newName.isEmpty()) {
            throw new ManagementException("Name to be renamed to must be provided");
        }
        if(newName.indexOf('/') >= 0) {
            throw new ManagementException("Name to be renamed to cannot contain a slash");
        }
        try {
            answer = resourceRelocation.rename(fromResource, newName, true);
            updateModification(answer);
        } catch (Exception e) {
            logger.error("problems while moving", e);
            throw new ManagementException("Failed to Move Resource. From: '" + fromResource.getPath() + "' to: '" + newName + "'", e);
        }
        return answer;
    }

    @Override
    public Resource createAssetFromStream(Resource parent, String assetName, String contentType, InputStream inputStream) throws ManagementException {
        Resource answer = null;
        if(parent == null) {
            throw new ManagementException("Parent Resource must be provided to create Asset");
        }
        if(assetName == null || assetName.isEmpty()) {
            throw new ManagementException("Asset Name must be provided to create Asset");
        }
        if(contentType == null || contentType.isEmpty()) {
            throw new ManagementException("Content Type must be provided to create Asset");
        }
        if(inputStream == null) {
            throw new ManagementException("Input Stream must be provided to create Asset");
        }
        try {
            Node parentNode = parent.adaptTo(Node.class);
            Node newAsset = parentNode.addNode(assetName, ASSET_PRIMARY_TYPE);
            Node content = newAsset.addNode(JCR_CONTENT, ASSET_CONTENT_TYPE);
            Binary data = parentNode.getSession().getValueFactory().createBinary(inputStream);
            content.setProperty(JCR_DATA, data);
            content.setProperty(JCR_MIME_TYPE, contentType);
            updateModification(parent.getResourceResolver(), newAsset);

            answer = parent.getResourceResolver().getResource(newAsset.getPath());
            PerAsset perAsset = answer.adaptTo(PerAsset.class);
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(perAsset.getRenditionStream((Resource) null));
                for(Directory directory : metadata.getDirectories()) {
                    String directoryName = directory.getName();
                    ImageMetadataSelector selector = null;
                    for(ImageMetadataSelector item : imageMetadataSelectors) {
                        String temp = item.acceptCategory(directoryName);
                        if(temp != null) {
                            selector = item;
                            directoryName = temp;
                        }
                    }
                    boolean asJson = selector != null && selector.asJsonProperty();
                    String json = "{";
                    for(Tag tag : directory.getTags()) {
                        String name = tag.getTagName();
                        String tagName = selector != null ? selector.acceptTag(name) : name;
                        if(tagName != null) {
                            logger.debug("Add Tag, Category: '{}', Tag Name: '{}', Value: '{}'", new Object[]{directoryName, tagName, tag.getDescription()});
                            if(asJson) {
                                json += "\"" + tagName + "\":\"" + tag.getDescription() + "\",";
                            } else {
                                perAsset.addTag(directoryName, tagName, tag.getDescription());
                            }
                        }
                    }
                    if(asJson) {
                        if(json.length() > 1) {
                            json = json.substring(0, json.length() - 1);
                            json += "}";
                            perAsset.addTag(directoryName, "raw_tags", json);
                        }
                    }
                }
            } catch(ImageProcessingException e) {
                e.printStackTrace();
            }
        } catch(RepositoryException e) {
            throw new ManagementException("Failed to Create Asset Node in Parent: " + parent.getPath() + ", name: " + assetName, e);
        } catch(IOException e) {
            throw new ManagementException("Failed to Create Rendition in Parent: " + parent.getPath() + ", name: " + assetName, e);
        }
        return answer;
    }

    public ImageContext createRendition(Resource resource, String renditionName, String sourceMimeType) throws ManagementException {
        if(resource == null) {
            throw new ManagementException("No Asset Resource provided");
        }
        PerAsset asset = resource.adaptTo(PerAsset.class);
        if(asset == null) {
            throw new ManagementException("Resource: " + resource.getPath() + " could not be adapted to an Asset");
        }
        if(renditionName == null || renditionName.isEmpty()) {
            throw new ManagementException("No Rendition Name provided");
        }
        if(sourceMimeType == null || sourceMimeType.isEmpty()) {
            throw new ManagementException("No Source Mime Type provided");
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

    // todo: needs deep clone
    private Node createNode(Node parent, Map data) throws RepositoryException {
        data.remove("path");
        String component = (String) data.remove("component");

        Node newNode = parent.addNode("n"+ UUID.randomUUID(), "nt:unstructured");
        newNode.setProperty("sling:resourceType", component);
        for (Object key: data.keySet()) {
            Object val = data.get(key);
            if(val instanceof String) {
                newNode.setProperty(key.toString(), (String) val);
            }
        }
        return newNode;
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
    public Resource updateResource(ResourceResolver resourceResolver, String path, String jsonContent) throws ManagementException {
        Resource answer = null;
        try {
            answer = getResource(resourceResolver, path);
            if(answer == null) {
                throw new ManagementException("Resource not found, Path: " + path);
            }
            if(jsonContent == null || jsonContent.isEmpty()) {
                throw new ManagementException("No Content provided, Path: " + path);
            }
            Map content = convertToMap(jsonContent);
            //AS TODO: Check if we could add some guards here to avoid misplaced updates (JCR Primary Type / Sling Resource Type)
            updateResourceTree(answer, content);
            resourceResolver.commit();
        } catch(IOException e) {
            throw new ManagementException("Failed to parse Json Content: " + jsonContent);
        }
        return answer;
    }

    private void updateResourceTree(Resource resource, Map<String, Object> properties) throws ManagementException {
        ModifiableValueMap updateProperties = getModifiableProperties(resource, false);
        for(Entry<String, Object> entry: properties.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof Map) {
                Resource child = resource.getChild(name);
                if(child == null) {
                    throw new ManagementException("Property: '" + name + "' is a map but not child resource found with that name");
                }
                updateResourceTree(child, (Map) value);
            } else {
//            } else if(value instanceof Object[]) {
                updateProperties.put(name, value);
            }
        }
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
        }
    }

    public static Map convertToMap(String json) throws IOException {
        Map answer = new LinkedHashMap();
        if(json != null) {
            ObjectMapper mapper = new ObjectMapper();
            answer = mapper.readValue(json, LinkedHashMap.class);
        }
        return answer;
    }

    private Node createPageOrTemplate(Resource parent, String name, String templateComponent, String templatePath) throws RepositoryException {
        Node parentNode = parent.adaptTo(Node.class);
        Node newPage = null;
        newPage = parentNode.addNode(name, PAGE_PRIMARY_TYPE);
        Node content = newPage.addNode(JCR_CONTENT);
        content.setPrimaryType(PAGE_CONTENT_TYPE);
        content.setProperty(SLING_RESOURCE_TYPE, templateComponent);
        content.setProperty(JCR_TITLE, name);
        if(templatePath != null) {
            content.setProperty(TEMPLATE, templatePath);
        }
        newPage.getSession().save();
        return newPage;
    }
}
