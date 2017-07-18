package com.peregrine.admin.resource;

import com.peregrine.admin.transform.ImageContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by schaefa on 7/6/17.
 */
public interface ResourceManagement {

    public Resource createNode(Resource parent, String name, String primaryType, String resourceType) throws ManagementException;

    public Resource createFolder(ResourceResolver resourceResolver, String parentPath, String name) throws ManagementException;

    public Resource createObject(ResourceResolver resourceResolver, String parentPath, String name, String resourceType) throws ManagementException;

    public Resource createPage(ResourceResolver resourceResolver, String parentPath, String name, String templatePath) throws ManagementException;

    public Resource createTemplate(ResourceResolver resourceResolver, String parentPath, String name, String component) throws ManagementException;

    public DeletionResponse deleteResource(ResourceResolver resourceResolver, String path) throws ManagementException;

    public DeletionResponse deleteResource(ResourceResolver resourceResolver, String path, String primaryType) throws ManagementException;

    public Resource updateResource(ResourceResolver resourceResolver, String path, String jsonContent) throws ManagementException;

    public Resource insertNode(Resource resource, Map<String, Object> properties, boolean addAsChild, boolean orderBefore) throws ManagementException;

    public Resource moveNode(Resource fromResource, Resource toResource, boolean addAsChild, boolean orderBefore) throws ManagementException;

    public Resource rename(Resource fromResource, String newName) throws ManagementException;

    public Resource createAssetFromStream(Resource parent, String assetName, String contentType, InputStream inputStream) throws ManagementException;

    public ImageContext createRendition(Resource resource, String renditionName, String sourceMimeType) throws ManagementException;

    public void updateModification(ResourceResolver resourceResolver, Node node);

    public void updateModification(Resource resource);

    public class ManagementException
        extends Exception
    {
        public ManagementException(String message) {
            super(message);
        }

        public ManagementException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public class DeletionResponse {
        private String name;
        private String path;
        private String parentPath;
        private String type;

        public String getName() {
            return name;
        }

        public DeletionResponse setName(String name) {
            this.name = name;
            return this;
        }

        public String getPath() {
            return path;
        }

        public DeletionResponse setPath(String path) {
            this.path = path;
            return this;
        }

        public String getParentPath() {
            return parentPath;
        }

        public DeletionResponse setParentPath(String parentPath) {
            this.parentPath = parentPath;
            return this;
        }

        public String getType() {
            return type;
        }

        public DeletionResponse setType(String type) {
            this.type = type;
            return this;
        }
    }
}
