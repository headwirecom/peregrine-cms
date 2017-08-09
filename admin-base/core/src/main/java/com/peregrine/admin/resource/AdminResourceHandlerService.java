package com.peregrine.admin.resource;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.adaption.PerAsset;
import com.peregrine.admin.replication.ImageMetadataSelector;
import com.peregrine.rendition.BaseResourceHandler;
import com.peregrine.commons.util.PerUtil;
import com.sun.media.sound.ModelIdentifier;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static com.peregrine.commons.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getResource;

/**
 * Created by schaefa on 7/6/17.
 */
@Component(
    service = AdminResourceHandler.class,
    immediate = true
)
public class AdminResourceHandlerService
    implements AdminResourceHandler
{
    public static final String DELETION_PROPERTY_NAME = "_opDelete";

    @Reference
    ResourceRelocation resourceRelocation;
    @Reference
    BaseResourceHandler baseResourceHandler;

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
            baseResourceHandler.updateModification(resourceResolver, newFolder);
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
            baseResourceHandler.updateModification(resourceResolver, newObject);
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
    public Resource createTemplate(ResourceResolver resourceResolver, String parentPath, String name, String component) throws ManagementException {
        try {
            Resource parent = PerUtil.getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException("Could not find Template Parent Resource. Path: " + parentPath + ", name: " + name);
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException("Template Name is not provided. Path: " + parentPath);
            }
            Node newPage = createPageOrTemplate(parent, name, component, null);
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
                baseResourceHandler.updateModification(resource.getResourceResolver(), newNode);
                if(firstChild != null) {
                    resourceRelocation.reorder(resource, newNode.getName(), firstChild.getName(), true);
                }
                answer = resource.getResourceResolver().getResource(newNode.getPath());
                baseResourceHandler.updateModification(answer);
            } else {
                Node parent = node.getParent();
                newNode = createNode(parent, properties);
                baseResourceHandler.updateModification(resource.getResourceResolver(), newNode);
                resourceRelocation.reorder(resource.getParent(), newNode.getName(), node.getName(), orderBefore);
                answer = resource.getResourceResolver().getResource(newNode.getPath());
                baseResourceHandler.updateModification(answer);
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
                baseResourceHandler.updateModification(answer);
            } else {
                // Both BEFORE and AFTER can be handled in one as the only difference is if added before or after
                // and if they are the same parent we means we only ORDER otherwise we MOVE first
                boolean sameParent = resourceRelocation.hasSameParent(fromResource, toResource);
                if(!sameParent) {
                    answer = resourceRelocation.moveToNewParent(fromResource, toResource.getParent(), false);
                }
                resourceRelocation.reorder(toResource.getParent(), fromResource.getName(), toResource.getName(), orderBefore);
                baseResourceHandler.updateModification(answer);
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
            baseResourceHandler.updateModification(answer);
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
            baseResourceHandler.updateModification(parent.getResourceResolver(), newAsset);

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

    public Resource createNode(Resource parent, String name, String primaryType, String resourceType) throws ManagementException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JCR_PRIMARY_TYPE, primaryType);
        if(resourceType != null && !resourceType.isEmpty()) {
            properties.put(SLING_RESOURCE_TYPE, resourceType);
        }
        try {
            return parent.getResourceResolver().create(parent, name, properties);
        } catch(PersistenceException e) {
            throw new ManagementException("Failed to create resource: " + name + " on parent: " + parent.getPath(), e);
        }
    }

    // todo: needs deep clone
    private Node createNode(Node parent, Map data) throws RepositoryException {
        data.remove("path");
        String component = (String) data.remove("component");

        Node newNode = parent.addNode("n"+ UUID.randomUUID(), NT_UNSTRUCTURED);
        newNode.setProperty("sling:resourceType", component);
        for (Object key: data.keySet()) {
            Object val = data.get(key);
            if(val instanceof String) {
                newNode.setProperty(key.toString(), (String) val);
            }
        }
        return newNode;
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
        } catch(IOException e) {
            throw new ManagementException("Failed to parse Json Content: " + jsonContent);
        }
        return answer;
    }

    private void updateResourceTree(Resource resource, Map<String, Object> properties) throws ManagementException {
        // Handle Deletion:
        // 1) Delete property with either 'true' or null as value -> remove the given resource
        // 2) Delete Property's value converted to string and then looked up as child of the given resource
        //    - If found delete that resource
        //    - If properties have an entry with that name and it is a Map -> remove it to avoid re-adding it during the processing of the properties
        if(properties.containsKey(DELETION_PROPERTY_NAME)) {
            Object value = properties.get(DELETION_PROPERTY_NAME);
            if(value == null || "true".equalsIgnoreCase(value.toString())) {
                // This indicates that this node shall be removed
                try {
                    resource.getResourceResolver().delete(resource);
                    // This resource is gone so there is noting left that can be done here
                    return;
                } catch(PersistenceException e) {
                    throw new ManagementException("Failed to delete resource: " + resource.getPath(), e);
                }
            } else {
                String name = value.toString();
                Resource child = resource.getChild(name);
                if(child != null) {
                    try {
                        resource.getResourceResolver().delete(child);
                        if(properties.containsKey(name)) {
                            value = properties.get(name);
                            if(value instanceof Map) {
                                properties.remove(name);
                            }
                        }
                    } catch(PersistenceException e) {
                        throw new ManagementException("Failed to delete child resource: " + child.getPath(), e);
                    }
                }
            }
        }
        ModifiableValueMap updateProperties = getModifiableProperties(resource, false);
        for(Entry<String, Object> entry: properties.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof Map) {
                Map childProperties = (Map) value;
                Resource child = resource.getChild(name);
                // If child is missing then create it
                if(child == null) {
                    Object val = childProperties.get(SLING_RESOURCE_TYPE);
                    if(val instanceof String) {
                        String resourceType = (String) val;
                        childProperties.remove("name");
                        childProperties.remove(SLING_RESOURCE_TYPE);
                        childProperties.remove(JCR_PRIMARY_TYPE);
                        child = createNode(resource, name, NT_UNSTRUCTURED, resourceType);
                        // Now update the child with any remaining properties
                        ModifiableValueMap newChildProperties = getModifiableProperties(child, false);
                        for(Object childPropertyKey : childProperties.keySet()) {
                            newChildProperties.put(childPropertyKey + "", childProperties.get(childPropertyKey));
                        }
                    } else {
                        throw new ManagementException("Property: '" + name + "' is a map, is not found and cannot be created due to missing Sling Resource Type");
                    }
                } else {
                    updateResourceTree(child, (Map) value);
                }
            } else {
                updateProperties.put(name, value);
            }
        }
        baseResourceHandler.updateModification(resource);
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
        baseResourceHandler.updateModification(parent.getResourceResolver(), newPage);
        return newPage;
    }
}
