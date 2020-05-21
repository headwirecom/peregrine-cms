package com.peregrine.admin.resource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import com.peregrine.admin.models.Recyclable;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Defines the Interface for the Admin Resource
 * Handler which is the central service to deal with
 * Admin data and nodes.
 *
 * Created by Andreas Schaefer on 7/6/17.
 */
public interface AdminResourceHandler {

    /**
     * Create a new JCR Resource Node
     *
     * @param parent Parent JCR Resource in which the node is created in. It must be provided and exist
     * @param name Name of the Resource which cannot be null or empty
     * @param primaryType Primary Type (jcr:primaryType) of the Resource
     * @param resourceType Optional Sling Resource Type of the Resource. Is ignored if null or empty
     * @return Newly created JCR Resource
     * @throws ManagementException If the creation failed
     */
    Resource createNode(Resource parent, String name, String primaryType, String resourceType) throws ManagementException;

    /**
     * Creates a Sling Order Folder Resource Node
     *
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param parentPath Path to the Parent Resource which cannot be null or empty
     * @param name Name of the Folder which cannot be null or empty
     * @return Newly created JCR Resource Folder
     * @throws ManagementException If the creation failed
     */
    Resource createFolder(ResourceResolver resourceResolver, String parentPath, String name) throws ManagementException;

    /**
     * Creates an Peregrine Object Resource
     *
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param parentPath Path to the Parent Resource which cannot be null or empty
     * @param name Name of the Folder which cannot be null or empty
     * @param resourceType Sling Resource Type of the Object which cannot be null or empty
     * @return Newly created Peregrine Object Resource
     * @throws ManagementException If the creation failed
     */
    Resource createObject(ResourceResolver resourceResolver, String parentPath, String name, String resourceType) throws ManagementException;

    /**
     * Creates an Peregrine Object Resource
     *
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param parentPath Path to the Parent Resource which cannot be null or empty
     * @param name Name of the Folder which cannot be null or empty
     * @param templatePath Path to the page template resource (absolute path) which must exist
     * @return Newly created Peregrine Page Resource
     * @throws ManagementException If the creation failed
     */
    Resource createPage(ResourceResolver resourceResolver, String parentPath, String name, String templatePath, String title) throws ManagementException;

    /**
     * Creates an Peregrine Object Resource
     *
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param parentPath Path to the Parent Resource which cannot be null or empty
     * @param name Name of the Folder which cannot be null or empty
     * @param component Path to the component this template is based on (relative to /apps) used as Template Sling Resource Type
     *                  The component must exist
     * @return Newly created Peregrine Template Resource
     * @throws ManagementException If the creation failed
     */
    Resource createTemplate(ResourceResolver resourceResolver, String parentPath, String name, String component, String title) throws ManagementException;

    /**
     * Deletes a resource
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param path Absolute Path to the resource to be deleted and this resource must exist
     * @param primaryType Optional Sling Primary Type to check if they match before deletion and fail if they don't match
     * @return A Response for Deletion
     * @throws ManagementException If the deletion failed
     */
    DeletionResponse deleteResource(ResourceResolver resourceResolver, String path, String primaryType) throws ManagementException;

    /**
     * Creates a resource version
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param path Absolute Path of the resource to be versioned and this resource must exist
     * @return The Version which was created from the Resource
     * @throws ManagementException If creating the version failed
     */
    Version createVersion(ResourceResolver resourceResolver, String path) throws ManagementException;

    /**
     * Restore a version
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param path Absolute path of the resource having the version
     * @param versionPath Absolute frozen node path of version to be restored
     * @param force if there is a resource already in the location of the path, it will be replaced if force is true
     * @return The Resource at the supplied Version
     * @throws ManagementException If creating the version failed
     */
    Resource restoreVersion(ResourceResolver resourceResolver, String path, String versionPath, boolean force) throws ManagementException;

