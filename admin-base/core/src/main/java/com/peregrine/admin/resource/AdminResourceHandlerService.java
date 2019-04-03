package com.peregrine.admin.resource;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.peregrine.adaption.PerAsset;
import com.peregrine.rendition.BaseResourceHandler;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.replication.ImageMetadataSelector;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.convertToMap;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.isEmpty;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.isPrimaryType;

/**
 * Created by Andreas Schaefer on 7/6/17.
 */
@Component(
    service = AdminResourceHandler.class,
    immediate = true
)
public class AdminResourceHandlerService
    implements AdminResourceHandler
{
    public static final String DELETION_PROPERTY_NAME = "_opDelete";

    private static final String PARENT_NOT_FOUND = "Could not find %s Parent Resource. Path: '%s', name: '%s'";
    private static final String RESOURCE_TYPE_UNEDEFINED = "Resource Type is not provided. Path: '%s', name: '%s'";
    private static final String NAME_UNDEFINED = "%s Name is not provided. Parent Path: '%s'";
    private static final String TEMPLATE_NOT_FOUND = "Could not find template with path: '%s'";

    private static final String FAILED_TO_HANDLE = "Failed to handle %s node. Parent Path: '%s', name: '%s'";
    private static final String RESOURCE_FOR_DELETION_NOT_FOUND = "Could not find Resource for Deletion. Path: '%s'";
    private static final String PRIMARY_TYPE_ASKEW_FOR_DELETION = "Failed to Delete Resource: '%s'. Expected Primary Type: '%s', Actual Primary Type: '%s'";
    private static final String FAILED_TO_DELETE = "Failed to Delete Resource: '%s'";

    private static final String INSERT_RESOURCE_MISSING = "To Insert a New Node the Reference Resource must be provided";
    private static final String INSERT_RESOURCE_PROPERTIES_MISSING = "To Insert a New Node the Node Properties must be provided";
    private static final String FAILED_TO_INSERT = "Failed to insert node at: '%s'";

    private static final String MOVE_FROM_RESOURCE_MISSING = "To Move a Node the Source Resource must be provided";
    private static final String MOVE_TO_RESOURCE_MISSING = "To Move a Node the Reference Resource must be provided";
    private static final String FAILED_TO_MOVE = "Failed to Move Resource. From: '%s' to: '%s'";

    private static final String RENAME_RESOURCE_MISSING = "To Rename a Node a Resource must be provided";
    private static final String NAME_TO_BE_RENAMED_TO_MUST_BE_PROVIDED = "Name to be renamed to must be provided";
    private static final String NAME_TO_BE_RENAMED_TO_CANNOT_CONTAIN_A_SLASH = "Name to be renamed to cannot contain a slash";
    private static final String FAILED_TO_RENAME = "Failed to Rename Resource. From: '%s' to: '%s'";

    private static final String PARENT_RESOURCE_MUST_BE_PROVIDED_TO_CREATE_ASSET = "Parent Resource must be provided to create Asset";
    private static final String ASSET_NAME_MUST_BE_PROVIDED_TO_CREATE_ASSET = "Asset Name must be provided to create Asset";
    private static final String CONTENT_TYPE_MUST_BE_PROVIDED_TO_CREATE_ASSET = "Content Type must be provided to create Asset";
    private static final String INPUT_STREAM_MUST_BE_PROVIDED_TO_CREATE_ASSET = "Input Stream must be provided to create Asset";
    private static final String FAILED_TO_CREATE = "Failed to Create %s in Parent: '%s', name: '%s'";
    private static final String FAILED_TO_COPY = "Failed to copy source: '%s' on target parent: '%s'";
    private static final String RESOURCE_NOT_FOUND = "Resource not found, Path: '%s'";
    private static final String NO_CONTENT_PROVIDED = "No Content provided, Path: '%s'";
    private static final String FAILED_TO_PARSE_JSON = "Failed to parse Json Content: '%s'";

    private static final String FAILED_TO_DELETE_CHILD = "Failed to delete child resource: '%s'";
    private static final String OJECT_FIRST_ITEM_WITH_UNSUPPORTED_TYPE = "Object List had an unsupported first entry: '%s' (type: '%s')";
    private static final String OJECT_ITEM_WITH_UNSUPPORTED_TYPE = "Object List was a single list but had an unsupported entry: '%s' (type: '%s')";
    private static final String ITEM_NAME_MISSING = "Item: '%s' does not have a name (parent: '%s'";
    private static final String OBJECT_LIST_WITH_UNSUPPORTED_ITEM = "Object List was a full list but had an unsupported entry: '%s' (type: '%s')";

    private static final String RAW_TAGS = "raw_tags";

    private static final String FOLDER = "folder";
    private static final String OBJECT = "object";
    private static final String PAGE = "page";
    private static final String TEMPLATE = "template";
    private static final String NODE = "node";
    private static final String ASSET = "asset";
    private static final String RENDITION = "rendition";

    private static final List<String> IGNORED_PROPERTIES_FOR_COPY = new ArrayList<>();
    private static final List<String> IGNORED_RESOURCE_PROPERTIES_FOR_COPY = new ArrayList<>();

    public static final String MISSING_RESOURCE_RESOLVER_FOR_SITE_COPY = "Resource Resolver must be provide to copy a Site";
    public static final String MISSING_PARENT_RESOURCE_FOR_COPY_SITES = "Sites Parent Resource was not provided or does not exist";
    public static final String MISSING_SOURCE_SITE_NAME = "Source Name must be provide";
    public static final String SOURCE_NAME_AND_TARGET_NAME_CANNOT_BE_THE_SAME_VALUE = "Source Name and Target Name cannot be the same value: ";
    public static final String MISSING_NEW_SITE_NAME = "Name of the Target Site must be provided to copy a Site";
    public static final String SOURCE_SITE_DOES_NOT_EXIST = "Source Site: '%s' was not provided or does not exist";
    public static final String TARGET_SITE_EXISTS = "Target Site: '%s' does exist and so copy failed";
    public static final String SOURCE_SITE_IS_NOT_A_PAGE = "Source Site: '%s' is not a Page";

    static {
        IGNORED_PROPERTIES_FOR_COPY.add(JCR_PRIMARY_TYPE);
        IGNORED_PROPERTIES_FOR_COPY.add(JCR_UUID);

        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_UUID);
        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_CREATED);
        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_CREATED_BY);
    }


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
                throw new ManagementException(String.format(PARENT_NOT_FOUND, FOLDER, parentPath, name));
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException(String.format(NAME_UNDEFINED, FOLDER, parentPath));
            }
            Node parentNode =  parent.adaptTo(Node.class);
            Node newFolder = parentNode.addNode(name, SLING_ORDERED_FOLDER);
            newFolder.setProperty(JCR_TITLE, name);
            baseResourceHandler.updateModification(resourceResolver, newFolder);
            return resourceResolver.getResource(newFolder.getPath());
        } catch(RepositoryException e) {
            logger.debug("Failed to create Folder. Parent Path: '{}', Name: '{}'", parentPath, name);
            logger.error("Failed to create Folder", e);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, FOLDER, parentPath, name), e);
        }
    }

    public Resource createObject(ResourceResolver resourceResolver, String parentPath, String name, String resourceType) throws ManagementException {
        try {
            Resource parent = PerUtil.getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, OBJECT, parentPath, name));
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException(String.format(NAME_UNDEFINED, OBJECT, parentPath));
            }
            if(resourceType == null || resourceType.isEmpty()) {
                throw new ManagementException(String.format(RESOURCE_TYPE_UNEDEFINED, parentPath, name));
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
            throw new ManagementException(String.format(FAILED_TO_HANDLE, OBJECT, parentPath, name), e);
        }
    }

    @Override
    public Resource createPage(ResourceResolver resourceResolver, String parentPath, String name, String templatePath) throws ManagementException {
        try {
            Resource parent = PerUtil.getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, PAGE, parentPath, name));
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException(String.format(NAME_UNDEFINED, PAGE, parentPath));
            }
            Resource templateResource = PerUtil.getResource(resourceResolver, templatePath + "/" + JCR_CONTENT);
            if(templateResource == null) {
                throw new ManagementException(String.format(TEMPLATE_NOT_FOUND, templatePath));
            }
            String templateComponent = templateResource.getValueMap().get(SLING_RESOURCE_TYPE, String.class);
            Node newPage = createPageOrTemplate(parent, name, templateComponent, templatePath);
            return resourceResolver.getResource(newPage.getPath());
        } catch(RepositoryException e) {
            logger.debug("Failed to create Page. Parent Path: '{}', Name: '{}', Template Path: '{}'", parentPath, name, templatePath);
            logger.error("Failed to create Page", e);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, PAGE, parentPath, name), e);
        }
    }

    @Override
    public Resource createTemplate(ResourceResolver resourceResolver, String parentPath, String name, String component) throws ManagementException {
        try {
            Resource parent = PerUtil.getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, TEMPLATE, parentPath, name));
            }
            if(name == null || name.isEmpty()) {
                throw new ManagementException(String.format(NAME_UNDEFINED, TEMPLATE, parentPath));
            }
            Node newPage = createPageOrTemplate(parent, name, component, null);
            // If there is a component then we check the component node and see if there is a child jcr:content node
            // If found we copy this over into our newly created node
            if(isNotEmpty(component)) {
                logger.trace("Component: '{}' provided for template. Copy its properties over if there is a JCR Content Node");
                try {
                    if(component.startsWith("/")) {
                        logger.warn("Component (for template): '{}' started with a slash which is not valid -> ignored", component);
                    } else {
                        String componentPath = APPS_ROOT + SLASH + component;
                        if(newPage.getSession().itemExists(componentPath)) {
                            Node componentNode = newPage.getSession().getNode("/apps/" + component);
                            if(componentNode.hasNode(JCR_CONTENT)) {
                                Node source = componentNode.getNode(JCR_CONTENT);
                                Node target = newPage.getNode(JCR_CONTENT);
                                copyNode(source, target, true);
                            }
                        }
                    }
                } catch(PathNotFoundException e) {
                    logger.warn("Component (for template)t: '{}' not found -> ignored", component);
                }
            }
            return resourceResolver.getResource(newPage.getPath());
        } catch(RepositoryException e) {
            logger.debug("Failed to create Template. Parent Path: '{}', Name: '{}'", parentPath, name);
            logger.error("Failed to create Template", e);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, TEMPLATE, parentPath, name), e);
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
            throw new ManagementException(String.format(RESOURCE_FOR_DELETION_NOT_FOUND, path));
        }
        try {
            String primaryTypeValue = resource.getValueMap().get(JCR_PRIMARY_TYPE, String.class);
            if(primaryType != null && !primaryType.isEmpty() && !primaryType.equals(primaryTypeValue)) {
                throw new ManagementException(String.format(PRIMARY_TYPE_ASKEW_FOR_DELETION, path, primaryType, primaryTypeValue));
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
            throw new ManagementException(String.format(FAILED_TO_DELETE, path), e);
        }
    }

    @Override
    public Resource insertNode(Resource resource, Map<String, Object> properties, boolean addAsChild, boolean orderBefore, String variation) throws ManagementException {
        Resource answer = null;
        if(resource == null) {
            throw new ManagementException(INSERT_RESOURCE_MISSING);
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
                newNode = createNode(node, properties, variation);
                baseResourceHandler.updateModification(resource.getResourceResolver(), newNode);
                if(firstChild != null) {
                    resourceRelocation.reorder(resource, newNode.getName(), firstChild.getName(), true);
                }
                answer = resource.getResourceResolver().getResource(newNode.getPath());
                baseResourceHandler.updateModification(answer);
            } else {
                Node parent = node.getParent();
                newNode = createNode(parent, properties, variation);
                baseResourceHandler.updateModification(resource.getResourceResolver(), newNode);
                resourceRelocation.reorder(resource.getParent(), newNode.getName(), node.getName(), orderBefore);
                answer = resource.getResourceResolver().getResource(newNode.getPath());
                baseResourceHandler.updateModification(answer);
            }
        } catch (RepositoryException e) {
            logger.trace("Failed to insert node at: " + resource.getPath(), e);
            throw new ManagementException(String.format(FAILED_TO_INSERT, resource.getPath()), e);
        }
        return answer;
    }

    @Override
    public Resource moveNode(Resource fromResource, Resource toResource, boolean addAsChild, boolean orderBefore) throws ManagementException {
        Resource answer = null;
        if(fromResource == null) {
            throw new ManagementException(MOVE_FROM_RESOURCE_MISSING);
        }
        if(toResource == null) {
            throw new ManagementException(MOVE_TO_RESOURCE_MISSING);
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
            throw new ManagementException(String.format(FAILED_TO_MOVE, fromResource.getPath(), toResource.getPath()), e);
        }
        return answer;
    }


    @Override
    public Resource rename(Resource fromResource, String newName) throws ManagementException {
        Resource answer = null;
        if(fromResource == null) {
            throw new ManagementException(RENAME_RESOURCE_MISSING);
        }
        if(newName == null || newName.isEmpty()) {
            throw new ManagementException(NAME_TO_BE_RENAMED_TO_MUST_BE_PROVIDED);
        }
        if(newName.indexOf('/') >= 0) {
            throw new ManagementException(NAME_TO_BE_RENAMED_TO_CANNOT_CONTAIN_A_SLASH);
        }
        try {
            answer = resourceRelocation.rename(fromResource, newName, true);
            baseResourceHandler.updateModification(answer);
        } catch (Exception e) {
            logger.error("problems while moving", e);
            throw new ManagementException(String.format(FAILED_TO_RENAME, fromResource.getPath(), newName), e);
        }
        return answer;
    }

    @Override
    public Resource createAssetFromStream(Resource parent, String assetName, String contentType, InputStream inputStream) throws ManagementException {
        Resource answer = null;
        if(parent == null) {
            throw new ManagementException(PARENT_RESOURCE_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        if(assetName == null || assetName.isEmpty()) {
            throw new ManagementException(ASSET_NAME_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        if(contentType == null || contentType.isEmpty()) {
            throw new ManagementException(CONTENT_TYPE_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        if(inputStream == null) {
            throw new ManagementException(INPUT_STREAM_MUST_BE_PROVIDED_TO_CREATE_ASSET);
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
                    logger.trace("Image Metadata Directory: '{}'", directoryName);
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
                        logger.trace("Image Metadata Tag Name: '{}'", name);
                        String tagName = selector != null ? selector.acceptTag(name) : name;
                        if(tagName != null) {
                            logger.trace("Add Tag, Category: '{}', Tag Name: '{}', Value: '{}'", new Object[]{directoryName, tagName, tag.getDescription()});
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
                            perAsset.addTag(directoryName, RAW_TAGS, json);
                        }
                    }
                }
            } catch(ImageProcessingException e) {
                e.printStackTrace();
            }
        } catch(RepositoryException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, ASSET, parent.getPath(), assetName), e);
        } catch(IOException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, RENDITION, parent.getPath(), assetName), e);
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
//            logger.trace("Failed to create Node, parent: '{}', name: '{}', properties: '{}'", parent, name, properties);
//            logger.trace("Failure Exception", e);
            throw new ManagementException(String.format(FAILED_TO_CREATE, NODE, parent.getPath(), name), e);
        } catch(RuntimeException e) {
            logger.trace("Failed to create Node, parent: '{}', name: '{}', properties: '{}'", parent, name, properties);
            logger.trace("Failure Exception", e);
            throw new ManagementException(String.format(FAILED_TO_CREATE, NODE, parent.getPath(), name), e);
        }
    }

    // todo: needs deep clone
    private Node createNode(Node parent, Map data, String variation) throws RepositoryException, ManagementException {
        data.remove(PATH);
        String component = (String) data.remove(COMPONENT);

        Node newNode = parent.addNode("n"+ UUID.randomUUID(), NT_UNSTRUCTURED);
        newNode.setProperty(SLING_RESOURCE_TYPE, component);
        for (Object key: data.keySet()) {
            Object val = data.get(key);
            if(val instanceof String) {
                newNode.setProperty(key.toString(), (String) val);
            }
        }

        // If there is a component then we check the component node and see if there is a child jcr:content node
        // If found we copy this over into our newly created node
        if(isNotEmpty(component)) {
            try {
                if(component.startsWith("/")) {
                    logger.warn("Component: '{}' started with a slash which is not valid -> ignored", component);
                } else {
                    String componentPath = APPS_ROOT + SLASH + component;
                    Node contentNode = null;
                    if(parent.getSession().itemExists(componentPath)) {
                        Node componentNode = parent.getSession().getNode(componentPath);
                        if(componentNode != null) {
                            if(componentNode.hasNode(JCR_CONTENT)) {
                                contentNode = componentNode.getNode(JCR_CONTENT);
                            } else {
                                // Loop for a sling:resourceSuperType and copy this one in instead
                                Node superTypeNode = componentNode;
                                List<String> alreadyVisitedNodes = new ArrayList<>();
                                while(true) {
                                    // If we already visited that node then exit to avoid an endless loop
                                    if(alreadyVisitedNodes.contains(superTypeNode.getPath())) { break; }
                                    alreadyVisitedNodes.add(superTypeNode.getPath());
                                    if(superTypeNode.hasProperty(SLING_RESOURCE_SUPER_TYPE)) {
                                        String resourceSuperType = superTypeNode.getProperty(SLING_RESOURCE_SUPER_TYPE).getString();
                                        if(isNotEmpty(resourceSuperType)) {
                                            try {
                                                superTypeNode = superTypeNode.getSession().getNode(APPS_ROOT + SLASH + resourceSuperType);
                                                logger.trace("Found Resource Super Type: '{}'", superTypeNode.getPath());
                                                // If we find the JCR Content then we are done here otherwise try to find this one's super resource type
                                                if(superTypeNode.hasNode(JCR_CONTENT)) {
                                                    contentNode = superTypeNode.getNode(JCR_CONTENT);
                                                    logger.trace("Found Content Node of Super Resource Type: '{}': '{}'", superTypeNode.getPath(), contentNode.getPath());
                                                    break;
                                                }
                                            } catch(PathNotFoundException e) {
                                                logger.warn("Could not find Resource Super Type Component: " + APPS_ROOT + SLASH + resourceSuperType + " -> ignore component", e);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if(contentNode != null) {
                                boolean isVariations = false;
                                if(contentNode.hasProperty(VARIATIONS)) {
                                    isVariations = contentNode.getProperty(VARIATIONS).getBoolean();
                                }
                                if(isVariations) {
                                    boolean useDefault = true;
                                    if(isNotEmpty(variation)) {
                                        // Look up the variation node
                                        if(contentNode.hasNode(variation)) {
                                            Node variationNode = contentNode.getNode(variation);
                                            if(variationNode.hasNode(JCR_CONTENT)) {
                                                contentNode = variationNode.getNode(JCR_CONTENT);
                                                useDefault = false;
                                            } else {
                                                logger.trace("Found variation node: '{}' but it did not contain a jcr:content child -> ignore", variationNode.getPath());
                                                contentNode = null;
                                            }
                                        } else {
                                            logger.trace("Variation: '{}' is given but no such child node found under: '{}' -> use first one", variation, contentNode.getPath());
                                        }
                                    }
                                    if(useDefault) {
                                        NodeIterator i = contentNode.getNodes();
                                        if(i.hasNext()) {
                                            Node variationNode = i.nextNode();
                                            if(variationNode.hasNode(JCR_CONTENT)) {
                                                contentNode = variationNode.getNode(JCR_CONTENT);
                                            } else {
                                                logger.trace("Found default variation node: '{}' but it did not contain a jcr:content child -> ignore", variationNode.getPath());
                                                contentNode = null;
                                            }
                                        }
                                    }
                                }
                                if(contentNode != null) {
                                    copyNode(contentNode, newNode, true);
                                }
                            }
                        }
                    }
                }
            } catch(PathNotFoundException e) {
                logger.warn("Component: '{}' not found -> ignored", component);
            }
        }
        return newNode;
    }

    public Node copyNode(Node source, Node target, boolean deep) throws ManagementException {
        try {
            // Copy all properties
            PropertyIterator pi = source.getProperties();
            while(pi.hasNext()) {
                Property property = pi.nextProperty();
                if(!IGNORED_PROPERTIES_FOR_COPY.contains(property.getName())) {
                    if(property.isMultiple()) {
                        target.setProperty(property.getName(), property.getValues(), property.getType());
                    } else {
                        target.setProperty(property.getName(), property.getValue(), property.getType());
                    }
                }
            }
            if(deep) {
                NodeIterator ni = source.getNodes();
                while(ni.hasNext()) {
                    Node sourceChild = ni.nextNode();
                    // Create Target first
                    Node targetChild = target.addNode(sourceChild.getName(), sourceChild.getPrimaryNodeType().getName());
                    copyNode(sourceChild, targetChild, true);
                }
            }
            return target;
        } catch(RepositoryException e) {
            logger.trace("Failed to copy components node", e);
            throw new ManagementException(String.format(FAILED_TO_COPY, source, target), e);
        }
    }

    @Override
    public Resource copySite(ResourceResolver resourceResolver, String sitesParentPath, String fromName, String targetName) throws ManagementException {
        // Check the given parameters and make sure everything is correct
        if(resourceResolver == null) { throw new ManagementException(MISSING_RESOURCE_RESOLVER_FOR_SITE_COPY); }
        Resource parentResource = getResource(resourceResolver, sitesParentPath);
        if(parentResource == null) { throw new ManagementException(MISSING_PARENT_RESOURCE_FOR_COPY_SITES); }
        if(isEmpty(fromName)) { throw new ManagementException(MISSING_SOURCE_SITE_NAME); }
        if(fromName.equals(targetName)) { throw new ManagementException(SOURCE_NAME_AND_TARGET_NAME_CANNOT_BE_THE_SAME_VALUE + fromName); }
        Resource source = getResource(parentResource, fromName);
        if(source == null) { throw new ManagementException(String.format(SOURCE_SITE_DOES_NOT_EXIST, fromName)); }
        if(isEmpty(targetName)) { throw new ManagementException(MISSING_NEW_SITE_NAME); }
        Resource answer = getResource(parentResource, targetName);
        if(answer != null) { throw new ManagementException(String.format(TARGET_SITE_EXISTS, targetName)); }
        // Ensure the Site Resource is a page
        if(!isPrimaryType(source, PAGE_PRIMARY_TYPE)) { throw new ManagementException(String.format(SOURCE_SITE_IS_NOT_A_PAGE, fromName)); }

        ArrayList<String> superTypes = new ArrayList<>();

        Resource appsSource = getResource(resourceResolver, APPS_ROOT + SLASH + fromName);
        if(appsSource != null) {
            Resource appsTarget = getResource(resourceResolver, APPS_ROOT + SLASH + targetName);
            if(appsTarget == null) {
                appsTarget = copyFolder(appsSource, appsSource.getParent(), targetName);
            }
            // for each component in /apps/<fromSite>/components create a stub component in /apps/<toSite>/components
            // with the sling:resourceSuperType set to the <fromSite> component
            copyStubs(appsSource, appsTarget, COMPONENTS, superTypes);
            // for each object in /apps/<fromSite>/objects create a stub component in /apps/<toSite>/objects
            // with the sling:resourceSuperType set to the <fromSite> object
            copyStubs(appsSource, appsTarget, OBJECTS, null);
        }

        // copy /content/assets/<fromSite> to /content/assets/<toSite>
        Resource sourceResource = getResource(resourceResolver, ASSETS_ROOT + SLASH + fromName);
        if(sourceResource != null) {
            Resource targetResource = copyResources(sourceResource, sourceResource.getParent(), targetName);
            if(targetResource != null) {
                copyChildResources(sourceResource, true, targetResource, null, targetName);
            }
        }
        // copy /content/objects/<fromSite> to /content/objects/<toSite> and fix all references
        sourceResource = getResource(resourceResolver, OBJECTS_ROOT + SLASH + fromName);
        if(sourceResource != null) {
            Resource targetResource = copyResources(sourceResource, sourceResource.getParent(), targetName);
            if(targetResource != null) {
                copyChildResources(sourceResource, true, targetResource, fromName, targetName);
            }
        }
        // copy /content/templates/<fromSite> to /content/templates/<toSite> and fix all references
        sourceResource = getResource(resourceResolver, TEMPLATES_ROOT + SLASH + fromName);
        if(sourceResource != null) {
            Resource targetResource = copyResources(sourceResource, sourceResource.getParent(), targetName);
            if(targetResource != null) {
                copyChildResources(sourceResource, true, targetResource, fromName, targetName);
            }
        }
        // copy /content/sites/<fromSite> to /content/sites/<toSite> and fix all references
        sourceResource = getResource(resourceResolver, SITES_ROOT + SLASH + fromName);
        if(sourceResource != null) {
            Resource targetResource = copyResources(sourceResource, sourceResource.getParent(), targetName);
            if(targetResource != null) {
                copyChildResources(sourceResource, true, targetResource, fromName, targetName);
            }
            answer = targetResource;
        }
        // create an /etc/felibs/<toSite> felib, extend felib to include a dependency on the /etc/felibs/<fromSite>
        sourceResource = getResource(resourceResolver, FELIBS_ROOT + SLASH + fromName);
        if(sourceResource != null) {
            Resource targetResource = copyResources(sourceResource, sourceResource.getParent(), targetName);
            logger.trace("Copied Felibs for Target: '{}': '{}'", targetName, targetResource);
            ValueMap properties = getModifiableProperties(targetResource, false);
            logger.trace("Copied Felibs Properties: '{}'", properties);
            if(properties != null) {
                String[] dependencies = properties.get(DEPENDENCIES, String[].class);
                if(dependencies == null) {
                    dependencies = new String[]{sourceResource.getPath()};
                } else {
                    String[] newDependencies = new String[dependencies.length + 1];
                    System.arraycopy(dependencies, 0, newDependencies, 0, dependencies.length);
                    newDependencies[dependencies.length] = sourceResource.getPath();
                    dependencies = newDependencies;
                }
                properties.put(DEPENDENCIES, dependencies);
            }

            StringBuilder mappings = new StringBuilder();
            for (String superType : superTypes) {
                String componentSourceName = PerUtil.getComponentVariableNameFromString(superType);
                String componentDestName = PerUtil.getComponentVariableNameFromString(superType.replace(fromName+"/", targetName+"/"));
                mappings.append("var ");
                mappings.append(componentDestName);
                mappings.append(" = ");
                mappings.append(componentSourceName);
                mappings.append('\n');
            }
            createResourceFromString(resourceResolver, targetResource, "mapping.js", mappings.toString());
            createResourceFromString(resourceResolver, targetResource, "js.txt", "mapping.js\n");
        }

        return answer;
    }

    private void createResourceFromString(ResourceResolver resourceResolver, Resource parent, String name, String data) throws ManagementException {
        try {
            Node parentNode = parent.adaptTo(Node.class);
            Node newAsset = parentNode.addNode(name, NT_FILE);
            Node content = newAsset.addNode(JCR_CONTENT, NT_RESOURCE);
            content.setProperty(JCR_DATA, data);
            content.setProperty(JCR_MIME_TYPE, TEXT_MIME_TYPE);
        } catch(RepositoryException e) {
            logger.error("failed to create resource {}", name, e);
            throw new ManagementException(String.format(FAILED_TO_CREATE, name, parent.getPath(), name), e);
        }
    }

    @Override
    public void deleteSite(ResourceResolver resourceResolver, String sitesParentPath, String name) throws ManagementException {
        // Check the given parameters and make sure everything is correct
        if(resourceResolver == null) { throw new ManagementException(MISSING_RESOURCE_RESOLVER_FOR_SITE_COPY); }
        Resource parentResource = getResource(resourceResolver, sitesParentPath);
        if(parentResource == null) { throw new ManagementException(MISSING_PARENT_RESOURCE_FOR_COPY_SITES); }
        if(isEmpty(name)) { throw new ManagementException(MISSING_SOURCE_SITE_NAME); }

        Resource source = getResource(parentResource, name);
        if(source == null) { throw new ManagementException(String.format(SOURCE_SITE_DOES_NOT_EXIST, name)); }

        if(!isPrimaryType(source, PAGE_PRIMARY_TYPE)) { throw new ManagementException(String.format(SOURCE_SITE_IS_NOT_A_PAGE, name)); }

        Resource appsSource = getResource(resourceResolver, APPS_ROOT + SLASH + name);
        deleteResource(resourceResolver, appsSource);

        Resource sourceResource = getResource(resourceResolver, ASSETS_ROOT + SLASH + name);
        deleteResource(resourceResolver, sourceResource);

        sourceResource = getResource(resourceResolver, OBJECTS_ROOT + SLASH + name);
        deleteResource(resourceResolver, sourceResource);

        sourceResource = getResource(resourceResolver, TEMPLATES_ROOT + SLASH + name);
        deleteResource(resourceResolver, sourceResource);

        sourceResource = getResource(resourceResolver, SITES_ROOT + SLASH + name);
        deleteResource(resourceResolver, sourceResource);

        sourceResource = getResource(resourceResolver, FELIBS_ROOT + SLASH + name);
        deleteResource(resourceResolver, sourceResource);

    }

    private void deleteResource(ResourceResolver resourceResolver, Resource resource) throws ManagementException {
        if(resource != null) {
            try {
                resourceResolver.delete(resource);
            } catch (PersistenceException e) {
                throw new ManagementException(String.format("not able to delete {}", resource.getPath()));
            }
        }
    }

    private Resource copyResources(Resource source, Resource targetParent, String toName) {
        Resource target = getResource(targetParent, toName);
        if(target != null) {
            logger.warn("Target Resource: '{}' already exist -> copy is ignored", target.getPath());
            return null;
        }
        Map<String, Object> newProperties = copyProperties(source.getValueMap());
        logger.trace("Resource Properties: '{}'", newProperties);
        try {
            target = source.getResourceResolver().create(targetParent, toName, newProperties);
            updateTitle(target, toName);
        } catch(PersistenceException e) {
            logger.warn("Copy of " + source.getName() + ": '" + source.getPath() + "' failed", e);
            return null;
        }
        logger.trace("New Resource Properties: '{}'", target.getValueMap());
        return target;
    }

    private Map<String, Object> copyProperties(ValueMap source) {
        Map<String, Object> answer = new HashMap<>(source);
        for(String ignore: IGNORED_RESOURCE_PROPERTIES_FOR_COPY) {
            if(answer.containsKey(ignore)) {
                answer.remove(ignore);
            }
        }
        return answer;
    }

    private void updateTitle(Resource resource, String title) {
        if(JCR_CONTENT.equals(resource.getName())) {
            ValueMap properties = getModifiableProperties(resource, false);
            if(properties.containsKey(JCR_TITLE)) {
                properties.put(JCR_TITLE, title);
            }
        }
    }

    private void copyChildResources(Resource source, boolean deep, Resource target, String fromName, String toName) {
        // For deep copies, we need to know the depth of our copy, since the top-level assets will use the toName
        // while child assets will use the name from the source; otherwise every asset has the same name
        copyChildResources(source, deep, target, fromName, toName, 0);
    }

    private void copyChildResources(Resource source, boolean deep, Resource target, String fromName, String toName, int depth) {
        logger.trace("Copy Child Resource from: '{}', to: '{}'", source.getPath(), target.getPath());
        for(Resource child: source.getChildren()) {
            logger.trace("Child handling started: '{}'", child.getPath());
            Map<String, Object> newProperties = copyProperties(child.getValueMap());
            try {
                if(isNotEmpty(fromName)) {
                    String pattern1 = SLASH + fromName;
                    String pattern2 = fromName + SLASH;
                    for(Entry<String, Object> entry : newProperties.entrySet()) {
                        String key = entry.getKey();
                        Object temp = entry.getValue();
                        if(temp instanceof String) {
                            String value = (String) temp;
                            int index = value.indexOf(pattern1);
                            String newValue = null;
                            if(value.startsWith("/content/") && index > 0) {
                                // Check if the string ends or if the next character is a slash to avoid collisions
                                logger.trace("Value Length: {}, Index: {}, Pattern Length: {}", value.length(), index, pattern1.length());
                                if(index + pattern1.length() == value.length()) {
                                    newValue = value.substring(0, index) + SLASH + toName;
                                } else if(value.charAt(index + pattern1.length()) == '/') {
                                    newValue = value.substring(0, index) + SLASH + toName + SLASH + value.substring(index + pattern1.length() + 1);
                                }
                            } else if(value.startsWith(pattern2)) {
                                newValue = toName + SLASH + value.substring(pattern2.length());
                            }
                            if(newValue != null) {
                                entry.setValue(newValue);
                                logger.trace("Updated Properties: '{}'", newProperties);
                            }
                        }
                    }
                }
                Resource childTarget = source.getResourceResolver().create(target, child.getName(), newProperties);
                updateTitle(childTarget, (((depth > 0) && (newProperties.get(JCR_TITLE) != null)) ?  (String) newProperties.get(JCR_TITLE) : toName));

                logger.trace("Child Target Created: '{}'", childTarget == null ? "null" : childTarget.getPath());
                // Copy grandchildren
                if(deep) {
                    copyChildResources(child, true, childTarget, fromName, toName, depth + 1);
                }
            } catch(PersistenceException e) {
                logger.warn("Copy of " + source.getName() + ": '" + source.getPath() + "' failed", e);
                return;
            }
            logger.trace("Child handled: '{}'", child.getPath());
        }
    }

    private void copyStubs(Resource source, Resource target, String folderName, List superTypes) throws ManagementException {
        Resource appsSource = getResource(source, folderName);
        if(appsSource != null) {
            Resource appsTarget = getResource(source.getResourceResolver(), target.getPath() + SLASH + folderName);
            if(appsTarget == null) {
                appsTarget = copyFolder(appsSource, target, folderName);
                if(appsTarget == null) { return; }
            }
            for(Resource child : appsSource.getChildren()) {
                ValueMap properties = child.getValueMap();
                Map<String, Object> newProperties = new HashMap<>(properties);
                String originalAppsPath = child.getPath();
                originalAppsPath = originalAppsPath.substring(APPS_ROOT.length() + 1);
                if(superTypes != null) {
                    superTypes.add(originalAppsPath);
                }
                newProperties.put(SLING_RESOURCE_SUPER_TYPE, originalAppsPath);
                try {
                    source.getResourceResolver().create(appsTarget, child.getName(), newProperties);
                } catch(PersistenceException e) {
                    logger.warn("Copy of " + folderName + ": '" + child.getPath() + "' failed", e);
                }
            }
        }
    }

    private Resource copyFolder(Resource folder, Resource targetParent, String folderName) {
        Resource answer = null;
        Map<String, Object> newProperties = copyProperties(folder.getValueMap());
        logger.trace("Resource Properties: '{}'", newProperties);
        try {
            answer = folder.getResourceResolver().create(targetParent, folderName, newProperties);
        } catch(PersistenceException e) {
            logger.warn("Copy of " + folder.getName() + ": '" + folder.getPath() + "' failed", e);
        }
        return answer;
    }

    @Override
    public Resource updateResource(ResourceResolver resourceResolver, String path, String jsonContent) throws ManagementException {
        Resource answer = null;
        try {
            answer = getResource(resourceResolver, path);
            if(answer == null) {
                throw new ManagementException(String.format(RESOURCE_NOT_FOUND, path));
            }
            if(jsonContent == null || jsonContent.isEmpty()) {
                throw new ManagementException(String.format(NO_CONTENT_PROVIDED, path));
            }
            Map content = convertToMap(jsonContent);
            //AS TODO: Check if we could add some guards here to avoid misplaced updates (JCR Primary Type / Sling Resource Type)
            updateResourceTree(answer, content);
        } catch(IOException e) {
            throw new ManagementException(String.format(FAILED_TO_PARSE_JSON, jsonContent));
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
            if(value == null || Boolean.TRUE.toString().equalsIgnoreCase(value.toString())) {
                // This indicates that this node shall be removed
                try {
                    resource.getResourceResolver().delete(resource);
                    // This resource is gone so there is noting left that can be done here
                    return;
                } catch(PersistenceException e) {
                    throw new ManagementException(String.format(FAILED_TO_DELETE, resource.getPath()), e);
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
                        throw new ManagementException(String.format(FAILED_TO_DELETE_CHILD, child.getPath()), e);
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
                String childPath = (String) childProperties.get(PATH); 
                Resource child = resource.getResourceResolver().getResource(childPath);
                if(child == null) child = resource.getChild(name);


                // If child is missing then create it
                if(child == null) {
                    Object val = childProperties.get(SLING_RESOURCE_TYPE);
                    String resourceType = val == null ? null : (String) val;
                    childProperties.remove(NAME);
                    childProperties.remove(SLING_RESOURCE_TYPE);
                    childProperties.remove(JCR_PRIMARY_TYPE);
                    child = createNode(resource, name, NT_UNSTRUCTURED, resourceType);
                    // Now update the child with any remaining properties
                    ModifiableValueMap newChildProperties = getModifiableProperties(child, false);
                    for(Object childPropertyKey : childProperties.keySet()) {
                        newChildProperties.put(childPropertyKey + "", childProperties.get(childPropertyKey));
                    }
                } else {
                    updateResourceTree(child, childProperties);
                }
            } else if(value instanceof List) {
                List list = (List) value;
                int type = 0;
                Object first = null;
                if(!list.isEmpty()) {
                    first = list.get(0);
                    type = first instanceof Map ? 2 :
                        first instanceof String ? 1 :
                            -1;
                }
                if(type == 2) {
                    Resource child = resource.getChild(name);
                    if(child == null) {
                        child = createNode(resource, name, NT_UNSTRUCTURED, null);
                    }
                    // We support either a List of Objects (Maps) or list of Strings which are stored as multi-valued String property
                    // for which we have to get all the values in a list and then afterwards if such values were found update
                    // them as a property
                    updateObjectList(list, child);
                } else if(type == 1) {
                    updateObjectSingleList(name, list, resource);
                } else if(type < 0) {
                    throw new ManagementException(String.format(OJECT_FIRST_ITEM_WITH_UNSUPPORTED_TYPE, first, (first == null ? "null" : first.getClass().getName())));
                }
            } else {
                updateProperties.put(name, value);
            }
        }
        baseResourceHandler.updateModification(resource);
    }

    private void updateObjectSingleList(String name, List incomingList, Resource resource) throws ManagementException {
        List<String> newSingleList = new ArrayList<>();
        for(Object item : incomingList) {
            if(item instanceof String) {
                String listItem = item.toString();
                if(listItem.isEmpty()) {
                    continue;
                }
                newSingleList.add(listItem);
            } else {
                throw new ManagementException(String.format(OJECT_ITEM_WITH_UNSUPPORTED_TYPE, item, (item == null ? "null" : item.getClass().getName())));
            }
        }
        ModifiableValueMap childProperties = getModifiableProperties(resource, false);
        childProperties.put(name, newSingleList.toArray(new String[newSingleList.size()]));
    }

    private void updateObjectList(List incomingList, Resource resource) throws ManagementException {
        Resource lastResourceItem = null;
        for(int i = 0; i < incomingList.size(); i++) {
            Object item = incomingList.get(i);
            if(item instanceof Map) {
                Map incomingItemProperties = (Map) item;
                Object temp = incomingItemProperties.get(NAME);
                String incomingItemName = null;
                if(temp != null) {
                    incomingItemName = temp.toString();
                }
                if(incomingItemName == null || incomingItemName.isEmpty()) {
                    throw new ManagementException(String.format(ITEM_NAME_MISSING, item, resource.getPath()));
                }
                // Get index of the matching resource child to compare with the index in the list
                int index = -1;
                Resource resourceListItem = null;
                for(Resource tempResource: resource.getChildren()) {
                    index++;
                    if(incomingItemName.equals(tempResource.getName())) {
                        resourceListItem = tempResource;
                        break;
                    }
                }
                // Handle new item
                if(resourceListItem == null) {
                    Object val = incomingItemProperties.get(SLING_RESOURCE_TYPE);
                    String resourceType = val == null ? null : (String) val;
                    incomingItemProperties.remove(NAME);
                    incomingItemProperties.remove(SLING_RESOURCE_TYPE);
                    incomingItemProperties.remove(JCR_PRIMARY_TYPE);
                    resourceListItem = createNode(resource, incomingItemName, NT_UNSTRUCTURED, resourceType);
                    // Move the child to the correct position
                    if(lastResourceItem == null) {
                        // No saved last resource item so we need to place it as the first entry
                        Resource first = null;
                        for(Resource tempResource: resource.getChildren()) {
                            first = tempResource;
                            break;
                        }
                        // If there are no items then ignore it (it will be first
                        if(first != null) {
                            moveNode(resourceListItem, first, false, true);
                        }
                    } else {
                        // There is a resource item -> move the new one after the last one
                        moveNode(resourceListItem, lastResourceItem, false, false);
                    }
                    // Now update the child with any remaining properties
                    ModifiableValueMap newChildProperties = getModifiableProperties(resourceListItem, false);
                    for(Object childPropertyKey : incomingItemProperties.keySet()) {
                        newChildProperties.put(childPropertyKey + "", incomingItemProperties.get(childPropertyKey));
                    }
                } else {
                    if(incomingItemProperties.containsKey(DELETION_PROPERTY_NAME)) {
                        Object value = incomingItemProperties.get(DELETION_PROPERTY_NAME);
                        if(value == null || Boolean.TRUE.toString().equalsIgnoreCase(value.toString())) {
                            try {
                                logger.trace("Remove List Child: '{}' ('{}')", incomingItemName, resourceListItem.getPath());
                                resource.getResourceResolver().delete(resourceListItem);
                                continue;
                            } catch(PersistenceException e) {
                                throw new ManagementException(String.format(FAILED_TO_DELETE, resourceListItem.getPath()), e);
                            }
                        }
                    }
                    updateResourceTree(resourceListItem, incomingItemProperties);
                    // Check order
                    if(i != index) {
                        if(lastResourceItem == null) {
                            // No saved last resource item so we need to place it as the first entry
                            Resource first = null;
                            for(Resource tempResource : resource.getChildren()) {
                                first = tempResource;
                                break;
                            }
                            // If there are no items then ignore it (it will be first
                            if(first != null) {
                                moveNode(resourceListItem, first, false, true);
                            }
                        } else {
                            // We only have to move if this wasn't already the first item due to deletion
                            boolean doMove = false;
                            Iterator<Resource> ir = resource.getChildren().iterator();
                            if(ir.hasNext()) {
                                if(!lastResourceItem.getName().equals(ir.next().getName())) {
                                    doMove = true;
                                    break;
                                }
                            }
                            if(doMove) {
                                moveNode(resourceListItem, lastResourceItem, false, false);
                            }
                        }
                    }
                }
                lastResourceItem = resourceListItem;
            } else {
                throw new ManagementException(String.format(OBJECT_LIST_WITH_UNSUPPORTED_ITEM, item, (item == null ? "null" : item.getClass().getName())));
            }
        }
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
