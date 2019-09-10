package com.peregrine.admin.resource;

import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.ASSETS_ROOT;
import static com.peregrine.commons.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.COMPONENTS;
import static com.peregrine.commons.util.PerConstants.DEPENDENCIES;
import static com.peregrine.commons.util.PerConstants.FELIBS_ROOT;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_CREATED;
import static com.peregrine.commons.util.PerConstants.JCR_CREATED_BY;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.JCR_UUID;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.NT_RESOURCE;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.OBJECTS_ROOT;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PACKAGES_PATH;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.SITES_ROOT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_SUPER_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerConstants.TEMPLATES_ROOT;
import static com.peregrine.commons.util.PerConstants.TEXT_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.VARIATIONS;
import static com.peregrine.commons.util.PerUtil.*;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.peregrine.adaption.PerAsset;
import com.peregrine.rendition.BaseResourceHandler;
import com.peregrine.replication.ImageMetadataSelector;

@Component(
    service = AdminResourceHandler.class,
    immediate = true
)
public final class AdminResourceHandlerService
        implements AdminResourceHandler {

    public static final String DELETION_PROPERTY_NAME = "_opDelete";
    public static final String MODE_PROPERTY = "mode";

    private static final String PARENT_NOT_FOUND = "Could not find %s Parent Resource. Path: '%s', name: '%s'";
    private static final String RESOURCE_TYPE_UNDEFINED = "Resource Type is not provided. Path: '%s', name: '%s'";
    private static final String NAME_UNDEFINED = "%s Name is not provided. Parent Path: '%s'";
    private static final String TEMPLATE_NOT_FOUND = "Could not find template with path: '%s'";

    private static final String FAILED_TO_HANDLE = "Failed to handle %s node. Parent Path: '%s', name: '%s'";
    private static final String RESOURCE_FOR_DELETION_NOT_FOUND = "Could not find Resource for Deletion. Path: '%s'";
    private static final String PRIMARY_TYPE_ASKEW_FOR_DELETION = "Failed to Delete Resource: '%s'. Expected Primary Type: '%s', Actual Primary Type: '%s'";
    private static final String FAILED_TO_DELETE = "Failed to Delete Resource: '%s'";

    private static final String INSERT_RESOURCE_MISSING = "To Insert a New Node the Reference Resource must be provided";
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
    private static final String OBJECT_FIRST_ITEM_WITH_UNSUPPORTED_TYPE = "Object List had an unsupported first entry: '%s' (type: '%s')";
    private static final String OBJECT_ITEM_WITH_UNSUPPORTED_TYPE = "Object List was a single list but had an unsupported entry: '%s' (type: '%s')";
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
    public static final String COPY_FAILED = "Copy of %s: '%s' failed";
    private static final String IMAGE_METADATA_TAG_NAME = "Image Metadata Tag Name: '{}'";
    private static final String ADD_TAG_CATEGORY_TAG_NAME_VALUE = "Add Tag, Category: '{}', Tag Name: '{}', Value: '{}'";

    //Package creation constants
    private static final String PACKAGE_SUFFIX = "-full-package";
    private static final double DEFAULT_PACKAGE_VERSION = 1.0;
    private static final double MAXIMUM_VERSION = 10.0;
    private static final String ZIP_EXTENSION = ".zip";
    private static final String DASH = "-";
    private static final String ZIP_MIME_TYPE = "application/zip";
    private static final String VLT_PACKAGE = "vlt:Package";
    private static final String GROUP_PROPERTY = "group";
    private static final String NAME_PROPERTY = "name";
    private static final String VERSION_PROPERTY = "version";
    private static final String VLT_PACKAGE_DEFINITION = "vlt:PackageDefinition";
    private static final String VLT_DEFINITION = "vlt:definition";
    private static final String FILTER = "filter";
    private static final String REPLACE_VALUE = "replace";
    private static final String ROOT_PROPERTY = "root";
    private static final String RULES_PROPERTY = "rules";

    static {
        IGNORED_PROPERTIES_FOR_COPY.add(JCR_PRIMARY_TYPE);
        IGNORED_PROPERTIES_FOR_COPY.add(JCR_UUID);

        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_UUID);
        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_CREATED);
        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_CREATED_BY);
    }

    @Reference
    private ResourceRelocation resourceRelocation;

    @Reference
    private BaseResourceHandler baseResourceHandler;

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
            if(isEmpty(name)) {
                throw new ManagementException(String.format(NAME_UNDEFINED, FOLDER, parentPath));
            }

            final Node parent = Optional.ofNullable(parentPath)
                    .map(p -> getResource(resourceResolver, p))
                    .map(r -> r.adaptTo(Node.class))
                    .orElse(null);
            if(parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, FOLDER, parentPath, name));
            }

            Node newFolder = parent.addNode(name, SLING_ORDERED_FOLDER);
            newFolder.setProperty(JCR_TITLE, name);
            baseResourceHandler.updateModification(resourceResolver, newFolder);
            return adaptNodeToResource(resourceResolver, newFolder);
        } catch(RepositoryException e) {
            logger.debug("Failed to create Folder. Parent Path: '{}', Name: '{}'", parentPath, name);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, FOLDER, parentPath, name), e);
        }
    }

    private Resource adaptNodeToResource(final ResourceResolver resourceResolver, final Node node) throws RepositoryException {
        if (node == null) {
            return null;
        }

        return resourceResolver.getResource(node.getPath());
    }

    public Resource createObject(ResourceResolver resourceResolver, String parentPath, String name, String resourceType) throws ManagementException {
        try {
            if(isEmpty(name)) {
                throw new ManagementException(String.format(NAME_UNDEFINED, OBJECT, parentPath));
            }

            if(isEmpty(resourceType)) {
                throw new ManagementException(String.format(RESOURCE_TYPE_UNDEFINED, parentPath, name));
            }

            final Node parent = Optional.ofNullable(parentPath)
                    .map(p -> getResource(resourceResolver, p))
                    .map(r -> r.adaptTo(Node.class))
                    .orElse(null);

            if(parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, OBJECT, parentPath, name));
            }

            Node newObject = parent.addNode(name, OBJECT_PRIMARY_TYPE);
            newObject.setProperty(SLING_RESOURCE_TYPE, resourceType);
            newObject.setProperty(JCR_TITLE, name);
            baseResourceHandler.updateModification(resourceResolver, newObject);
            return adaptNodeToResource(resourceResolver, newObject);
        } catch(RepositoryException e) {
            logger.debug("Failed to create Object. Parent Path: '{}', Name: '{}'", parentPath, name);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, OBJECT, parentPath, name), e);
        }
    }

    @Override
    public Resource createPage(ResourceResolver resourceResolver, String parentPath, String name, String templatePath) throws ManagementException {
        try {
            Resource parent = getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, PAGE, parentPath, name));
            }
            if(isEmpty(name)) {
                throw new ManagementException(String.format(NAME_UNDEFINED, PAGE, parentPath));
            }
            Resource templateResource = getResource(resourceResolver, templatePath + "/" + JCR_CONTENT);
            if(templateResource == null) {
                throw new ManagementException(String.format(TEMPLATE_NOT_FOUND, templatePath));
            }
            String templateComponent = templateResource.getValueMap().get(SLING_RESOURCE_TYPE, String.class);
            Node newPage = createPageOrTemplate(parent, name, templateComponent, templatePath);
            return adaptNodeToResource(resourceResolver, newPage);
        } catch(RepositoryException e) {
            logger.debug("Failed to create Page. Parent Path: '{}', Name: '{}', Template Path: '{}'", parentPath, name, templatePath);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, PAGE, parentPath, name), e);
        }
    }

    @Override
    public Resource createTemplate(ResourceResolver resourceResolver, String parentPath, String name, String component) throws ManagementException {
        try {
            Resource parent = getResource(resourceResolver, parentPath);
            if(parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, TEMPLATE, parentPath, name));
            }
            if(isEmpty(name)) {
                throw new ManagementException(String.format(NAME_UNDEFINED, TEMPLATE, parentPath));
            }
            Node newPage = createPageOrTemplate(parent, name, component, null);
            // If there is a component then we check the component node and see if there is a child jcr:content node
            // If found we copy this over into our newly created node
            if(isNotEmpty(component)) {
                logger.trace("Component: '{}' provided for template. Copy its properties over if there is a JCR Content Node", component);
                copyAppsComponentToNewTemplate(component, newPage);
            }
            return adaptNodeToResource(resourceResolver, newPage);
        } catch(RepositoryException e) {
            logger.debug("Failed to create Template. Parent Path: '{}', Name: '{}'", parentPath, name);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, TEMPLATE, parentPath, name), e);
        }
    }

    private void copyAppsComponentToNewTemplate(final String component, final Node template) throws RepositoryException, ManagementException {
        try {
            if(component.startsWith(SLASH)) {
                logger.warn("Component (for template): '{}' started with a slash which is not valid -> ignored", component);
            } else if (template != null) {
                String componentPath = APPS_ROOT + SLASH + component;
                final Session session = template.getSession();
                if(session.itemExists(componentPath)) {
                    Node componentNode = session.getNode(componentPath);
                    if(componentNode.hasNode(JCR_CONTENT)) {
                        Node source = componentNode.getNode(JCR_CONTENT);
                        Node target = template.getNode(JCR_CONTENT);
                        copyNode(source, target, true);
                    }
                }
            }
        } catch(PathNotFoundException e) {
            logger.warn("Component (for template)t: '{}' not found -> ignored", component);
        }
    }

    @Override
    public DeletionResponse deleteResource(ResourceResolver resourceResolver, String path) throws ManagementException {
        return deleteResource(resourceResolver, path, null);
    }

    @Override
    public DeletionResponse deleteResource(ResourceResolver resourceResolver, String path, String primaryType) throws ManagementException {
        final Resource resource = getResource(resourceResolver, path);
        if (resource == null) {
            throw new ManagementException(String.format(RESOURCE_FOR_DELETION_NOT_FOUND, path));
        }

        try {
            final String primaryTypeValue = resource.getValueMap().get(JCR_PRIMARY_TYPE, EMPTY);
            if (isNotEmpty(primaryType) && !primaryTypeValue.equals(primaryType)) {
                throw new ManagementException(String.format(PRIMARY_TYPE_ASKEW_FOR_DELETION, path, primaryType, primaryTypeValue));
            }

            final Resource parent = resource.getParent();
            final DeletionResponse response = new DeletionResponse()
                .setName(resource.getName())
                .setPath(resource.getPath())
                .setParentPath(parent != null ? parent.getPath() : "")
                .setType(StringUtils.defaultIfEmpty(primaryTypeValue, "not-found"));
            resourceResolver.delete(resource);
            return response;
        } catch (PersistenceException e) {
            throw new ManagementException(String.format(FAILED_TO_DELETE, path), e);
        }
    }

    @Override
    public Resource insertNode(Resource resource, Map<String, Object> properties, boolean addAsChild, boolean orderBefore, String variation) throws ManagementException {
        final Node node = Optional.ofNullable(resource)
                .map(r -> r.adaptTo(Node.class))
                .orElse(null);
        if(node == null) {
            throw new ManagementException(INSERT_RESOURCE_MISSING);
        }

        try {
            final Node newNode;
            final ResourceResolver resourceResolver = resource.getResourceResolver();
            if(addAsChild) {
                newNode = createNode(node, properties, variation);
                baseResourceHandler.updateModification(resourceResolver, newNode);
                if(orderBefore) {
                    final Iterator<Resource> i = resource.listChildren();
                    if(i.hasNext()) {
                        resourceRelocation.reorder(resource, newNode.getName(), i.next().getName(), true);
                    }
                }
            } else {
                Node parent = node.getParent();
                newNode = createNode(parent, properties, variation);
                baseResourceHandler.updateModification(resourceResolver, newNode);
                resourceRelocation.reorder(resource.getParent(), newNode.getName(), node.getName(), orderBefore);
            }

            final Resource answer = adaptNodeToResource(resourceResolver, newNode);
            baseResourceHandler.updateModification(answer);
            return answer;
        } catch (RepositoryException e) {
            throw new ManagementException(String.format(FAILED_TO_INSERT, resource.getPath()), e);
        }
    }

    @Override
    public Resource moveNode(Resource fromResource, Resource toResource, boolean addAsChild, boolean orderBefore) throws ManagementException {
        if(fromResource == null) {
            throw new ManagementException(MOVE_FROM_RESOURCE_MISSING);
        }
        if(toResource == null) {
            throw new ManagementException(MOVE_TO_RESOURCE_MISSING);
        }
        try {
            Resource answer = fromResource;
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

            return answer;
        } catch (Exception e) {
            throw new ManagementException(String.format(FAILED_TO_MOVE, fromResource.getPath(), toResource.getPath()), e);
        }
    }


    @Override
    public Resource rename(Resource fromResource, String newName) throws ManagementException {
        if(fromResource == null) {
            throw new ManagementException(RENAME_RESOURCE_MISSING);
        }
        if(isEmpty(newName)) {
            throw new ManagementException(NAME_TO_BE_RENAMED_TO_MUST_BE_PROVIDED);
        }
        if(newName.indexOf('/') >= 0) {
            throw new ManagementException(NAME_TO_BE_RENAMED_TO_CANNOT_CONTAIN_A_SLASH);
        }
        try {
            final Resource answer = resourceRelocation.rename(fromResource, newName, true);
            baseResourceHandler.updateModification(answer);
            return answer;
        } catch (Exception e) {
            throw new ManagementException(String.format(FAILED_TO_RENAME, fromResource.getPath(), newName), e);
        }
    }

    @Override
    public Resource createAssetFromStream(Resource parent, String assetName, String contentType, InputStream inputStream) throws ManagementException {
        if(isEmpty(assetName)) {
            throw new ManagementException(ASSET_NAME_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        if(isEmpty(contentType)) {
            throw new ManagementException(CONTENT_TYPE_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        if(inputStream == null) {
            throw new ManagementException(INPUT_STREAM_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }

        final Node parentNode = Optional.ofNullable(parent)
                .map(r -> r.adaptTo(Node.class))
                .orElse(null);
        if(parentNode == null) {
            throw new ManagementException(PARENT_RESOURCE_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        try {
            Node newAsset = parentNode.addNode(assetName, ASSET_PRIMARY_TYPE);
            Node content = newAsset.addNode(JCR_CONTENT, ASSET_CONTENT_TYPE);
            Binary data = parentNode.getSession().getValueFactory().createBinary(inputStream);
            content.setProperty(JCR_DATA, data);
            content.setProperty(JCR_MIME_TYPE, contentType);
            final ResourceResolver resourceResolver = parent.getResourceResolver();
            baseResourceHandler.updateModification(resourceResolver, newAsset);
            final Resource answer = adaptNodeToResource(resourceResolver, newAsset);
            if (answer != null) {
                processNewAsset(answer.adaptTo(PerAsset.class));
            }

            return answer;
        } catch(RepositoryException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, ASSET, parent.getPath(), assetName), e);
        } catch(IOException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, RENDITION, parent.getPath(), assetName), e);
        }
    }

    private void processNewAsset(final PerAsset asset) throws IOException, RepositoryException {
        if (asset == null) {
            return;
        }

        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(asset.getRenditionStream((Resource) null));
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
                if (selector != null && selector.asJsonProperty()) {
                    addTagsToNewAssetAsJson(asset, directory, directoryName, selector);
                } else {
                    addTagsToNewAsset(asset, directory, directoryName, selector);
                }
            }
            // Obtain the Asset Dimension and store directly in the meta data folder
            handleAssetDimensions(asset);
        } catch(ImageProcessingException e) {
            logger.debug(EMPTY, e);
        }
    }

    private void addTagsToNewAssetAsJson(PerAsset asset, Directory directory, String directoryName, ImageMetadataSelector selector) throws PersistenceException, RepositoryException {
        final StringBuilder json = new StringBuilder("{");
        for(final Tag tag : directory.getTags()) {
            final String name = tag.getTagName();
            logger.trace(IMAGE_METADATA_TAG_NAME, name);
            final String tagName = selector != null ? selector.acceptTag(name) : name;
            if(tagName != null) {
                logger.trace(ADD_TAG_CATEGORY_TAG_NAME_VALUE, directoryName, tagName, tag.getDescription());
                json.append("\"");
                json.append(tagName);
                json.append("\":\"");
                json.append(tag.getDescription());
                json.append("\",");
            }
        }

        final int length = json.length();
        if(length > 1) {
            json.deleteCharAt(length - 1);
            json.append("}");
            asset.addTag(directoryName, RAW_TAGS, json.toString());
        }
    }

    private void addTagsToNewAsset(PerAsset asset, Directory directory, String directoryName, ImageMetadataSelector selector) throws PersistenceException, RepositoryException {
        for(final Tag tag : directory.getTags()) {
            final String name = tag.getTagName();
            logger.trace(IMAGE_METADATA_TAG_NAME, name);
            final String tagName = selector != null ? selector.acceptTag(name) : name;
            if(tagName != null) {
                logger.trace(ADD_TAG_CATEGORY_TAG_NAME_VALUE, directoryName, tagName, tag.getDescription());
                asset.addTag(directoryName, tagName, tag.getDescription());
            }
        }
    }

    public Resource createNode(Resource parent, String name, String primaryType, String resourceType) throws ManagementException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JCR_PRIMARY_TYPE, primaryType);
        if(isNotEmpty(resourceType)) {
            properties.put(SLING_RESOURCE_TYPE, resourceType);
        }
        try {
            return parent.getResourceResolver().create(parent, name, properties);
        } catch(PersistenceException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, NODE, parent.getPath(), name), e);
        } catch(RuntimeException e) {
            logger.debug("Failed to create Node, parent: '{}', name: '{}', properties: '{}'", parent, name, properties);
            throw new ManagementException(String.format(FAILED_TO_CREATE, NODE, parent.getPath(), name), e);
        }
    }

    // TODO: needs deep clone
    private Node createNode(Node parent, Map<String, Object> data, String variation) throws RepositoryException, ManagementException {
        data.remove(PATH);
        final String component = (String) data.remove(COMPONENT);

        Node newNode = parent.addNode("n" + UUID.randomUUID(), NT_UNSTRUCTURED);
        newNode.setProperty(SLING_RESOURCE_TYPE, component);
        final Set<Map.Entry<String, Object>> entrySet = data.entrySet();
        for (final Map.Entry<String, Object> entry: entrySet) {
            Object key = entry.getKey();
            Object val = entry.getValue();
            if(val instanceof String) {
                newNode.setProperty(key.toString(), (String) val);
            }
        }

        // If there is a component then we check the component node and see if there is a child jcr:content node
        // If found we copy this over into our newly created node
        if(isNotEmpty(component)) {
            try {
                if(component.startsWith(SLASH)) {
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
                                    if(useDefault && contentNode != null) {
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
            throw new ManagementException(String.format(FAILED_TO_COPY, source, target), e);
        }
    }

    @Override
    public Resource copySite(
            final ResourceResolver resourceResolver,
            final String sitesParentPath,
            final String fromName,
            final String targetName) throws ManagementException {
        checkCopySiteParameters(resourceResolver, sitesParentPath, fromName, targetName);

        final Resource parentResource = getResource(resourceResolver, sitesParentPath);
        if(parentResource == null) { throw new ManagementException(MISSING_PARENT_RESOURCE_FOR_COPY_SITES); }
        final Resource source = getResource(parentResource, fromName);
        if(source == null) { throw new ManagementException(String.format(SOURCE_SITE_DOES_NOT_EXIST, fromName)); }
        // Ensure the Site Resource is a page
        if(!isPrimaryType(source, PAGE_PRIMARY_TYPE)) { throw new ManagementException(String.format(SOURCE_SITE_IS_NOT_A_PAGE, fromName)); }

        Resource answer = getResource(parentResource, targetName);
        if(answer != null) { throw new ManagementException(String.format(TARGET_SITE_EXISTS, targetName)); }

        final ArrayList<Resource> resourcesToPackage = new ArrayList<>();
        final ArrayList<String> superTypes = new ArrayList<>();

        final StructureCopier copier = new StructureCopier(resourceResolver);
        copier.setToName(targetName);
        copier.setFromName(fromName);
        resourcesToPackage.add(copier.copyApps(superTypes));
        // copy /content/assets/<fromSite> to /content/assets/<toSite>
        copier.setFromName(null);
        resourcesToPackage.add(copier.copyFromRoot(ASSETS_ROOT));
        copier.setFromName(fromName);
        // copy /content/objects/<fromSite> to /content/objects/<toSite> and fix all references
        resourcesToPackage.add(copier.copyFromRoot(OBJECTS_ROOT));
        // copy /content/templates/<fromSite> to /content/templates/<toSite> and fix all references
        resourcesToPackage.add(copier.copyFromRoot(TEMPLATES_ROOT));
        // copy /content/sites/<fromSite> to /content/sites/<toSite> and fix all references
        answer = copier.copyFromRoot(SITES_ROOT);
        resourcesToPackage.add(answer);
        if (answer != null) {
            updateStringsInFiles(answer, targetName);
        }

        // create an /etc/felibs/<toSite> felib, extend felib to include a dependency on the /etc/felibs/<fromSite>
        final Resource sourceResource = getResource(resourceResolver, FELIBS_ROOT + SLASH + fromName);
        if(sourceResource != null) {
            final Resource targetResource = copyResources(sourceResource, sourceResource.getParent(), targetName);
            resourcesToPackage.add(targetResource);
            logger.trace("Copied Felibs for Target: '{}': '{}'", targetName, targetResource);
            final ValueMap properties = getModifiableProperties(targetResource, false);
            logger.trace("Copied Felibs Properties: '{}'", properties);
            if (properties != null) {
                String[] dependencies = properties.get(DEPENDENCIES, String[].class);
                if(dependencies == null) {
                    dependencies = new String[]{ sourceResource.getPath() };
                } else {
                    final String[] newDependencies = new String[dependencies.length + 1];
                    System.arraycopy(dependencies, 0, newDependencies, 0, dependencies.length);
                    newDependencies[dependencies.length] = sourceResource.getPath();
                    dependencies = newDependencies;
                }

                properties.put(DEPENDENCIES, dependencies);
            }

            createResourceFromString(targetResource, "mapping.js", assembleMappingsJs(superTypes, fromName, targetName));
            createResourceFromString(targetResource, "js.txt", "mapping.js\n");
        }

        try {
            final List<String> packagePaths = resourcesToPackage.stream()
                    .filter(Objects::nonNull)
                    .map(Resource::getPath)
                    .collect(Collectors.toList());
            createSitePackage(resourceResolver, targetName, packagePaths);
        } catch (PersistenceException e) {
            logger.error("Failed to create package for site " + targetName, e);
        }

        return answer;
    }

    private void checkCopySiteParameters(
            final ResourceResolver resourceResolver,
            final String sitesParentPath,
            final String fromName,
            final String targetName) throws ManagementException {
        // Check the given parameters and make sure everything is correct
        if (resourceResolver == null) {
            throw new ManagementException(MISSING_RESOURCE_RESOLVER_FOR_SITE_COPY);
        }

        if (isBlank(sitesParentPath)) {
            throw new ManagementException(MISSING_PARENT_RESOURCE_FOR_COPY_SITES);
        }

        if (isEmpty(fromName)) {
            throw new ManagementException(MISSING_SOURCE_SITE_NAME);
        }

        if (fromName.equals(targetName)) {
            throw new ManagementException(SOURCE_NAME_AND_TARGET_NAME_CANNOT_BE_THE_SAME_VALUE + fromName);
        }

        if (isEmpty(targetName)) {
            throw new ManagementException(MISSING_NEW_SITE_NAME);
        }
    }

    private String assembleMappingsJs(final List<String> superTypes, final String fromName, final String targetName) {
        final StringBuilder builder = new StringBuilder();
        for (final String superType : superTypes) {
            String sourceName = getComponentVariableNameFromString(superType);
            String destName = getComponentVariableNameFromString(superType.replace(fromName + SLASH, targetName + SLASH));
            builder.append("var ");
            builder.append(destName);
            builder.append(" = ");
            builder.append(sourceName);
            builder.append('\n');
        }

        return builder.toString();
    }

    private void createSitePackage(ResourceResolver resourceResolver, String siteName, List<String> packagePaths) throws PersistenceException {
        Resource packagesRoot = resourceResolver.getResource(PACKAGES_PATH);
        if(packagesRoot == null) {
            logger.error("Package root path '{}' could not be resolved.", PACKAGES_PATH);
            return;
        }

        Map<String, Object> propertiesMap;

        Resource groupResource = packagesRoot.getChild(siteName);
        if(groupResource == null) {
            propertiesMap = new HashMap<>();
            propertiesMap.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            groupResource = resourceResolver.create(packagesRoot, siteName, propertiesMap);
        }

        String packageName = siteName + PACKAGE_SUFFIX;
        double version = DEFAULT_PACKAGE_VERSION;
        String filename = packageName + DASH + version + ZIP_EXTENSION;

        Resource packageResource = groupResource.getChild(filename);
        //Since it's possible that backups of a previous site with the same name exist, we'll increment
        //the version number until we find a version we don't have (or hit a maximum version number)
        while(packageResource != null) {
            version += 1;
            filename = packageName + DASH + version + ZIP_EXTENSION;
            packageResource = groupResource.getChild(filename);
            if(version >= MAXIMUM_VERSION) {
                final String path = Optional.ofNullable(packageResource)
                        .map(Resource::getPath)
                        .orElse(null);
                logger.error("{} versions of the full site package already exist for '{}'. Stopping so we don't get stuck in an infinite loop.", version, path);
                return;
            }
        }

        propertiesMap = new HashMap<>();
        propertiesMap.put(JCR_PRIMARY_TYPE, NT_FILE);
        packageResource = resourceResolver.create(groupResource, filename, propertiesMap);

        propertiesMap = new HashMap<>();
        propertiesMap.put(JCR_PRIMARY_TYPE, NT_RESOURCE);
        propertiesMap.put(JCR_MIME_TYPE, ZIP_MIME_TYPE);
        propertiesMap.put(JcrConstants.JCR_MIXINTYPES, VLT_PACKAGE);
        //jcr:data property must exist when the node is created but does not need to have anything in it
        propertiesMap.put(JCR_DATA, "");
        Resource contentResource = resourceResolver.create(packageResource, JCR_CONTENT, propertiesMap);

        propertiesMap = new HashMap<>();
        propertiesMap.put(GROUP_PROPERTY, siteName);
        propertiesMap.put(JCR_PRIMARY_TYPE, VLT_PACKAGE_DEFINITION);
        propertiesMap.put(NAME_PROPERTY, packageName);
        propertiesMap.put(VERSION_PROPERTY, ""+version);
        Resource vltDefinitionResource = resourceResolver.create(contentResource, VLT_DEFINITION, propertiesMap);

        propertiesMap = new HashMap<>();
        propertiesMap.put(JCR_PRIMARY_TYPE, NT_UNSTRUCTURED);
        Resource filterResource = resourceResolver.create(vltDefinitionResource, FILTER, propertiesMap);

        for(int i = 0; i < packagePaths.size(); i++) {
            String filterPath = packagePaths.get(i);
            propertiesMap = new HashMap<>();
            propertiesMap.put(MODE_PROPERTY, REPLACE_VALUE);
            propertiesMap.put(ROOT_PROPERTY, filterPath);
            propertiesMap.put(RULES_PROPERTY, new String[0]);
            //Named to match the filters that appear under naturally created packages
            resourceResolver.create(filterResource, "f"+i, propertiesMap);
        }

    }

    private void updateStringsInFiles(final Resource target, final String siteName) {
        final Resource content = target.getChild(JCR_CONTENT);
        if (content == null) {
            logger.error("No jcr:content resource for resource '{}'", target.getPath());
            return;
        }

        final Resource replacements = content.getChild("replacements");
        if (replacements == null) {
            logger.info("No replacements defined for resource '{}'", target.getPath());
            return;
        }

        for (final Resource replacement : replacements.getChildren()) {
            // If the file resource doesn't have children, we don't need to do anything
            // since the children define the actual replacements
            if (replacement.hasChildren()) {
                final String fileName = replacement.getName();
                final Resource file = target.getChild(fileName);
                if (file != null) {
                    updateStringsInFile(file, replacement, siteName);
                }
            }
        }
    }

    private void updateStringsInFile(final Resource file, final Resource replacements, final String siteName) {
        String content = getFileContentAsString(file);
        if (isBlank(content)) {
            return;
        }

        for (final Resource replacement : replacements.getChildren()) {
            content = replaceSiteName(content, replacement.getValueMap(), siteName);
        }

        try {
            replaceFileContent(file, content);
        } catch (final IOException e) {
            logger.error("IOException replacing contents of file " + file.getPath(), e);
        } catch (RepositoryException e) {
            logger.error("RepositoryException replacing contents of file " + file.getPath(), e);
        }
    }

    private String getFileContentAsString(final Resource file) {
        try (final InputStream is = file.adaptTo(InputStream.class)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8.name());
        } catch (final IOException e) {
            logger.error("Exception getting contents of file:" + file.getPath(), e);
            return null;
        }
    }

    private String replaceSiteName(final String text, final ValueMap regexMap, final String siteName) {
        final String regex = regexMap.get("regex", EMPTY);
        String replaceWith = regexMap.get("replaceWith", EMPTY);
        if (isAnyBlank(regex, replaceWith)) {
            return text;
        }

        // "_SITENAME_" is a placeholder for the actual new site name
        replaceWith = replaceWith.replace("_SITENAME_", siteName);
        return text.replaceAll(regex, replaceWith);
    }

    private void replaceFileContent(Resource fileResource, String newContent) throws IOException, RepositoryException {
        if(fileResource == null)
        {
            logger.error("Could not replace file contents: resource was null");
            return;
        }
        Node fileNode = fileResource.adaptTo(Node.class);
        if(fileNode == null) {
            logger.error("Could not replace file contents: could not adapt resource '{}' to node.", fileResource.getPath());
            return;
        }
        String mimeType = "";
        Resource fileContent = fileResource.getChild("jcr:content");
        if(fileContent != null) {
            ValueMap fileContentProperties = fileContent.getValueMap();
            mimeType = fileContentProperties.get("jcr:mimeType", "");
        }
        InputStream newContentStream = IOUtils.toInputStream(newContent, StandardCharsets.UTF_8.name());
        JcrUtils.putFile(fileNode.getParent(), fileNode.getName(), mimeType, newContentStream);
    }

    private void createResourceFromString(Resource parent, String name, String data) throws ManagementException {
        final Optional<Resource> optionalParent = Optional.ofNullable(parent);
        final String parentPathDisplay = optionalParent
                .map(Resource::getPath)
                .orElse(null);
        final Node parentNode = optionalParent
                .map(r -> r.adaptTo(Node.class))
                .orElse(null);
        if (parentNode == null) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, name, parentPathDisplay, name));
        }

        try {
            Node newAsset = parentNode.addNode(name, NT_FILE);
            Node content = newAsset.addNode(JCR_CONTENT, NT_RESOURCE);
            content.setProperty(JCR_DATA, data);
            content.setProperty(JCR_MIME_TYPE, TEXT_MIME_TYPE);
        } catch(RepositoryException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, name, parentPathDisplay, name), e);
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
                final String message = String.format("not able to delete %s", resource.getPath());
                throw new ManagementException(message);
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
            logger.warn(String.format(COPY_FAILED, source.getName(), source.getPath()), e);
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

    private final class StructureCopier {

        private final ResourceResolver resourceResolver;

        private String fromName;
        private String toName;

        private String patternSlashName;
        private String patternNameSlash;
        private int patternLength;

        public StructureCopier(final ResourceResolver resourceResolver) {
            this.resourceResolver = resourceResolver;
        }

        public void setFromName(final String fromName) {
            this.fromName = fromName;
            if (isEmpty(fromName)) {
                patternSlashName = null;
                patternNameSlash = null;
                patternLength = 0;
            } else {
                patternSlashName = SLASH + fromName;
                patternNameSlash = fromName + SLASH;
                patternLength = patternSlashName.length();
            }
        }

        public void setToName(final String toName) {
            this.toName = toName;
        }

        public Resource copyApps(final List<String> superTypes) {
            final String pathPrefix = APPS_ROOT + SLASH;
            final Resource source = getResource(resourceResolver, pathPrefix + fromName);
            if (source != null) {
                Resource target = getResource(resourceResolver, pathPrefix + toName);
                Resource answer = null;
                if (target == null) {
                    target = copyFolder(source, source.getParent(), toName);
                    answer = target;
                }

                if (target != null) {
                    // for each component in /apps/<fromSite>/components create a stub component in /apps/<toSite>/components
                    // with the sling:resourceSuperType set to the <fromSite> component
                    superTypes.addAll(copyStubs(source, target, COMPONENTS));
                    // for each object in /apps/<fromSite>/objects create a stub component in /apps/<toSite>/objects
                    // with the sling:resourceSuperType set to the <fromSite> object
                    copyStubs(source, target, OBJECTS);
                }

                return answer;
            }

            return null;
        }

        public Resource copyFromRoot(final String rootPath) {
            final Resource source = getResource(resourceResolver, rootPath + SLASH + fromName);
            if (source != null) {
                final Resource target = copyResources(source, source.getParent(), toName);
                if (target != null) {
                    // For deep copies, we need to know the depth of our copy, since the top-level assets will use the toName
                    // while child assets will use the name from the source; otherwise every asset has the same name
                    copyChildren(source, target, 0);
                }

                return target;
            }

            return null;
        }

        private void copyChildren(final Resource source, final Resource target, final int depth) {
            logger.trace("Copy Child Resource from: '{}', to: '{}'", source.getPath(), target.getPath());
            for (Resource child : source.getChildren()) {
                logger.trace("Child handling started: '{}'", child.getPath());
                try {
                    copyChild(child, target, depth);
                } catch (PersistenceException e) {
                    logger.warn(String.format(COPY_FAILED, source.getName(), source.getPath()), e);
                    return;
                }
                logger.trace("Child handled: '{}'", child.getPath());
            }
        }

        private void copyChild(final Resource sourceChild, final Resource targetParent, final int depth) throws PersistenceException {
            Map<String, Object> newProperties = copyProperties(sourceChild.getValueMap());
            if (patternLength > 0) {
                updatePaths(newProperties);
            }

            Resource childTarget = resourceResolver.create(targetParent, sourceChild.getName(), newProperties);
            updateTitle(childTarget, ((depth > 0) && newProperties.containsKey(JCR_TITLE)) ? (String) newProperties.get(JCR_TITLE) : toName);
            final String childTargetPathDisplay = Optional.ofNullable(childTarget)
                    .map(Resource::getPath)
                    .orElse(null);
            logger.trace("Child Target Created: '{}'", childTargetPathDisplay);
            // Copy grandchildren
            copyChildren(sourceChild, childTarget, depth + 1);
        }

        private void updatePaths(final Map<String, Object> properties) {
            for (final Entry<String, Object> entry : properties.entrySet()) {
                final Object temp = entry.getValue();
                if (temp instanceof String) {
                    String newValue = updatePath((String) temp);
                    if (newValue != null) {
                        entry.setValue(newValue);
                        logger.trace("Updated Properties: '{}'", properties);
                    }
                }
            }
        }

        private String updatePath(final String value) {
            int index = value.indexOf(patternSlashName);
            if (value.startsWith("/content/") && index > 0) {
                // Check if the string ends or if the next character is a slash to avoid collisions
                logger.trace("Value Length: {}, Index: {}, Pattern Length: {}", value.length(), index, patternLength);
                if (index + patternLength == value.length()) {
                    return value.substring(0, index) + SLASH + toName;
                } else if (value.charAt(index + patternLength) == '/') {
                    return value.substring(0, index) + SLASH + toName + SLASH + value.substring(index + patternLength + 1);
                }
            } else if (value.startsWith(patternNameSlash)) {
                return toName + SLASH + value.substring(patternLength);
            }

            return null;
        }

        private Resource copyFolder(final Resource folder, final Resource targetParent, final String folderName) {
            final Map<String, Object> newProperties = copyProperties(folder.getValueMap());
            logger.trace("Resource Properties: '{}'", newProperties);
            try {
                return resourceResolver.create(targetParent, folderName, newProperties);
            } catch(final PersistenceException e) {
                logger.warn(String.format(COPY_FAILED, folder.getName(), folder.getPath()), e);
            }

            return null;
        }

        private List<String> copyStubs(final Resource source, final Resource target, final String folderName) {
            final List<String> superTypes = new ArrayList<>();
            final Resource appsSource = getResource(source, folderName);
            if (appsSource == null) {
                return superTypes;
            }

            Resource appsTarget = getResource(resourceResolver, target.getPath() + SLASH + folderName);
            if (appsTarget == null) {
                appsTarget = copyFolder(appsSource, target, folderName);
                if (appsTarget == null) {
                    return superTypes;
                }
            }

            for (final Resource child : appsSource.getChildren()) {
                final ValueMap properties = child.getValueMap();
                final Map<String, Object> newProperties = new HashMap<>(properties);
                final String originalAppsPath = child.getPath().substring(APPS_ROOT.length() + 1);
                superTypes.add(originalAppsPath);
                newProperties.put(SLING_RESOURCE_SUPER_TYPE, originalAppsPath);
                try {
                    resourceResolver.create(appsTarget, child.getName(), newProperties);
                } catch(PersistenceException e) {
                    logger.warn(String.format(COPY_FAILED, folderName, child.getPath()), e);
                }
            }

            return superTypes;
        }
    }

    @Override
    public Resource updateResource(final ResourceResolver resourceResolver, final String path, final String jsonContent) throws ManagementException {
        if (isEmpty(jsonContent)) {
            throw new ManagementException(String.format(NO_CONTENT_PROVIDED, path));
        }

        try {
            final Resource answer = getResource(resourceResolver, path);
            if (answer == null) {
                throw new ManagementException(String.format(RESOURCE_NOT_FOUND, path));
            }

            // AS TODO: Check if we could add some guards here to avoid misplaced updates (JCR Primary Type / Sling Resource Type)
            final ResourceUpdater updater = new ResourceUpdater(resourceResolver, answer);
            updater.updateResourceTree(convertToMap(jsonContent));
            return answer;
        } catch(final IOException e) {
            throw new ManagementException(String.format(FAILED_TO_PARSE_JSON, jsonContent));
        }
    }

    private final class ResourceUpdater {

        private final ResourceResolver resourceResolver;
        private final Resource resource;

        public ResourceUpdater(
                final ResourceResolver resourceResolver,
                final Resource resource
        ) {
            this.resourceResolver = resourceResolver;
            this.resource = resource;
        }

        public void updateResourceTree(final Map<String, Object> properties) throws ManagementException {
            updateResourceTree(resource, properties);
        }

        private void updateResourceTree(final Resource resource, final Map<String, Object> properties) throws ManagementException {
            // Handle Deletion:
            // 1) Delete property with either 'true' or null as value -> remove the given resource
            // 2) Delete Property's value converted to string and then looked up as child of the given resource
            //    - If found delete that resource
            //    - If properties have an entry with that name and it is a Map -> remove it to avoid re-adding it during the processing of the properties
            if (deleteIfContainsMarkerProperty(properties)) {
                return;
            }

            for (final Entry<String, Object> entry: properties.entrySet()) {
                final String name = entry.getKey();
                final Object value = entry.getValue();
                if (value instanceof Map) {
                    updateMapProperty(name, (Map) value);
                } else if (value instanceof List) {
                    updateListProperty(name, (List) value);
                } else {
                    getModifiableProperties(resource, false).put(name, value);
                }
            }

            baseResourceHandler.updateModification(resource);
        }

        private boolean deleteIfContainsMarkerProperty(
                final Map<String, Object> properties
        ) throws ManagementException {
            if (properties.containsKey(DELETION_PROPERTY_NAME)) {
                final Object value = properties.get(DELETION_PROPERTY_NAME);
                if (isNullOrTrue(value)) {
                    // This indicates that this node shall be removed
                    try {
                        resourceResolver.delete(resource);
                        // This resource is gone so there is noting left that can be done here
                        return true;
                    } catch(final PersistenceException e) {
                        throw new ManagementException(String.format(FAILED_TO_DELETE, resource.getPath()), e);
                    }
                } else {
                    deleteChild(properties, value.toString());
                }
            }

            return false;
        }

        private void deleteChild(
                final Map<String, Object> properties,
                final String childName
        ) throws ManagementException {
            final Resource child = resource.getChild(childName);
            if (child != null) {
                try {
                    resourceResolver.delete(child);
                    if (properties.containsKey(childName)) {
                        final Object value = properties.get(childName);
                        if (value instanceof Map) {
                            properties.remove(childName);
                        }
                    }
                } catch(final PersistenceException e) {
                    throw new ManagementException(String.format(FAILED_TO_DELETE_CHILD, child.getPath()), e);
                }
            }
        }

        private void updateMapProperty(
                final String propertyName,
                final Map map
        ) throws ManagementException {
            final String childPath = (String) map.get(PATH);
            Resource child = resourceResolver.getResource(childPath);
            if (child == null) {
                child = resource.getChild(propertyName);
            }

            // If child is missing then create it
            if (child == null) {
                final String resourceType = (String) map.get(SLING_RESOURCE_TYPE);
                map.remove(NAME);
                map.remove(SLING_RESOURCE_TYPE);
                map.remove(JCR_PRIMARY_TYPE);
                child = createNode(resource, propertyName, NT_UNSTRUCTURED, resourceType);
                // Now update the child with any remaining properties
                final ModifiableValueMap newChildProperties = getModifiableProperties(child, false);
                final Set<Entry> childPropertyEntrySet = map.entrySet();
                for (final Entry childPropertyEntry: childPropertyEntrySet) {
                    final Object childPropertyKey = childPropertyEntry.getKey();
                    newChildProperties.put(String.valueOf(childPropertyKey), childPropertyEntry.getValue());
                }
            } else {
                updateResourceTree(child, map);
            }
        }

        private void updateListProperty(
                final String propertyName,
                final List list
        ) throws ManagementException {
            if (list.isEmpty()) {
                return;
            }

            final Object first = list.get(0);
            if (first instanceof Map) {
                Resource child = resource.getChild(propertyName);
                if(child == null) {
                    child = createNode(resource, propertyName, NT_UNSTRUCTURED, null);
                }

                // We support either a List of Objects (Maps) or list of Strings which are stored as multi-valued String property
                // for which we have to get all the values in a list and then afterwards if such values were found update
                // them as a property
                updateObjectList(list, child);
            } else if (first instanceof String) {
                updateObjectSingleList(propertyName, list);
            } else {
                throw new ManagementException(String.format(OBJECT_FIRST_ITEM_WITH_UNSUPPORTED_TYPE, first, (first == null ? "null" : first.getClass().getName())));
            }
        }

        private void updateObjectSingleList(String name, List incomingList) throws ManagementException {
            List<String> newSingleList = new ArrayList<>();
            for(Object item : incomingList) {
                if(item instanceof String) {
                    String listItem = item.toString();
                    if(listItem.isEmpty()) {
                        continue;
                    }
                    newSingleList.add(listItem);
                } else {
                    throw new ManagementException(String.format(OBJECT_ITEM_WITH_UNSUPPORTED_TYPE, item, (item == null ? "null" : item.getClass().getName())));
                }
            }

            getModifiableProperties(resource, false).put(name, newSingleList.toArray(new String[newSingleList.size()]));
        }

        private void updateObjectList(
                final List incomingList,
                final Resource resource
        ) throws ManagementException {
            Resource lastResourceItem = null;
            for(int i = 0; i < incomingList.size(); i++) {
                final Object item = incomingList.get(i);
                if(item instanceof Map) {
                    final Map incomingItemProperties = (Map) item;
                    final String incomingItemName = getString(incomingItemProperties, NAME);
                    if(isEmpty(incomingItemName)) {
                        throw new ManagementException(String.format(ITEM_NAME_MISSING, item, resource.getPath()));
                    }
                    // Get index of the matching resource child to compare with the index in the list
                    Resource resourceListItem = resource.getChild(incomingItemName);
                    int index = getChildIndex(resource, resourceListItem);
                    // Handle new item
                    if(resourceListItem == null) {
                        String resourceType = (String) incomingItemProperties.get(SLING_RESOURCE_TYPE);
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
                        final Set<Map.Entry> childPropertyEntrySet = incomingItemProperties.entrySet();
                        for(Map.Entry childPropertyEntry : childPropertyEntrySet) {
                            Object childPropertyKey = childPropertyEntry.getKey();
                            ModifiableValueMap newChildProperties = getModifiableProperties(resourceListItem, false);
                            newChildProperties.put(String.valueOf(childPropertyKey), childPropertyEntry.getValue());
                        }
                    } else {
                        if (incomingItemProperties.containsKey(DELETION_PROPERTY_NAME)
                            && isNullOrTrue(incomingItemProperties.get(DELETION_PROPERTY_NAME))) {
                            try {
                                logger.trace("Remove List Child: '{}' ('{}')", incomingItemName, resourceListItem.getPath());
                                resourceResolver.delete(resourceListItem);
                                continue;
                            } catch(final PersistenceException e) {
                                throw new ManagementException(String.format(FAILED_TO_DELETE, resourceListItem.getPath()), e);
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
                                final Iterator<Resource> ir = resource.getChildren().iterator();
                                if(ir.hasNext() && !lastResourceItem.getName().equals(ir.next().getName())) {
                                    moveNode(resourceListItem, lastResourceItem, false, false);
                                    break;
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
    }

    private Node createPageOrTemplate(Resource parent, String name, String templateComponent, String templatePath) throws RepositoryException {
        final Node parentNode = Optional.ofNullable(parent)
                .map(r -> r.adaptTo(Node.class))
                .orElse(null);
        if (parentNode == null) {
            return null;
        }

        Node newPage = parentNode.addNode(name, PAGE_PRIMARY_TYPE);
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

    public static void handleAssetDimensions(PerAsset perAsset) throws RepositoryException, IOException {
        InputStream is = perAsset.getRenditionStream((String) null);
        // Ignore images that do not have a jcr:data element aka stream
        if(is != null) {
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis);
                int minIndex = reader.getMinIndex();
                int width = reader.getWidth(minIndex);
                int height = reader.getHeight(minIndex);
                perAsset.addTag("per-data", "width", width);
                perAsset.addTag("per-data", "height", height);
                break;
            }
        }
    }
}