    /**
     * Restore a delete item based on a path where it was and the path of a version
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param path Absolute path of the resource having the version
     * @param versionPath Absolute frozen node path of version to be restored
     * @param force if there is a resource already in the location of the path, it will be replaced if force is true
     * @return The Resource at the supplied Version
     * @throws ManagementException If restoring the version failed
     */
    Resource restoreDeleted(ResourceResolver resourceResolver, String path, String versionPath, boolean force) throws ManagementException, PersistenceException, RepositoryException;

    /**
     * Pull an item from the recyclebin
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param recyclable resource from /var/recyclebin
     * @param force if there is a resource already in the location of the path, it will be replaced if force is true
     * @return The Resource at the supplied Version
     * @throws ManagementException If restoring the version failed
     */
    Resource recycleDeleted(ResourceResolver resourceResolver, Recyclable recyclable, boolean force) throws ManagementException, PersistenceException, RepositoryException;

    /**
     * Creates a recyclable item
     * @param resourceResolver Resource Resolver
     * @param resource like pages, assets and folders under pages or assets can be recycled
     * @return Null or a recyclable item
     * @throws ManagementException if there's an error
     */
    Recyclable createRecyclable(ResourceResolver resourceResolver, Resource resource) throws ManagementException;

    /**
     * Given a resource, return the path of the Site Home
     * @param resourceResolver Resource Resolver
     * @param resource any resource (pages, assets) under the site home
     * @return Null or a String
     */
    String getSiteHomePath(ResourceResolver resourceResolver, Resource resource);

    /**
     * Get a list of Recyclable items
     * @param resourceResolver Resource Resolver
     * @param path of a resource to restore (recycle)
     * @return Null or a List recyclable (restorable) items
     * @throws ManagementException if there's an error
     */
    List<Recyclable> getRecyclables(ResourceResolver resourceResolver, String path) ;


    /**
     * Updates a given resource based on the given JSon Content
     * @param resourceResolver Resource Resolver to manage resources and cannot be null
     * @param path Absolute Path to the resource to be updated and this resource must exist
     * @param jsonContent The new resource content in JSon representation. Missing properties
     *                    are untouched. To remove a node use '_opDelete' as key and then
     *                    'null' or 'true' to delete the entire (sub) resource or the name
     *                    of the property to be removed.
     * @return Resource that was updated
     * @throws ManagementException If the update failed
     */
    Resource updateResource(ResourceResolver resourceResolver, String path, String jsonContent) throws ManagementException;

    /**
     * Inserts another resource to the given resource
     * @param resource The resource the insert is done relative to. This resource must exist. This resource
     *                 is either the parent (addAsChild = true) or a sibling (addAsChild = false)
     * @param properties Resource Properties
     * @param addAsChild If true added as child of the given resource otherwise as a sibling
     * @param orderBefore - If true and added as a child then as first child
     *                    - If false and added as a child then as last child
     *                    - If true and added as sibling then before the given resource
     *                    - If false and added as sibling then after the given resource
     * @param variation Name of a default variation or null if default should be used
     * @return Newly created Resource
     * @throws ManagementException If the creation / reorder failed
     */
    Resource insertNode(Resource resource, Map<String, Object> properties, boolean addAsChild, boolean orderBefore, String variation) throws ManagementException;

    /**
     * Moves an existing resource to a new place
     * @param fromResource The resource to be moved. If must exist
     * @param toResource The resource the insert is done relative to. This resource must exist. This resource
     *                   is either the parent (addAsChild = true) or a sibling (addAsChild = false)
     * @param addAsChild If true added as child of the given toResource otherwise as its sibling
     * @param orderBefore - If true and added as a child then as first child
     *                    - If false and added as a child then as last child
     *                    - If true and added as sibling then before the given toResource
     *                    - If false and added as sibling then after the given toResource
     * @return Moved Resource
     * @throws ManagementException If the move / reorder failed
     */
    Resource moveNode(Resource fromResource, Resource toResource, boolean addAsChild, boolean orderBefore) throws ManagementException;

