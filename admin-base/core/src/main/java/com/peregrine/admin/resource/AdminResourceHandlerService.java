package com.peregrine.admin.resource;

import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.ASSET;
import static com.peregrine.commons.util.PerConstants.ASSETS_ROOT;
import static com.peregrine.commons.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.COMPONENT;
import static com.peregrine.commons.util.PerConstants.COMPONENTS;
import static com.peregrine.commons.util.PerConstants.COMPONENT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.CONTENT_ROOT;
import static com.peregrine.commons.util.PerConstants.DEPENDENCIES;
import static com.peregrine.commons.util.PerConstants.FELIBS_ROOT;
import static com.peregrine.commons.util.PerConstants.FOLDER;
import static com.peregrine.commons.util.PerConstants.INTERNAL;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_CREATED;
import static com.peregrine.commons.util.PerConstants.JCR_CREATED_BY;
import static com.peregrine.commons.util.PerConstants.JCR_DATA;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.JCR_UUID;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.NODE;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.NT_RESOURCE;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.OBJECT;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.OBJECTS_ROOT;
import static com.peregrine.commons.util.PerConstants.OBJECT_DEFINITION_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PACKAGES_PATH;
import static com.peregrine.commons.util.PerConstants.PAGE;
import static com.peregrine.commons.util.PerConstants.PAGES_ROOT;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PATH;
import static com.peregrine.commons.util.PerConstants.RENDITION;
import static com.peregrine.commons.util.PerConstants.SITE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_ORDERED_FOLDER;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_SUPER_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerConstants.TEMPLATE;
import static com.peregrine.commons.util.PerConstants.TEMPLATES_ROOT;
import static com.peregrine.commons.util.PerConstants.TENANT;
import static com.peregrine.commons.util.PerConstants.TEXT_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.VARIATIONS;
import static com.peregrine.commons.util.PerUtil.convertToMap;
import static com.peregrine.commons.util.PerUtil.getBoolean;
import static com.peregrine.commons.util.PerUtil.getChildIndex;
import static com.peregrine.commons.util.PerUtil.getClassOrNull;
import static com.peregrine.commons.util.PerUtil.getComponentVariableNameFromString;
import static com.peregrine.commons.util.PerUtil.getFirstChild;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static com.peregrine.commons.util.PerUtil.getNode;
import static com.peregrine.commons.util.PerUtil.getNodeAtPosition;
import static com.peregrine.commons.util.PerUtil.getPath;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.getString;
import static com.peregrine.commons.util.PerUtil.isPrimaryType;
import static com.peregrine.commons.util.PerUtil.isPropertyPresentAndEqualsTrue;
import static com.peregrine.commons.util.PerUtil.toStringOrNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.peregrine.adaption.PerAsset;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.rendition.BaseResourceHandler;
import com.peregrine.replication.ImageMetadataSelector;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
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
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Andreas Schaefer on 7/6/17.
 */
