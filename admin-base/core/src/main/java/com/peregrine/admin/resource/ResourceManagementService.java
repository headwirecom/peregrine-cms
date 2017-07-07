package com.peregrine.admin.resource;

import com.peregrine.admin.servlets.AbstractBaseServlet.ErrorResponse;
import com.peregrine.util.PerUtil;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_TITLE;
import static com.peregrine.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.SLING_ORDERED_FOLDER;
import static com.peregrine.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.util.PerUtil.TEMPLATE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

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