    /**
     * Renames a given resource node name
     * @param fromResource Resource to be renamed. It must exist
     * @param newName New Name of the Resource which cannot be null or empty and there may not a resource
     *                already exist with than name under the parent of the fromResource
     * @return Renamed Resource
     * @throws ManagementException If the rename failed
     */
    Resource rename(Resource fromResource, String newName) throws ManagementException;

    /**
     * Updates the jcr:title of a given JCR:CONTENT resource
     * @param resource Resource to get new title. It must exist and be JCR:CONTENT
     * @param jcrTitle as String
     * @return void
     */
    void updateTitle(Resource resource, String jcrTitle);

    /**
     * Updates the title of a given JCR:CONTENT resource (Assets)
     * @param resource Asset Resource to get new title. It must exist and be JCR:CONTENT
     * @param title as String
     * @return void
     */
    void updateOrCreateAssetTitle(Resource resource, String title);

    /**
     * Create an Asset Resource which the given Byte Input Stream
     * @param parent Parent Source under which the asset is created. It must exist
     * @param assetName Name of the Asset. There must not be an asset with that name already exist
     *                  in the given parent
     * @param contentType Mime Type of the Asset which must be provided
     * @param inputStream Input Stream of the Asset's Content
     * @return New created Asset Resource
     * @throws ManagementException If the creation failed
     */
    Resource createAssetFromStream(Resource parent, String assetName, String contentType, InputStream inputStream) throws ManagementException;

    /**
     * Copies the Content of a given Node to another Node
     * @param source Source Node which must exist
     * @param target Target Node which must exit
     * @param deep If true child resources are created and copied over, too
     * @return Target Node
     * @throws ManagementException If a management error occurs
     */
    Node copyNode(Node source, Node target, boolean deep) throws ManagementException;

    /**
     * Copies the Content of a given Site to another Site which includes
     * its content, assets, objects, templates and apps, felibs settings
     * @param resourceResolver Resource Resolver to obtain the resources
     * @param sitesPath Absolute Path to Sites
     * @param fromName Name of the source Site which must exist
     * @param toName Name of the target Site which cannot be null and must not exist
     * @return Resource of the Target Copy
     * @throws ManagementException If a management error occurs
     */
    Resource copySite(ResourceResolver resourceResolver, String sitesPath, String fromName, String toName) throws ManagementException;

    /**
     * Delete a peregrine cms site
     *
     * @param resourceResolver Resource Resolver to obtain the resources
     * @param sitesParentPath Absolute Path to Sites
     * @param name Name of the Site to delete
     * @return
     * @throws ManagementException
     */
    void deleteSite(ResourceResolver resourceResolver, String sitesParentPath, String name) throws ManagementException;

    /**
     * Copies a resource to a new location
     *
     * @param resourceResolver Resource Resolver to obtain the resources
     * @param resourceToCopy the resource to copy
     * @param newParent the new parent of the copied resource
     * @param newName the new node name for the copy
     * @param newTitle the new title property for the copy
     * @param nextSibling the resource to order the copy before
     * @param deep true to copy all children of the resource; false to only copy the resource and its content
     * @return the copied resource
     * @throws ManagementException
     */
    Resource copyResource(ResourceResolver resourceResolver, Resource resourceToCopy, Resource newParent, String newName, String newTitle, Resource nextSibling, boolean deep) throws ManagementException;

    /**
     * Update a peregrine cms site's components and felibs from its source site
     *
     * @param resourceResolver Resource Resolver to obtain the resources
     * @param siteName The name of the site to update
     * @throws ManagementException
     */
    void updateSite(ResourceResolver resourceResolver, String siteName) throws ManagementException;

    class ManagementException extends Exception {
        public ManagementException(String message) {
            super(message);
        }

        public ManagementException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    class DeletionResponse {
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