@Component(
    service = AdminResourceHandler.class,
    immediate = true
)
public class AdminResourceHandlerService
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
    private static final String INSERT_RESOURCE_PROPERTIES_MISSING = "To Insert a New Node the Node Properties must be provided";
    private static final String FAILED_TO_INSERT = "Failed to insert node at: '%s'";

    private static final String MOVE_FROM_RESOURCE_MISSING = "To Move a Node the Source Resource must be provided";
    public static final String MOVE_TO_RESOURCE_MISSING = "To Move a Node the Reference Resource must be provided";
    private static final String FAILED_TO_MOVE = "Failed to Move Resource. From: '%s' to: '%s'";

    private static final String RENAME_RESOURCE_MISSING = "To Rename a Node a Resource must be provided";
    private static final String NAME_TO_BE_RENAMED_TO_MUST_BE_PROVIDED = "Name to be renamed to must be provided";
    public static final String NAME_TO_BE_RENAMED_TO_CANNOT_CONTAIN_A_SLASH = "Name to be renamed to cannot contain a slash";
    private static final String FAILED_TO_RENAME = "Failed to Rename Resource. From: '%s' to: '%s'";

    private static final String PARENT_RESOURCE_MUST_BE_PROVIDED_TO_CREATE_ASSET = "Parent Resource must be provided to create Asset";
    private static final String ASSET_NAME_MUST_BE_PROVIDED_TO_CREATE_ASSET = "Asset Name must be provided to create Asset";
    private static final String CONTENT_TYPE_MUST_BE_PROVIDED_TO_CREATE_ASSET = "Content Type must be provided to create Asset";
    private static final String INPUT_STREAM_MUST_BE_PROVIDED_TO_CREATE_ASSET = "Input Stream must be provided to create Asset";
    private static final String FAILED_TO_CREATE = "Failed to Create %s in Parent: '%s', name: '%s'";
    private static final String FAILED_TO_CREATE_RENDITION = "Failed to Create %s Rendition in Parent: '%s', name: '%s'";
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

    private static final List<String> IGNORED_PROPERTIES_FOR_COPY = new ArrayList<>();
    private static final List<String> IGNORED_RESOURCE_PROPERTIES_FOR_COPY = new ArrayList<>();

    public static final String MISSING_RESOURCE_RESOLVER_FOR_SITE_COPY = "Resource Resolver must be provide to copy a Site";
    public static final String MISSING_PARENT_RESOURCE_FOR_COPY_SITES = "Sites Parent Resource was not provided or does not exist";
    public static final String MISSING_SOURCE_SITE_NAME = "Source Name must be provide";
    public static final String SOURCE_NAME_AND_TARGET_NAME_CANNOT_BE_THE_SAME_VALUE = "Source Name and Target Name cannot be the same value: ";
    public static final String MISSING_NEW_SITE_NAME = "Name of the Target Site must be provided to copy a Site";
    public static final String SOURCE_SITE_DOES_NOT_EXIST = "Source Site: '%s' was not provided or does not exist";
    public static final String TARGET_SITE_EXISTS = "Target Site: '%s' does exist and so copy failed";
    public static final String INVALID_SOURCE_SITE = "Source Site: '%s' is not a a valid site";
    public static final String COPY_FAILED = "Copy of %s: '%s' failed";
    private static final String IMAGE_METADATA_TAG_NAME = "Image Metadata Tag Name: '{}'";
    private static final String ADD_TAG_CATEGORY_TAG_NAME_VALUE = "Add Tag, Category: '{}', Tag Name: '{}', Value: '{}'";

    public static final String MISSING_RESOURCE_RESOLVER_FOR_UPDATE = "Resource Resolver must be provided to update a site from its source";
    public static final String MISSING_SITE_RESOURCE = "Site: '%s' does not exist";
    public static final String MISSING_CONTENT_RESOURCE = "Site: '%s' does not have a jcr:content resource";
    public static final String MISSING_SOURCE_NAME = "Site: '%s' does not have a source site name";
    public static final String MISSING_SITE_LOCATIONS = "Resources for site: '%s' could not be found";
    public static final String MISSING_SOURCE_LOCATIONS = "Resources for source site: '%s' could not be found";

    private static final String SOURCE_SITE = "sourceSite";

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
    private static final String RESOURCE_NAME_COLLISION = "Resource '%s' already has a child named '%s'";
    private static final String CANNOT_MOVE_RESOURCE_BELOW_ITSELF_OR_DESCENDANT = "Cannot move resource '%s' below itself or one of its descendants '%s'";
    private static final String RESOURCE_RESOLVER_PROVIDED_TO_COPY_OPERATION_IS_NULL = "Resource resolver provided to copy operation is null";
    private static final String NO_RESOURCE_TO_COPY_PROVIDED = "No resource to copy provided.";
    private static final String NO_NEW_PARENT_RESOURCE_PROVIDED = "No new parent resource provided.";
    private static final String NO_JCR_CONTENT_FOR_COPY = "Resource being copied '%s' does not have a jcr:content resource child";
    private static final String COPY_GENERIC_EXCEPTION = "Exception occurred copying '%s' to '%s'";
    private static final String VAR = "/var";
    private static final String NO_VAR_RESOURCE = "No resource exists at /var so temp resource could not be created";
    private static final String TEMP = "temp";
    private static final String NO_COPIED_RESOURCE = "Resource copy should've yielded a resource at '%s' but our resource is null";
    private static final String REORDER_EXCEPTION = "Exception occurred trying to order node '%s' before '%s'";

    private static final String NAME_CONSTRAINT_VIOLATION = "The provided name '%s' is not valid.";


    static {
        IGNORED_PROPERTIES_FOR_COPY.add(JCR_PRIMARY_TYPE);
        IGNORED_PROPERTIES_FOR_COPY.add(JCR_UUID);

        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_UUID);
        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_CREATED);
        IGNORED_RESOURCE_PROPERTIES_FOR_COPY.add(JCR_CREATED_BY);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Json Mapper for pretty print of Json text
     **/
    private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Reference
    private ResourceRelocation resourceRelocation;

    @Reference
    private BaseResourceHandler baseResourceHandler;

    @Reference
    private NodeNameValidation nodeNameValidation;

    private List<ImageMetadataSelector> imageMetadataSelectors = new ArrayList<>();

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC
    )
    void addImageMetadataSelector(ImageMetadataSelector selector) {
        imageMetadataSelectors.add(selector);
    }

    void removeImageMetadataSelector(ImageMetadataSelector selector) {
        imageMetadataSelectors.remove(selector);
    }

    @Override
    public Resource createFolder(ResourceResolver resourceResolver, String parentPath, String name) throws ManagementException {
        if(!nodeNameValidation.isValidPageName(name)) {
            throw new ManagementException(String.format(NAME_CONSTRAINT_VIOLATION, name));
        }
        try {
            if (isEmpty(name)) {
                throw new ManagementException(String.format(NAME_UNDEFINED, FOLDER, parentPath));
            }
            final Node parent = getNode(resourceResolver, parentPath);
            if (parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, FOLDER, parentPath, name));
            }
            Node newFolder = parent.addNode(name, SLING_ORDERED_FOLDER);
            newFolder.setProperty(JCR_TITLE, name);
            baseResourceHandler.updateModification(resourceResolver, newFolder);
            return adaptNodeToResource(resourceResolver, newFolder);
        } catch (RepositoryException e) {
            logger.debug("Failed to create Folder. Parent Path: '{}', Name: '{}'", parentPath, name);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, FOLDER, parentPath, name), e);
        }
    }

    private Resource adaptNodeToResource(ResourceResolver resourceResolver, Node node) throws RepositoryException {
        if (node == null) {
            return null;
        }
        return resourceResolver.getResource(node.getPath());
    }

    @Override
    public Resource createObject(ResourceResolver resourceResolver, String parentPath, String name, String resourceType) throws ManagementException {
        if(!nodeNameValidation.isValidPageName(name)) {
            throw new ManagementException(String.format(NAME_CONSTRAINT_VIOLATION, name));
        }
        try {
            if (isEmpty(name)) {
                throw new ManagementException(String.format(NAME_UNDEFINED, OBJECT, parentPath));
            }
            final Node parent = getNode(resourceResolver, parentPath);
            if (parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, OBJECT, parentPath, name));
            }
            Node newObject = parent.addNode(name, OBJECT_PRIMARY_TYPE);
            newObject.setProperty(JCR_TITLE, name);
            if (!isEmpty(resourceType)) {
                newObject.setProperty(SLING_RESOURCE_TYPE, resourceType);
            }
            baseResourceHandler.updateModification(resourceResolver, newObject);
            return adaptNodeToResource(resourceResolver, newObject);
        } catch (RepositoryException e) {
            logger.debug("Failed to create Object. Parent Path: '{}', Name: '{}'", parentPath, name);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, OBJECT, parentPath, name), e);
        }
    }

    @Override
    public Resource createPage(ResourceResolver resourceResolver, String parentPath, String name, String templatePath, String title) throws ManagementException {
        if(!nodeNameValidation.isValidPageName(name)) {
            throw new ManagementException(String.format(NAME_CONSTRAINT_VIOLATION, name));
        }
        try {
            Resource parent = getResource(resourceResolver, parentPath);
            if (parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, PAGE, parentPath, name));
            }
            if (isEmpty(name)) {
                throw new ManagementException(String.format(NAME_UNDEFINED, PAGE, parentPath));
            }
            Resource templateResource = getResource(resourceResolver, templatePath + "/" + JCR_CONTENT);
            if (templateResource == null) {
                throw new ManagementException(String.format(TEMPLATE_NOT_FOUND, templatePath));
            }
            String templateComponent = templateResource.getValueMap().get(SLING_RESOURCE_TYPE, String.class);
            Node newPage = createPageOrTemplate(parent, name, templateComponent, templatePath, title);
            return adaptNodeToResource(resourceResolver, newPage);
        } catch (RepositoryException e) {
            logger.debug("Failed to create Page. Parent Path: '{}', Name: '{}', Template Path: '{}'", parentPath, name, templatePath);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, PAGE, parentPath, name), e);
        }
    }

    @Override
    public Resource createTemplate(ResourceResolver resourceResolver, String parentPath, String name, String component, String title) throws ManagementException {
        if(!nodeNameValidation.isValidPageName(name)) {
            throw new ManagementException(String.format(NAME_CONSTRAINT_VIOLATION, name));
        }
        try {
            Resource parent = getResource(resourceResolver, parentPath);
            if (parent == null) {
                throw new ManagementException(String.format(PARENT_NOT_FOUND, TEMPLATE, parentPath, name));
            }
            if (isEmpty(name)) {
                throw new ManagementException(String.format(NAME_UNDEFINED, TEMPLATE, parentPath));
            }
            Node newPage = createPageOrTemplate(parent, name, component, null, title);
            // If there is a component then we check the component node and see if there is a child jcr:content node
            // If found we copy this over into our newly created node
            if (isNotEmpty(component)) {
                logger.trace("Component: '{}' provided for template. Copy its properties over if there is a JCR Content Node", component);
                copyAppsComponentToNewTemplate(component, newPage);
            }
            return adaptNodeToResource(resourceResolver, newPage);
        } catch (RepositoryException e) {
            logger.debug("Failed to create Template. Parent Path: '{}', Name: '{}'", parentPath, name);
            throw new ManagementException(String.format(FAILED_TO_HANDLE, TEMPLATE, parentPath, name), e);
        }
    }

    private void copyAppsComponentToNewTemplate(String component, Node template) throws RepositoryException, ManagementException {
        try {
            if (component.startsWith(SLASH)) {
                logger.warn("Component (for template): '{}' started with a slash which is not valid -> ignored", component);
            } else if (template != null) {
                String componentPath = APPS_ROOT + SLASH + component;
                final Session session = template.getSession();
                if (session.itemExists(componentPath)) {
                    Node componentNode = session.getNode(componentPath);
                    if (componentNode.hasNode(JCR_CONTENT)) {
                        Node source = componentNode.getNode(JCR_CONTENT);
                        Node target = template.getNode(JCR_CONTENT);
                        copyNode(source, target, true);
                    }
                }
            }
        } catch (PathNotFoundException e) {
            logger.warn("Component (for template)t: '{}' not found -> ignored", component);
        }
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
        final Node node = getNode(resource);
        if (node == null) {
            throw new ManagementException(INSERT_RESOURCE_MISSING);
        }
        try {
            final Node newNode;
            final ResourceResolver resourceResolver = resource.getResourceResolver();
            if (addAsChild) {
                newNode = createNode(node, properties, variation);
                baseResourceHandler.updateModification(resourceResolver, newNode);
                if (orderBefore) {
                    final Iterator<Resource> i = resource.listChildren();
                    if (i.hasNext()) {
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
        if (fromResource == null) {
            throw new ManagementException(MOVE_FROM_RESOURCE_MISSING);
        }
        if (toResource == null) {
            throw new ManagementException(MOVE_TO_RESOURCE_MISSING);
        }
        try {
            Resource answer = fromResource;
            if (addAsChild) {
                boolean sameParent = resourceRelocation.isChildOfParent(fromResource, toResource);
                if (!sameParent) {
                    //AS TODO: Shouldn't we try to update the references ?
                    // If not the same parent then just move as they are added at the end
                    if(isAncestor(fromResource, toResource)) {
                        throw new ManagementException(String.format(CANNOT_MOVE_RESOURCE_BELOW_ITSELF_OR_DESCENDANT, fromResource.getPath(), toResource.getPath()));
                    }
                    answer = resourceRelocation.moveToNewParent(fromResource, toResource, false);
                }
                if (orderBefore || sameParent) {
                    // If we move to the front or if it is the same parent (move to the end)
                    // No Target Child Name means we move it the front for before and end for after
                    resourceRelocation.reorder(toResource, fromResource.getName(), null, orderBefore);
                }
                baseResourceHandler.updateModification(answer);
            } else {
                // Both BEFORE and AFTER can be handled in one as the only difference is if added before or after
                // and if they are the same parent we means we only ORDER otherwise we MOVE first
                boolean sameParent = resourceRelocation.hasSameParent(fromResource, toResource);
                if (!sameParent) {
                    if(isAncestor(fromResource, toResource.getParent())) {
                        throw new ManagementException(String.format(CANNOT_MOVE_RESOURCE_BELOW_ITSELF_OR_DESCENDANT, fromResource.getPath(), toResource.getParent().getPath()));
                    }
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
        if (isEmpty(newName)) {
            throw new ManagementException(NAME_TO_BE_RENAMED_TO_MUST_BE_PROVIDED);
        }
        if(!nodeNameValidation.isValidNodeName(newName)) {
            throw new ManagementException(String.format(NAME_CONSTRAINT_VIOLATION, newName));
        }
        if(fromResource == null) {
            throw new ManagementException(RENAME_RESOURCE_MISSING);
        }
        if (newName.indexOf('/') >= 0) {
            throw new ManagementException(NAME_TO_BE_RENAMED_TO_CANNOT_CONTAIN_A_SLASH);
        }
        if(fromResource.getParent() != null && fromResource.getParent().getChild(newName) != null) {
            throw new ManagementException(String.format(RESOURCE_NAME_COLLISION, fromResource.getParent().getPath(), newName));
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
        if(!nodeNameValidation.isValidNodeName(assetName)) {
            throw new ManagementException(String.format(NAME_CONSTRAINT_VIOLATION, assetName));
        }
        if(parent == null) {
            throw new ManagementException(PARENT_RESOURCE_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        if (isEmpty(contentType)) {
            throw new ManagementException(CONTENT_TYPE_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        if (inputStream == null) {
            throw new ManagementException(INPUT_STREAM_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }

        final Node parentNode = getNode(parent);
        if (parentNode == null) {
            throw new ManagementException(PARENT_RESOURCE_MUST_BE_PROVIDED_TO_CREATE_ASSET);
        }
        try {
            if (parentNode.hasNode(assetName)) {
                // Node already exists -> delete it
                Node existingNode = parentNode.getNode(assetName);
                existingNode.remove();
            }
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
        } catch (RepositoryException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, ASSET, parent.getPath(), assetName), e);
        } catch (IOException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, RENDITION, parent.getPath(), assetName), e);
        }
    }

    private void processNewAsset(PerAsset asset) throws IOException, RepositoryException {
        if (asset == null) {
            return;
        }

        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(asset.getRenditionStream((Resource) null));
            for (Directory directory : metadata.getDirectories()) {
                String directoryName = directory.getName();
                logger.trace("Image Metadata Directory: '{}'", directoryName);
                ImageMetadataSelector selector = null;
                for (ImageMetadataSelector item : imageMetadataSelectors) {
                    String temp = item.acceptCategory(directoryName);
                    if (temp != null) {
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
        } catch (ImageProcessingException e) {
            logger.debug(EMPTY, e);
        }
    }

    private void addTagsToNewAssetAsJson(PerAsset asset, Directory directory, String directoryName, ImageMetadataSelector selector) throws PersistenceException, RepositoryException {
        final StringBuilder json = new StringBuilder("{");
        for (final Tag tag : directory.getTags()) {
            final String name = tag.getTagName();
            logger.trace(IMAGE_METADATA_TAG_NAME, name);
            final String tagName = selector != null ? selector.acceptTag(name) : name;
            if (tagName != null) {
                logger.trace(ADD_TAG_CATEGORY_TAG_NAME_VALUE, directoryName, tagName, tag.getDescription());
                json.append("\"");
                json.append(tagName);
                json.append("\":\"");
                json.append(tag.getDescription());
                json.append("\",");
            }
        }
        final int length = json.length();
        if (length > 1) {
            json.deleteCharAt(length - 1);
            json.append("}");
            asset.addTag(directoryName, RAW_TAGS, json.toString());
        }
    }

    private void addTagsToNewAsset(PerAsset asset, Directory directory, String directoryName, ImageMetadataSelector selector) throws PersistenceException, RepositoryException {
        for (final Tag tag : directory.getTags()) {
            final String name = tag.getTagName();
            logger.trace(IMAGE_METADATA_TAG_NAME, name);
            final String tagName = selector != null ? selector.acceptTag(name) : name;
            if (tagName != null) {
                logger.trace(ADD_TAG_CATEGORY_TAG_NAME_VALUE, directoryName, tagName, tag.getDescription());
                asset.addTag(directoryName, tagName, tag.getDescription());
            }
        }
    }

    @Override
    public Resource createNode(Resource parent, String name, String primaryType, String resourceType) throws ManagementException {
        if(!nodeNameValidation.isValidNodeName(name)) {
            throw new ManagementException(String.format(NAME_CONSTRAINT_VIOLATION, name));
        }
        final Map<String, Object> properties = new HashMap<>();
        properties.put(JCR_PRIMARY_TYPE, primaryType);
        if (isNotEmpty(resourceType)) {
            properties.put(SLING_RESOURCE_TYPE, resourceType);
        }
        try {
            return parent.getResourceResolver().create(parent, name, properties);
        } catch (final PersistenceException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, NODE, parent.getPath(), name), e);
        } catch (final RuntimeException e) {
            logger.debug("Failed to create Node, parent: '{}', name: '{}', properties: '{}'", parent, name, properties);
            throw new ManagementException(String.format(FAILED_TO_CREATE, NODE, parent.getPath(), name), e);
        }
    }


    // TODO: needs deep clone
    private Node createNode(
        @NotNull final Node parent,
        @NotNull final Map<String, Object> properties,
        final String variation
    ) throws RepositoryException, ManagementException {
        properties.remove(PATH);
        final String component = (String) properties.remove(COMPONENT);
        final Node newNode = addNewNode(parent);
        if (isEmpty(component)) {
            return newNode;
        }

        newNode.setProperty(SLING_RESOURCE_TYPE, component);

        // If there is a component then we check the component node and see if there is a child jcr:content node
        // If found we copy this over into our newly created node
        if (component.startsWith(SLASH)) {
            logger.warn("Component: '{}' started with a slash which is not valid -> ignored", component);
        } else {
            copyPropertiesFromComponentVariation(newNode, variation);
        }

        applyProperties(newNode, properties);
        return newNode;
    }

    private Node addNewNode(final Node parent) throws RepositoryException {
        return parent.addNode("n" + UUID.randomUUID(), NT_UNSTRUCTURED);
    }

    private void copyPropertiesFromComponentVariation(Node node, String variation) throws RepositoryException, ManagementException {
        final String componentPath = APPS_ROOT + SLASH + node.getProperty(SLING_RESOURCE_TYPE).getString();
        copyPropertiesFromComponentVariation(node, componentPath, variation);
    }

    private void copyPropertiesFromComponentVariation(Node node, String componentPath, String variation) throws RepositoryException, ManagementException {
        final Session session = node.getSession();
        Node contentNode = getComponentContentNode(session, componentPath);
        if (contentNode == null) {
            return;
        }

        if (isPropertyPresentAndEqualsTrue(contentNode, VARIATIONS)) {
            contentNode = getVariationContentOrDefault(contentNode, variation);
        }

        if (contentNode != null) {
            logger.trace("Copy Node: '{}' to: '{}'", contentNode, node);
            copyNode(contentNode, node, true);
        }
    }

    private Node getComponentContentNode(Session session, String path) throws RepositoryException {
        if (!session.itemExists(path)) {
            return null;
        }

        final List<String> alreadyVisitedNodes = new ArrayList<>();
        Node component = session.getNode(path);
        while (true) {
            // If we find the JCR Content then we are done here otherwise try to find this one's super resource type
            if (component.hasNode(JCR_CONTENT)) {
                return component.getNode(JCR_CONTENT);
            }

            // If we already visited that node then exit to avoid an endless loop
            String componentPath = component.getPath();
            if (alreadyVisitedNodes.contains(componentPath)) {
                return null;
            }

            alreadyVisitedNodes.add(componentPath);
            if (!component.hasProperty(SLING_RESOURCE_SUPER_TYPE)) {
                return null;
            }

            final String resourceSuperType = component.getProperty(SLING_RESOURCE_SUPER_TYPE).getString();
            if (isEmpty(resourceSuperType)) {
                return null;
            }

            componentPath = APPS_ROOT + SLASH + resourceSuperType;
            try {
                component = session.getNode(componentPath);
                logger.trace("Found Resource Super Type: '{}'", componentPath);
            } catch (final PathNotFoundException e) {
                logger.warn("Could not find Resource Super Type Component: " + componentPath + " -> ignore component", e);
                return null;
            }
        }
    }

    private Node getVariationContentOrDefault(Node node, String variation) throws RepositoryException {
        // Look up the variation node
        final Node variationNode;
        if (isNotEmpty(variation) && node.hasNode(variation)) {
            variationNode = node.getNode(variation);
        } else {
            logger.trace("Variation: '{}' is given but no such child node found under: '{}' -> use first one", variation, node.getPath());
            variationNode = getFirstChild(node);
        }

        if (variationNode != null && variationNode.hasNode(JCR_CONTENT)) {
            return variationNode.getNode(JCR_CONTENT);
        }

        return null;
    }

    private void applyProperties(@NotNull Node node, @NotNull Map properties) throws RepositoryException, ManagementException {
        String prettyJson = prettyPrintJson(properties);
        logger.trace("Apply Properties, Node: '{}', props: '{}'", node, prettyJson);
        Set<Entry> entrySet = properties.entrySet();
        entrySet = entrySet.stream()
            .filter(e -> !IGNORED_PROPERTIES_FOR_COPY.contains(e.getKey()))
            .collect(Collectors.toSet());
        for (final Entry entry : entrySet) {
            final String key = toStringOrNull(entry.getKey());
            final Object value = entry.getValue();
            prettyJson = prettyPrintJson(value);
            logger.trace("Apply Props, handle prop: '{}'='{}', value type: '{}'", key, prettyJson, getClassOrNull(value));
            if (value instanceof String) {
                node.setProperty(key, (String) value);
            } else if (value instanceof List) {
                final List list = (List) value;
                // Get sub node
                if (node.hasNode(key)) {
                    applyChildProperties(node.getNode(key), list);
                } else {
                    applyChildProperties(node, list);
                }
            }
        }
    }

    private void applyChildProperties(@NotNull Node parent, @NotNull List childProperties) throws RepositoryException, ManagementException {
        // Loop over Array
        int counter = 0;
        for (final Object item : childProperties) {
            if (item instanceof Map) {
                applyChildProperties(parent, (Map) item, counter);
            } else {
                logger.warn("Array item: '{}' is not an Object and so ignored", item);
            }
            counter++;
        }
    }

    private void applyChildProperties(@NotNull Node parent, @NotNull Map properties, int position) throws RepositoryException, ManagementException {
        // Find matching child by name
        final String name = extractName(properties);
        if (isBlank(name)) {
            logger.warn("Neither Name nor Path Found in Object: '{}'", properties);
            return;
        }

        // Apply data
        if (parent.hasNode(name)) {
            applyProperties(parent.getNode(name), properties);
            return;
        }

        // No name or matching path name (auto generated IDs) -> use position to find target
        final Node target = getNodeAtPosition(parent, position);
        if (target != null) {
            logger.trace("Found Target by position: '{}'", target.getPath());
            // Check if component matches
            final Object sourceComponent = properties.get(COMPONENT);
            final Object targetComponent = target.getProperty(COMPONENT).getString();
            if (sourceComponent == null || !sourceComponent.equals(targetComponent)) {
                logger.warn("Source Component: '{}' does not match target: '{}'", sourceComponent, targetComponent);
            } else {
                applyProperties(target, properties);
            }
        } else {
            final String path = getPropsFromMap(properties, PATH, EMPTY);
            final Node sourceNode = findSourceByPath(parent, path.split(SLASH));
            if (sourceNode != null) {
                Node newNode = addNewNode(parent);
                if (sourceNode.hasProperty(SLING_RESOURCE_TYPE)) {
                    String componentName = sourceNode.getProperty(SLING_RESOURCE_TYPE).getString();
                    newNode.setProperty(SLING_RESOURCE_TYPE, componentName);
                    logger.trace("Copy Props from Component Variation, component name: '{}'", componentName);
                    copyPropertiesFromComponentVariation(newNode, APPS_ROOT + SLASH + componentName, null);
                }
                logger.trace("Apply Properties to node: '{}', props: '{}'", newNode, properties);
                applyProperties(newNode, properties);
            }
        }
    }

    /**
     * Finds a node that is connected by a parent and that parent is part of the segments
     *
     * @param start    Starting Node. If no match is found we call this method with its parent if there is one
     * @param segments A full or partial path split into its node names (String.split())
     * @return The node that matches the last segment if a matching node is found or null if not found
     * @throws RepositoryException A node operation failed but that should not happen
     */
    private Node findSourceByPath(Node start, String[] segments) throws RepositoryException {
        final String startName = start.getName();
        final int length = segments.length;
        int i = 0;
        for (; i < length - 1; i++) {
            if (StringUtils.equals(segments[i], startName)) {
                break;
            }
        }

        if (i < length - 1) {
            Node child = start;
            while (++i < length) {
                final String segment = segments[i];
                if (child.hasNode(segment)) {
                    child = child.getNode(segment);
                } else {
                    return null;
                }
            }
            return child;
        }

        final String segment = segments[length - 1];
        if (StringUtils.equals(segment, startName)) {
            // It is last entry so we found it
            return start;
        }

        // No child found -> go to parent and try again
        final Node parent = start.getParent();
        return parent == null ? null : findSourceByPath(parent, segments);
    }

    private String extractName(final Map properties) {
        final String name = toStringOrNull(properties.get(NAME));
        if (isNotBlank(name)) {
            return name;
        }

        final String path = toStringOrNull(properties.get(PATH));
        return PerUtil.extractName(path);

    }

    public Node copyNode(Node source, Node target, boolean deep) throws ManagementException {
        try {
            // Copy all properties
            final PropertyIterator pi = source.getProperties();
            while (pi.hasNext()) {
                Property property = pi.nextProperty();
                if (!IGNORED_PROPERTIES_FOR_COPY.contains(property.getName())) {
                    if (property.isMultiple()) {
                        target.setProperty(property.getName(), property.getValues(), property.getType());
                    } else {
                        target.setProperty(property.getName(), property.getValue(), property.getType());
                    }
                }
            }
            if (deep) {
                NodeIterator ni = source.getNodes();
                while (ni.hasNext()) {
                    Node sourceChild = ni.nextNode();
                    // Create Target first
                    Node targetChild = target.addNode(sourceChild.getName(), sourceChild.getPrimaryNodeType().getName());
                    copyNode(sourceChild, targetChild, true);
                }
            }
            return target;
        } catch (RepositoryException e) {
            throw new ManagementException(String.format(FAILED_TO_COPY, source, target), e);
        }
    }

    @Override
    public Resource copyTenant(ResourceResolver resourceResolver, String tenantsParentPath, String fromName, String toName) throws ManagementException {
        if(!nodeNameValidation.isValidSiteName(toName)) {
            throw new ManagementException(String.format(NAME_CONSTRAINT_VIOLATION, toName));
        }
        checkCopyTenantParameters(resourceResolver, tenantsParentPath, fromName, toName);

        final Resource sitesRoot = getResource(resourceResolver, tenantsParentPath);
        if (sitesRoot == null) {
            throw new ManagementException(MISSING_PARENT_RESOURCE_FOR_COPY_SITES);
        }
        final Resource source = getResource(sitesRoot, fromName);
        if (source == null) {
            throw new ManagementException(String.format(SOURCE_SITE_DOES_NOT_EXIST, fromName));
        }
        if (!isPrimaryType(source, SITE_PRIMARY_TYPE)) {
            throw new ManagementException(String.format(INVALID_SOURCE_SITE, fromName));
        }
        Resource answer = getResource(sitesRoot, toName);
        if (answer != null) {
            throw new ManagementException(String.format(TARGET_SITE_EXISTS, toName));
        } else {
            final Node rootNode = getNode(sitesRoot);
            Node newSite;
            try {
                newSite = rootNode.addNode(toName, SITE_PRIMARY_TYPE);
                newSite.setProperty(JCR_TITLE, toName);
                newSite.setProperty(TEMPLATE, false);
                newSite.setProperty(INTERNAL, false);
                newSite.setProperty(SOURCE_SITE, fromName);
                answer = getResource(resourceResolver, newSite.getPath());
            } catch (RepositoryException e) {
                logger.error("Error creating per:Site JCR node", e);
            }
        }

        final List<Resource> resourcesToPackage = new ArrayList<>();
        final List<String> superTypes = new ArrayList<>();

        final StructureCopier copier = new StructureCopier(resourceResolver, fromName, toName, answer);
        resourcesToPackage.add(copier.copyApps(superTypes));
        // copy /content/<fromTenant>/assets to /content/<toTenant>/assets
        resourcesToPackage.add(copier.copyFromRoot(ASSETS_ROOT));
        // copy /content/<fromTenant>/objects to /content/<toTenant>/objects and fix all references
        resourcesToPackage.add(copier.copyFromRoot(OBJECTS_ROOT));
        // copy /content/<fromTenant>/templates to /content/<toTenant>/templates and fix all references
        Resource templatesCopy = copier.copyFromRoot(TEMPLATES_ROOT);
        resourcesToPackage.add(templatesCopy);
        // Update css paths stored in /content/<toTenant>/pages in the template
        if (templatesCopy != null) {
            updateTemplateCssPaths(templatesCopy, fromName, toName);
        }
        // copy /content/<fromTenant>/pages to /content/<toTenant>/pages and fix all references
        Resource pagesCopy = copier.copyFromRoot(PAGES_ROOT);
        resourcesToPackage.add(pagesCopy);
        if (pagesCopy != null) {
            updateStringsInFiles(pagesCopy, toName);
        }

        // create an /etc/felibs/<toTenant> felib, extend felib to include a dependency on the /etc/felibs/<fromTenant>
        final Resource sourceResource = getResource(resourceResolver, FELIBS_ROOT + SLASH + fromName);
        if (sourceResource != null) {
            final Resource targetResource = copyResources(sourceResource, sourceResource.getParent(), toName, toName);
            resourcesToPackage.add(targetResource);
            logger.trace("Copied Felibs for Target: '{}': '{}'", toName, targetResource);
            final ValueMap properties = getModifiableProperties(targetResource, false);
            logger.trace("Copied Felibs Properties: '{}'", properties);
            if (properties != null) {
                String[] dependencies = properties.get(DEPENDENCIES, String[].class);
                if (dependencies == null) {
                    dependencies = new String[]{sourceResource.getPath()};
                } else {
                    final String[] newDependencies = new String[dependencies.length + 1];
                    System.arraycopy(dependencies, 0, newDependencies, 0, dependencies.length);
                    newDependencies[dependencies.length] = sourceResource.getPath();
                    dependencies = newDependencies;
                }
                properties.put(DEPENDENCIES, dependencies);
            }

            createResourceFromString(targetResource, "mapping.js", assembleMappingsJs(superTypes, fromName, toName));
            createResourceFromString(targetResource, "js.txt", "mapping.js\n");
        }

        try {
            final List<String> packagePaths = resourcesToPackage.stream()
                .filter(Objects::nonNull)
                .map(Resource::getPath)
                .filter(path -> !path.startsWith(CONTENT_ROOT + SLASH))
                .collect(Collectors.toList());

                packagePaths.add(CONTENT_ROOT + SLASH + toName);         
            createTenantPackage(resourceResolver, toName, packagePaths);
        } catch (PersistenceException e) {
            logger.error("Failed to create package for site " + toName, e);
        }

        return answer;
    }

    private void checkCopyTenantParameters(ResourceResolver resourceResolver, String tenantsParentPath, String fromName, String targetName) throws ManagementException {
        // Check the given parameters and make sure everything is correct
        if (resourceResolver == null) {
            throw new ManagementException(MISSING_RESOURCE_RESOLVER_FOR_SITE_COPY);
        }
        if (isBlank(tenantsParentPath)) {
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

    private String assembleMappingsJs(List<String> superTypes, String fromName, String targetName) {
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

    private String getMappingFileContent(String fromName, String targetName, List<String> superTypes) {
        StringBuilder mappings = new StringBuilder();
        for (String superType : superTypes) {
            String componentSourceName = PerUtil.getComponentVariableNameFromString(superType);
            String componentDestName = PerUtil.getComponentVariableNameFromString(superType.replace(fromName + "/", targetName + "/"));
            mappings.append("var ");
            mappings.append(componentDestName);
            mappings.append(" = ");
            mappings.append(componentSourceName);
            mappings.append('\n');
        }

        return mappings.toString();
    }

    private void createTenantPackage(ResourceResolver resourceResolver, String tenantName, List<String> packagePaths) throws PersistenceException {
        Resource packagesRoot = resourceResolver.getResource(PACKAGES_PATH);
        if (packagesRoot == null) {
            logger.error("Package root path '{}' could not be resolved.", PACKAGES_PATH);
            return;
        }

        Map<String, Object> propertiesMap;

        Resource groupResource = packagesRoot.getChild(tenantName);
        if (groupResource == null) {
            propertiesMap = new HashMap<>();
            propertiesMap.put(JCR_PRIMARY_TYPE, SLING_FOLDER);
            groupResource = resourceResolver.create(packagesRoot, tenantName, propertiesMap);
        }

        String packageName = tenantName + PACKAGE_SUFFIX;
        double version = DEFAULT_PACKAGE_VERSION;
        String filename = packageName + DASH + version + ZIP_EXTENSION;

        Resource packageResource = groupResource.getChild(filename);
        //Since it's possible that backups of a previous site with the same name exist, we'll increment
        //the version number until we find a version we don't have (or hit a maximum version number)
        while (packageResource != null) {
            version += 1;
            filename = packageName + DASH + version + ZIP_EXTENSION;
            packageResource = groupResource.getChild(filename);
            if (version >= MAXIMUM_VERSION) {
                final String path = getPath(packageResource);
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
        propertiesMap.put(GROUP_PROPERTY, tenantName);
        propertiesMap.put(JCR_PRIMARY_TYPE, VLT_PACKAGE_DEFINITION);
        propertiesMap.put(NAME_PROPERTY, packageName);
        propertiesMap.put(VERSION_PROPERTY, "" + version);
        Resource vltDefinitionResource = resourceResolver.create(contentResource, VLT_DEFINITION, propertiesMap);

        propertiesMap = new HashMap<>();
        propertiesMap.put(JCR_PRIMARY_TYPE, NT_UNSTRUCTURED);
        Resource filterResource = resourceResolver.create(vltDefinitionResource, FILTER, propertiesMap);

        for (int i = 0; i < packagePaths.size(); i++) {
            String filterPath = packagePaths.get(i);
            propertiesMap = new HashMap<>();
            propertiesMap.put(MODE_PROPERTY, REPLACE_VALUE);
            propertiesMap.put(ROOT_PROPERTY, filterPath);
            propertiesMap.put(RULES_PROPERTY, new String[0]);
            //Named to match the filters that appear under naturally created packages
            resourceResolver.create(filterResource, "f" + i, propertiesMap);
        }

    }

    private void updateTemplateCssPaths(Resource templateResource, String oldSiteName, String newSiteName) {
        final Resource templateContent = templateResource.getChild(JCR_CONTENT);
        if(templateContent == null) {
            logger.error("No jcr:content resource for resource '{}'", templateResource.getPath());
            return;
        }

        ValueMap templateContentProperties = templateContent.getValueMap();
        String[] siteCssProperty = templateContentProperties.get("siteCSS", String[].class);
        if(siteCssProperty != null && siteCssProperty.length > 0) {
            String[] cssReplacements = Arrays.stream(siteCssProperty)
                    .map(css -> css.replace(PAGES_ROOT.replace(TENANT, oldSiteName), PAGES_ROOT.replace(TENANT, newSiteName)))
                    .toArray(String[]::new);
            ModifiableValueMap modifiableProperties = getModifiableProperties(templateContent);
            modifiableProperties.put("siteCSS", cssReplacements);
        }
    }

    private void updateStringsInFiles(Resource target, String siteName) {
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

        for (Resource replacement : replacements.getChildren()) {
            //If the file resource doesn't have children, we don't need to do anything
            //since the children define the actual replacements
            if (replacement.hasChildren()) {
                final String fileName = replacement.getName();
                final Resource file = target.getChild(fileName);
                if (file != null) {
                    updateStringsInFile(file, replacement, siteName);
                }
            }
        }
    }

    private void updateStringsInFile(Resource file, Resource replacements, String siteName) {
        String content = getFileContentAsString(file);
        if (isBlank(content)) {
            return;
        }

        for (Resource replacement : replacements.getChildren()) {
            content = replaceTenantName(content, replacement.getValueMap(), siteName);
        }

        try {
            replaceFileContent(file, content);
        } catch (IOException e) {
            logger.error("IOException replacing contents of file " + file.getPath(), e);
        } catch (RepositoryException e) {
            logger.error("RepositoryException replacing contents of file " + file.getPath(), e);
        }
    }

    private String getFileContentAsString(Resource file) {
        try (InputStream is = file.adaptTo(InputStream.class)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8.name());
        } catch (final IOException e) {
            logger.error("Exception getting contents of file:" + file.getPath(), e);
            return null;
        }
    }

    private String replaceTenantName(String text, ValueMap regexMap, String tenantName) {
        final String regex = regexMap.get("regex", EMPTY);
        String replaceWith = regexMap.get("replaceWith", EMPTY);
        if (isAnyBlank(regex, replaceWith)) {
            return text;
        }

        //"_SITENAME_" is a placeholder for the actual new site name
        replaceWith = replaceWith.replace("_SITENAME_", tenantName);
        return text.replaceAll(regex, replaceWith);
    }

    private void replaceFileContent(Resource fileResource, String newContent) throws IOException, RepositoryException {
        if (fileResource == null) {
            logger.error("Could not replace file contents: resource was null");
            return;
        }
        Node fileNode = fileResource.adaptTo(Node.class);
        if (fileNode == null) {
            logger.error("Could not replace file contents: could not adapt resource '{}' to node.", fileResource.getPath());
            return;
        }
        String mimeType = "";
        Resource fileContent = fileResource.getChild(JCR_CONTENT);
        if (fileContent != null) {
            ValueMap fileContentProperties = fileContent.getValueMap();
            mimeType = fileContentProperties.get(JCR_MIME_TYPE, "");
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
        } catch (RepositoryException e) {
            throw new ManagementException(String.format(FAILED_TO_CREATE, name, parentPathDisplay, name), e);
        }
    }

    private void createResourceFromString(ResourceResolver resourceResolver, Resource parent, String name, String data) throws ManagementException {
        try {
            Node parentNode = parent.adaptTo(Node.class);
            Node newAsset = parentNode.addNode(name, NT_FILE);
            Node content = newAsset.addNode(JCR_CONTENT, NT_RESOURCE);
            content.setProperty(JCR_DATA, data);
            content.setProperty(JCR_MIME_TYPE, TEXT_MIME_TYPE);
        } catch (RepositoryException e) {
            logger.error("failed to create resource {}", name, e);
            throw new ManagementException(String.format(FAILED_TO_CREATE, name, parent.getPath(), name), e);
        }
    }

    @Override
    public void deleteTenant(ResourceResolver resourceResolver, String tenantsParentPath, String name) throws ManagementException {
        // Check the given parameters and make sure everything is correct
        if (resourceResolver == null) {
            throw new ManagementException(MISSING_RESOURCE_RESOLVER_FOR_SITE_COPY);
        }
        Resource parentResource = getResource(resourceResolver, tenantsParentPath);
        if (parentResource == null) {
            throw new ManagementException(MISSING_PARENT_RESOURCE_FOR_COPY_SITES);
        }
        if (isEmpty(name)) {
            throw new ManagementException(MISSING_SOURCE_SITE_NAME);
        }

        Resource source = getResource(parentResource, name);
        if (source == null) {
            throw new ManagementException(String.format(SOURCE_SITE_DOES_NOT_EXIST, name));
        }

        if (!isPrimaryType(source, SITE_PRIMARY_TYPE)) {
            throw new ManagementException(String.format(INVALID_SOURCE_SITE, name));
        }

        Resource appsSource = getResource(resourceResolver, APPS_ROOT + SLASH + name);
        deleteResource(resourceResolver, appsSource);

        Resource contentSource = getResource(resourceResolver, CONTENT_ROOT + SLASH + name);
        deleteResource(resourceResolver, contentSource);

        Resource felibsSource = getResource(resourceResolver, FELIBS_ROOT + SLASH + name);
        deleteResource(resourceResolver, felibsSource);
    }

    @Override
    public void updateTenant(ResourceResolver resourceResolver, String tenantName) throws ManagementException {
        if (resourceResolver == null) {
            throw new ManagementException(MISSING_RESOURCE_RESOLVER_FOR_UPDATE);
        }
        Resource siteResource = getResource(resourceResolver, CONTENT_ROOT + SLASH + tenantName);
        if (siteResource == null) {
            throw new ManagementException(String.format(MISSING_SITE_RESOURCE, tenantName));
        }
        ValueMap contentProperties = siteResource.getValueMap();
        String sourceSiteName = contentProperties.get(SOURCE_SITE, String.class);
        if (isBlank(sourceSiteName)) {
            throw new ManagementException(String.format(MISSING_SOURCE_NAME, tenantName));
        }

        Resource siteFelibsRoot = getResource(resourceResolver, FELIBS_ROOT + SLASH + tenantName);
        Resource siteAppsRoot = getResource(resourceResolver, APPS_ROOT + SLASH + tenantName);
        Resource sourceAppsRoot = getResource(resourceResolver, APPS_ROOT + SLASH + sourceSiteName);

        if (siteAppsRoot == null || siteFelibsRoot == null) {
            throw new ManagementException(String.format(MISSING_SITE_LOCATIONS, tenantName));
        }
        if (sourceAppsRoot == null) {
            throw new ManagementException(String.format(MISSING_SOURCE_LOCATIONS, sourceSiteName));
        }

        ArrayList<String> superTypes = new ArrayList<>();
        copyStubs(sourceAppsRoot, siteAppsRoot, COMPONENTS, superTypes);

        String mappingFileContent = getMappingFileContent(sourceSiteName, tenantName, superTypes);
        Resource mappingFileResource = siteFelibsRoot.getChild("mapping.js");
        if (mappingFileResource == null) {
            //TODO: We shouldn't ever end up here - not sure if we want to do this creation or just error out
            createResourceFromString(resourceResolver, siteFelibsRoot, "mapping.js", mappingFileContent);
        } else {
            Resource mappingFileContentResource = mappingFileResource.getChild(JCR_CONTENT);
            ValueMap properties = getModifiableProperties(mappingFileContentResource);
            properties.put(JCR_DATA, mappingFileContent);
        }
    }

    private void deleteResource(ResourceResolver resourceResolver, Resource resource) throws ManagementException {
        if (resource != null) {
            try {
                resourceResolver.delete(resource);
            } catch (PersistenceException e) {
                final String message = String.format("not able to delete %s", resource.getPath());
                throw new ManagementException(message);
            }
        }
    }

    private Resource copyResources(Resource source, Resource targetParent, String toName, String title) {
        Resource target = getResource(targetParent, toName);
        if (target != null) {
            logger.warn("Target Resource: '{}' already exist -> copy is ignored", target.getPath());
            return null;
        }
        Map<String, Object> newProperties = copyProperties(source.getValueMap());
        logger.trace("Resource Properties: '{}'", newProperties);
        try {
            target = source.getResourceResolver().create(targetParent, toName, newProperties);
            updateTitle(target, title);
        } catch (PersistenceException e) {
            logger.warn(String.format(COPY_FAILED, source.getName(), source.getPath()), e);
            return null;
        }
        logger.trace("New Resource Properties: '{}'", target.getValueMap());
        return target;
    }

    private Map<String, Object> copyProperties(ValueMap source) {
        Map<String, Object> answer = new HashMap<>(source);
        for (String ignore : IGNORED_RESOURCE_PROPERTIES_FOR_COPY) {
            if (answer.containsKey(ignore)) {
                answer.remove(ignore);
            }
        }
        return answer;
    }

    private void updateTitle(Resource resource, String title) {
        if (JCR_CONTENT.equals(resource.getName())) {
            ValueMap properties = getModifiableProperties(resource, false);
            if (properties.containsKey(JCR_TITLE)) {
                properties.put(JCR_TITLE, title);
            }
        }
    }

    private final class StructureCopier {

        private final ResourceResolver resourceResolver;
        private final String fromName;
        private final String toName;
        private final Resource sitesRoot;

        private String patternSlashName;
        private String patternNameSlash;
        private int patternLength;

        public StructureCopier(ResourceResolver resourceResolver, String fromName, String toName, Resource sitesRoot) {
            this.resourceResolver = resourceResolver;
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
            this.toName = toName;
            this.sitesRoot = sitesRoot;
        }

        public Resource copyApps(List<String> superTypes) {
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
                    // for each component in /apps/<fromTenant>/components create a stub component in /apps/<toTenant>/components
                    // with the sling:resourceSuperType set to the <fromTenant> component
                    superTypes.addAll(copyStubs(source, target, COMPONENTS));
                    // for each object in /apps/<fromTenant>/objects create a stub component in /apps/<toTenant>/objects
                    // with the sling:resourceSuperType set to the <fromTenant> object
                    copyStubs(source, target, OBJECTS);
                }
                return answer;
            }
            return null;
        }

        public Resource copyFromRoot(String rootPath) {
            final Resource source = getResource(resourceResolver, rootPath.replace(TENANT, fromName));
            String targetName = rootPath.replace(TENANT, toName).split("/")[3];
            if (source != null) {
                final Resource target = copyResources(source, sitesRoot, targetName, toName);
                if (target != null) {
                    ValueMap targetProps = getModifiableProperties(target, false);
                    if (targetProps.containsKey(JCR_TITLE)) {
                        String _title = ((String) targetProps.get(JCR_TITLE)).replace(fromName, toName);
                        targetProps.put(JCR_TITLE, _title);
                    }
                    // For deep copies, we need to know the depth of our copy, since the top-level assets will use the toName
                    // while child assets will use the name from the source; otherwise every asset has the same name
                    copyChildren(source, target, 0);
                    Resource contentResource = target.getChild(JCR_CONTENT);
                    if(contentResource != null) {
                        updateTitle(contentResource, targetName);
                    }
                }

                return target;
            }
            return null;
        }

        private void copyChildren(Resource source, Resource target, int depth) {
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

        private void copyChild(Resource sourceChild, Resource targetParent, int depth) throws PersistenceException {
            Map<String, Object> newProperties = copyProperties(sourceChild.getValueMap());
            if (patternLength > 0) {
                updatePaths(newProperties);
            }

            Resource childTarget = resourceResolver.create(targetParent, sourceChild.getName(), newProperties);
            updateTitle(childTarget, ((depth > 0) && newProperties.containsKey(JCR_TITLE)) ? (String) newProperties.get(JCR_TITLE) : toName);
            final String childTargetPathDisplay = getPath(childTarget);
            logger.trace("Child Target Created: '{}'", childTargetPathDisplay);
            // Copy grandchildren
            copyChildren(sourceChild, childTarget, depth + 1);
        }

        private void updatePaths(Map<String, Object> properties) {
            for (Entry<String, Object> entry : properties.entrySet()) {
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

        private String updatePath(String value) {
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

        private Resource copyFolder(Resource folder, Resource targetParent, String folderName) {
            final Map<String, Object> newProperties = copyProperties(folder.getValueMap());
            logger.trace("Resource Properties: '{}'", newProperties);
            try {
                return resourceResolver.create(targetParent, folderName, newProperties);
            } catch (PersistenceException e) {
                logger.warn(String.format(COPY_FAILED, folder.getName(), folder.getPath()), e);
            }
            return null;
        }

        private List<String> copyStubs(Resource source, Resource target, String folderName) {
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
            for (Resource child : appsSource.getChildren()) {
                if (isPrimaryType(child, COMPONENT_PRIMARY_TYPE) || isPrimaryType(child, OBJECT_DEFINITION_PRIMARY_TYPE)) {
                    final ValueMap properties = child.getValueMap();
                    final Map<String, Object> newProperties = new HashMap<>(properties);
                    final String originalAppsPath = child.getPath().substring(APPS_ROOT.length() + 1);
                    superTypes.add(originalAppsPath);
                    newProperties.put(SLING_RESOURCE_SUPER_TYPE, originalAppsPath);
                    try {
                        resourceResolver.create(appsTarget, child.getName(), newProperties);
                    } catch (PersistenceException e) {
                        logger.warn(String.format(COPY_FAILED, folderName, child.getPath()), e);
                    }
                }
            }
            return superTypes;
        }
    }

    private void updateResourceTree(Resource resource, Map<String, Object> properties) throws ManagementException {
        // Handle Deletion:
        // 1) Delete property with either 'true' or null as value -> remove the given resource
        // 2) Delete Property's value converted to string and then looked up as child of the given resource
        //    - If found delete that resource
        //    - If properties have an entry with that name and it is a Map -> remove it to avoid re-adding it during the processing of the properties
        if (deleteIfContainsMarkerProperty(resource, properties)) {
            return;
        }
        ModifiableValueMap updateProperties = getModifiableProperties(resource, false);
        for (Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                applyChildProperties(resource, name, (Map) value);
            } else if (value instanceof List) {
                applyListProperties(resource, name, (List)value);
            } else {
                updateProperties.put(name, value);
            }
        }
        baseResourceHandler.updateModification(resource);
    }

    private boolean deleteIfContainsMarkerProperty(Resource resource, Map<String, Object> properties) throws ManagementException {
        if (properties.containsKey(DELETION_PROPERTY_NAME)) {
            Object value = properties.get(DELETION_PROPERTY_NAME);
            if (value == null || Boolean.TRUE.toString().equalsIgnoreCase(value.toString())) {
                // This indicates that this node shall be removed
                try {
                    resource.getResourceResolver().delete(resource);
                    // This resource is gone so there is noting left that can be done here
                    return true;
                } catch (PersistenceException e) {
                    throw new ManagementException(String.format(FAILED_TO_DELETE, resource.getPath()), e);
                }
            } else {
                String name = value.toString();
                Resource child = resource.getChild(name);
                if (child != null) {
                    try {
                        resource.getResourceResolver().delete(child);
                        if (properties.containsKey(name)) {
                            value = properties.get(name);
                            if (value instanceof Map) {
                                properties.remove(name);
                            }
                        }
                    } catch (PersistenceException e) {
                        throw new ManagementException(String.format(FAILED_TO_DELETE_CHILD, child.getPath()), e);
                    }
                }
            }
        }
        return false;
    }

    private void applyChildProperties(Resource parent, String childName, Map childProperties) throws ManagementException {
        String childPath = (String) childProperties.get(PATH);
        Resource child = parent.getResourceResolver().getResource(childPath);
        if (child == null) {
            child = parent.getChild(childName);
        }
        // If child is missing then create it
        if (child == null) {
            final String resourceType = (String) childProperties.get(SLING_RESOURCE_TYPE);
            childProperties.remove(NAME);
            childProperties.remove(SLING_RESOURCE_TYPE);
            childProperties.remove(JCR_PRIMARY_TYPE);
            child = createNode(parent, childName, NT_UNSTRUCTURED, resourceType);
            // Now update the child with any remaining properties
            writeProperties(childProperties, child);
        } else {
            updateResourceTree(child, childProperties);
        }
    }

    private void applyListProperties(Resource resource, String childName, List list) throws ManagementException {
        if (list.isEmpty()) {
            ModifiableValueMap properties = getModifiableProperties(resource, false);
            //If the node already has a property with the same name as the empty list,
            //treat it as a deletion request
            if(properties.containsKey(childName)) {
                properties.remove(childName);
            }
            return;
        }
        final Object first = list.get(0);
        if (first instanceof Map) {
            Resource child = resource.getChild(childName);
            if (child == null) {
                child = createNode(resource, childName, NT_UNSTRUCTURED, null);
            }
            // We support either a List of Objects (Maps) or list of Strings which are stored as multi-valued String property
            // for which we have to get all the values in a list and then afterwards if such values were found update
            // them as a property
            updateObjectList(child, list);
        } else if (first instanceof String) {
            updateObjectSingleList(childName, list, resource);
        } else {
            throw new ManagementException(String.format(OBJECT_FIRST_ITEM_WITH_UNSUPPORTED_TYPE, first, (first == null ? "null" : first.getClass().getName())));
        }
    }

    private void writeProperties(Map<Object, Object> properties, Resource target) {
        final ModifiableValueMap modifiableProperties = getModifiableProperties(target, false);
        for (Entry entry : properties.entrySet()) {
            final Object key = entry.getKey();
            modifiableProperties.put(String.valueOf(key), entry.getValue());
        }
    }

    private void updateObjectSingleList(String name, List incomingList, Resource resource) throws ManagementException {
        List<String> newSingleList = new ArrayList<>();
        for (Object item : incomingList) {
            if (item instanceof String) {
                String listItem = item.toString();
                if (listItem.isEmpty()) {
                    continue;
                }
                newSingleList.add(listItem);
            } else {
                throw new ManagementException(String.format(OBJECT_ITEM_WITH_UNSUPPORTED_TYPE, item, (item == null ? "null" : item.getClass().getName())));
            }
        }
        ModifiableValueMap childProperties = getModifiableProperties(resource, false);
        childProperties.put(name, newSingleList.toArray(new String[newSingleList.size()]));
    }

    private void updateObjectList(Resource parent, List listProperties) throws ManagementException {
        Resource previousSibling = null;
        for (int i = 0; i < listProperties.size(); i++) {
            Object item = listProperties.get(i);
            if (item instanceof Map) {
                Map itemProperties = (Map) item;
                Resource temp = updateListItem(parent, itemProperties, i, previousSibling);
                previousSibling = temp != null ? temp : previousSibling;
            } else {
                throw new ManagementException(String.format(OBJECT_LIST_WITH_UNSUPPORTED_ITEM, item, (item == null ? "null" : item.getClass().getName())));
            }
        }
    }

    private Resource updateListItem(Resource parent, Map itemProperties, int position, Resource previousSibling) throws ManagementException {
        Resource answer;
        final String itemName = getString(itemProperties, NAME);
        if (isEmpty(itemName)) {
            throw new ManagementException(String.format(ITEM_NAME_MISSING, itemProperties, parent.getPath()));
        }
        Resource resourceListItem = parent.getChild(itemName);
        // Handle new item
        if (resourceListItem == null) {
            answer = createListItem(parent, itemName, itemProperties, previousSibling);
        } else {
            answer = handleExistingItem(parent, resourceListItem, itemProperties, position, previousSibling);
        }
        return answer;
    }

    private Resource createListItem(Resource parent, String itemName, Map itemProperties, Resource previousSibling) throws ManagementException {
        Object val = itemProperties.get(SLING_RESOURCE_TYPE);
        String resourceType = val == null ? null : (String) val;
        itemProperties.remove(NAME);
        itemProperties.remove(SLING_RESOURCE_TYPE);
        itemProperties.remove(JCR_PRIMARY_TYPE);
        Resource answer = createNode(parent, itemName, NT_UNSTRUCTURED, resourceType);
        // Move the child to the correct position
        if (previousSibling == null) {
            // No saved last resource item so we need to place it as the first entry
            Iterator<Resource> it = parent.getChildren().iterator();
            Resource first = it.hasNext() ? it.next() : null;
            // If there are no items then ignore it (it will be first
            if (first != null) {
                moveNode(answer, first, false, true);
            }
        } else {
            // There is a resource item -> move the new one after the last one
            moveNode(answer, previousSibling, false, false);
        }
        // Now update the child with any remaining properties
        ModifiableValueMap newChildProperties = getModifiableProperties(answer, false);
        for (Object childPropertyKey : itemProperties.keySet()) {
            newChildProperties.put(childPropertyKey + "", itemProperties.get(childPropertyKey));
        }
        return answer;
    }

    private Resource handleExistingItem(Resource parent, Resource child, Map itemProperties, int position, Resource previousSibling) throws ManagementException {
        Resource answer = null;
        boolean deleted = false;
        // Get index of the matching resource child to compare with the index in the list
        int index = getChildIndex(parent, child);
        String name = child.getName();
        if (getBoolean(itemProperties, DELETION_PROPERTY_NAME, false)) {
            try {
                logger.trace("Remove List Child: '{}' ('{}')", name, child.getPath());
                parent.getResourceResolver().delete(child);
                deleted = true;
            } catch (PersistenceException e) {
                throw new ManagementException(String.format(FAILED_TO_DELETE, previousSibling.getPath()), e);
            }
        }
        if (!deleted) {
            updateResourceTree(child, itemProperties);
            // Check order
            if (position != index) {
                if (previousSibling == null) {
                    // No saved last resource item so we need to place it as the first entry
                    Iterator<Resource> it = parent.getChildren().iterator();
                    Resource first = it.hasNext() ? it.next() : null;
                    // If there are no items then ignore it (it will be first
                    if (first != null) {
                        moveNode(child, first, false, true);
                    }
                } else {
                    // We only have to move if this wasn't already the first item due to deletion
                    boolean doMove = false;
                    Iterator<Resource> ir = parent.getChildren().iterator();
                    if (ir.hasNext() && !doMove) {
                        if (!previousSibling.getName().equals(ir.next().getName())) {
                            doMove = true;
                        }
                    }
                    if (doMove) {
                        moveNode(child, previousSibling, false, false);
                    }
                }
            }
            answer = child;
        }
        return answer;
    }

    private void copyStubs(Resource source, Resource target, String folderName, List superTypes) throws ManagementException {
        Resource appsSource = getResource(source, folderName);
        if (appsSource != null) {
            Resource appsTarget = getResource(source.getResourceResolver(), target.getPath() + SLASH + folderName);
            if (appsTarget == null) {
                appsTarget = copyFolder(appsSource, target, folderName);
                if (appsTarget == null) {
                    return;
                }
            }
            for (Resource child : appsSource.getChildren()) {
                if (isPrimaryType(child, COMPONENT_PRIMARY_TYPE) || isPrimaryType(child, OBJECT_DEFINITION_PRIMARY_TYPE)) {
                    ValueMap properties = child.getValueMap();
                    Map<String, Object> newProperties = new HashMap<>(properties);
                    String originalAppsPath = child.getPath();
                    originalAppsPath = originalAppsPath.substring(APPS_ROOT.length() + 1);
                    if (superTypes != null) {
                        superTypes.add(originalAppsPath);
                    }
                    newProperties.put(SLING_RESOURCE_SUPER_TYPE, originalAppsPath);
                    Resource existingResource = getResource(source.getResourceResolver(), appsTarget.getPath() + SLASH + child.getName());
                    //Check whether there's already a version of this resource for the new site
                    if (existingResource == null) {
                        try {
                            source.getResourceResolver().create(appsTarget, child.getName(), newProperties);
                        } catch (PersistenceException e) {
                            logger.warn("Copy of " + folderName + ": '" + child.getPath() + "' failed", e);
                        }
                    }
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
        } catch (PersistenceException e) {
            logger.warn("Copy of " + folder.getName() + ": '" + folder.getPath() + "' failed", e);
        }
        return answer;
    }

    public Resource updateResource(final ResourceResolver resourceResolver, final String path, final String jsonContent) throws ManagementException {
        if (isEmpty(jsonContent)) {
            throw new ManagementException(String.format(NO_CONTENT_PROVIDED, path));
        }

        try {
            final Resource answer = getResource(resourceResolver, path);
            if (answer == null) {
                throw new ManagementException(String.format(RESOURCE_NOT_FOUND, path));
            }
            Map content = convertToMap(jsonContent);
            //AS TODO: Check if we could add some guards here to avoid misplaced updates (JCR Primary Type / Sling Resource Type)
            updateResourceTree(answer, content);
            return answer;
        } catch (IOException e) {
            throw new ManagementException(String.format(FAILED_TO_PARSE_JSON, jsonContent));
        }
    }

    private Node createPageOrTemplate(Resource parent, String name, String templateComponent, String templatePath, String title) throws RepositoryException {
        final Node parentNode = getNode(parent);
        if (parentNode == null) {
            return null;
        }

        Node newPage = parentNode.addNode(name, PAGE_PRIMARY_TYPE);
        Node content = newPage.addNode(JCR_CONTENT);
        content.setPrimaryType(PAGE_CONTENT_TYPE);
        content.setProperty(SLING_RESOURCE_TYPE, templateComponent);
        content.setProperty(JCR_TITLE, title);
        if (templatePath != null) {
            content.setProperty(TEMPLATE, templatePath);
        }
        baseResourceHandler.updateModification(parent.getResourceResolver(), newPage);
        return newPage;
    }

    public void handleAssetDimensions(PerAsset perAsset) throws RepositoryException, IOException {
        InputStream is = perAsset.getRenditionStream((String) null);
        // Ignore images that do not have a jcr:data element aka stream
        if (is != null) {
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

    private String getPropsFromMap(Map source, String key, String defaultValue) {
        String answer = defaultValue;
        Object temp = source.get(key);
        if (temp != null) {
            String value = temp.toString();
            if (isNotBlank(value)) {
                answer = value;
            }
        }
        return answer;
    }

    public Resource copyResource(ResourceResolver resourceResolver, Resource resourceToCopy, Resource newParent, String newName, String newTitle, Resource nextSibling, boolean deep) throws ManagementException {
        if (resourceResolver == null) {
            throw new ManagementException(RESOURCE_RESOLVER_PROVIDED_TO_COPY_OPERATION_IS_NULL);
        }
        if(resourceToCopy == null) {
            throw new ManagementException(NO_RESOURCE_TO_COPY_PROVIDED);
        }
        if(newParent == null) {
            throw new ManagementException(NO_NEW_PARENT_RESOURCE_PROVIDED);
        }

        newName = getNameForCopy(resourceToCopy, newParent, newName);

        String originalPath = resourceToCopy.getPath();
        String parentPath = newParent.getPath();
        String newPath = parentPath + SLASH + newName;
        ValueMap copyProps = resourceToCopy.getValueMap();
        Resource copiedResource = null;

        if(!deep) {
            Resource pageContentResource = resourceToCopy.getChild(JCR_CONTENT);
            if(pageContentResource == null) {
                throw new ManagementException(String.format(NO_JCR_CONTENT_FOR_COPY, originalPath));
            }
            try {
                copiedResource = resourceResolver.create(newParent, newName, copyProps);
                resourceResolver.copy(pageContentResource.getPath(), copiedResource.getPath());
                return copiedResource;
            } catch (PersistenceException e) {
                throw new ManagementException(String.format(COPY_GENERIC_EXCEPTION, originalPath, newPath), e);
            }
        }
        else {
            //For deep copies, we're actually copying the resource to a temp location and then moving it to its destination
            //to get around renaming and cyclical reference issues
            Resource etc = resourceResolver.getResource(VAR);
            if(etc == null) {
                throw new ManagementException(NO_VAR_RESOURCE);
            }
            String tempResourceName = TEMP + System.currentTimeMillis() + new Random().nextInt(Integer.MAX_VALUE);
            try {
                //Create a temp location with a random (enough) path
                Resource tempResource = resourceResolver.create(etc, tempResourceName, new HashMap<>());
                //Make a resource with the new name under the temp location
                Resource tempCopy = resourceResolver.create(tempResource, newName, copyProps);
                //Copy all the children of the original resource to the temp copy
                for(Resource child : resourceToCopy.getChildren()) {
                    resourceResolver.copy(child.getPath(), tempCopy.getPath());
                }
                //Move the temp copy to the new parent location
                copiedResource = resourceResolver.move(tempCopy.getPath(), parentPath);
                //Clean up the temp location
                resourceResolver.delete(tempResource);
            } catch (PersistenceException e) {
                throw new ManagementException(String.format(COPY_GENERIC_EXCEPTION, originalPath, newPath), e);
            }
        }

        if(copiedResource == null) {
            //This should be unreachable since both deep and shallow copies should result in a resource assigned to this variable
            //or an exception but just in case
            throw new ManagementException((String.format(NO_COPIED_RESOURCE, newPath)));
        }

        //Handle retitling
        Resource copiedContent = copiedResource.getChild(JCR_CONTENT);
        if(copiedContent != null) {
            ValueMap modifiableProperties = getModifiableProperties(copiedContent);
            if(modifiableProperties.containsKey(NAME)) {
                modifiableProperties.put(NAME, newName);
            }
            if(StringUtils.isNotBlank(newTitle)) {
                modifiableProperties.put(JCR_TITLE, newTitle);
            }
        }

        //Handle reordering
        if(nextSibling != null) {
            String nextSiblingName = nextSibling.getName();
            Node parentNode = newParent.adaptTo(Node.class);
            try {
                parentNode.orderBefore(newName,nextSiblingName);
            } catch (RepositoryException e) {
                throw new ManagementException(String.format(REORDER_EXCEPTION, newPath, nextSibling.getPath()), e);
            }
        }
        return copiedResource;
    }

    private String getNameForCopy(Resource resourceToCopy, Resource newParent, String newName) throws ManagementException{
        if(StringUtils.isNotBlank(newName)) {
            if(newParent.getChild(newName) == null) {
                return newName;
            }
            else {
                throw new ManagementException(String.format(RESOURCE_NAME_COLLISION, newParent.getName(), newName));
            }
        }
        newName = resourceToCopy.getName();
        if(newParent.getChild(newName) == null) {
            return newName;
        }

        int i = 1;
        while(newParent.getChild(newName+i) != null) {
            i++;
        }
        return newName+i;
    }

    /**
     * Returns true if resource1 is an ancestor of or equal to resource 2
     *
     * @param resource1
     * @param resource2
     * @return
     */
    private boolean isAncestor(Resource resource1, Resource resource2) {
        return(resource2.getPath().startsWith(resource1.getPath()));
    }

    private String prettyPrintJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return object == null ? "null" : object.toString();
        }
    }
}