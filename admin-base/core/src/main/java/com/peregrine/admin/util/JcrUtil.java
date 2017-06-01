package com.peregrine.admin.util;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schaefa on 5/26/17.
 */
public class JcrUtil {

    public static final String JCR_CONTENT = "jcr:content";
    public static final String JCR_DATA = "jcr:data";
    public static final String NT_FILE = "nt:file";
    public static final String NT_RESOURCE = "nt:resource";
    public static final String JCR_MIME_TYPE = "jcr:mimeType";
    public static final String RENDITIONS = "renditions";
    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
    public static final String SLING_FOLDER = "sling:Folder";
    private static final Logger LOG = LoggerFactory.getLogger(JcrUtil.class);

    /**
     * Provides the relative path of a resource to a given root
     * @param root Root Resource
     * @param child Child Resource
     * @return Path from the Root to the Child if Child is a sub node of the
     *         root otherwise null
     */
    public static String relativePath(Resource root, Resource child) {
        String answer = null;
        String rootPath = root.getPath();
        String childPath = child.getPath();
        if(childPath.startsWith(rootPath) && childPath.length() > rootPath.length() + 1) {
            answer = childPath.substring(rootPath.length() + 1);
        }
        return answer;
    }

    /**
     * Get the path of the parent of the given child path
     * @param childPath JCR Path of the child node we want to find the parent
     * @return JCR Path of the parent if found otherwise null
     */
    public static String getParent(String childPath) {
        String answer = null;
        if(childPath != null && !childPath.isEmpty()) {
            int index = childPath.lastIndexOf('/');
            if(index > 1 && index < childPath.length() - 1) {
                answer = childPath.substring(0, index);
            }
        }
        return answer;
    }

    /**
     * Obtains the resource with a given path
     * @param source Starting resource if the path is relative
     * @param path Path of the resource to be found. If relative it is assumed to be a child of the source
     * @return Resource if found otherwise null. If the source or path is null then this is null as well
     */
    public static Resource getResource(Resource source, String path) {
        Resource answer = null;
        if(source != null && path != null) {
            if(path.startsWith("/")) {
                answer = source.getResourceResolver().getResource(path);
            } else {
                answer = source.getChild(path);
            }
        } else {
            if(source == null) {
                LOG.warn("Source is null so path: '{}' cannot be resolved", path);
            } else {
                LOG.warn("Path is null so call is ignored");
            }
        }
        return answer;
    }

    public static Resource getResource(ResourceResolver resourceResolver, String path) {
        Resource answer = null;
        if(resourceResolver != null && path != null) {
            answer = resourceResolver.getResource(path);
        } else {
            if(resourceResolver == null) {
                LOG.warn("Resource Resolver is null so path: '{}' cannot be resolved", path);
            } else {
                LOG.warn("Path is null so call with RR is ignored");
            }
        }
        return answer;
    }

    /**
     * Gets the Value Map of the Resource JCR Content
     *
     * @param resource Resource to look for the properties. If this is not the jcr:content node
     *                 then it will try to obtain the jcr:content node automatically
     * @return The Value Map of the JCR Content node if found otherwise the resource's value map. If
     *         resource is null then it will return null
     */
    public static ValueMap getProperties(Resource resource) {
        return getProperties(resource, true);
    }

    /**
     * Gets the Value Map of the Resource or its JCR Content
     *
     * @param resource Resource to look for the properties
     * @param goToJcrContent If true then if the given resource is not the JCR Content it will look that one up
     * @return The Value Map of the Resource or JCR Content node
     */
    public static ValueMap getProperties(Resource resource, boolean goToJcrContent) {
        ValueMap answer = null;
        Resource jcrContent = resource;
        if(goToJcrContent && !jcrContent.getName().equals(JCR_CONTENT)) {
            jcrContent = jcrContent.getChild(JCR_CONTENT);
        }
        if(jcrContent != null) {
            answer = jcrContent.getValueMap();
        }
        return answer;
    }

    /**
     * Gets the Modifiable Value Map of the Resource JCR Content
     *
     * @param resource Resource to look for the properties. If this is not the jcr:content node
     *                 then it will try to obtain the jcr:content node automatically
     * @return The Modifiable Value Map of the JCR Content node if found otherwise the resource's value map. If
     *         resource is null then it will return null
     */
    public static ModifiableValueMap getModifiableProperties(Resource resource) {
        return getModifiableProperties(resource, true);
    }

    /**
     * Gets the Modifiable Value Map of the Resource or its JCR Content
     *
     * @param resource Resource to look for the properties
     * @param goToJcrContent If true then if the given resource is not the JCR Content it will look that one up
     * @return The Modifiable Value Map of the Resource or JCR Content node
     */
    public static ModifiableValueMap getModifiableProperties(Resource resource, boolean goToJcrContent) {
        ModifiableValueMap answer = null;
        Resource jcrContent = resource;
        if(goToJcrContent && !jcrContent.getName().equals(JCR_CONTENT)) {
            jcrContent = jcrContent.getChild(JCR_CONTENT);
        }
        if(jcrContent != null) {
            answer = jcrContent.adaptTo(ModifiableValueMap.class);
        }
        return answer;
    }

    /**
     * Lists all the parent nodes between the child and the root if the root is one
     * of the child's parents. Both child and root and not included in the returned list
     * @param root
     * @param child
     * @return
     */
    public static List<Resource> listParents(Resource root, Resource child) {
        List<Resource> answer = new ArrayList<>();
        Resource parent = child.getParent();
        while(true) {
            if(parent == null) {
                // No parent matches 'source' so we ignore it
                answer.clear();
                break;
            }
            if(parent.getPath().equals(root.getPath())) {
                // Hit the source -> done with loop
                break;
            }
            answer.add(parent);
            parent = parent.getParent();
        }
        return answer;
    }

    public static void listMissingResources(Resource startingResource, List<Resource> response, Resource source, Resource target, boolean deep) {
        if(startingResource != null && source != null && target != null && response != null) {
            String relativePath = relativePath(source, startingResource);
            Resource targetResource = target.getChild(relativePath);
            if(targetResource == null && !containsResource(response, startingResource)) {
                response.add(startingResource);
            }
            for(Resource child : startingResource.getChildren()) {
                if(child.getName().equals(JCR_CONTENT)) {
                    listMissingResources(child, response, source, target, true);
                } else if(deep) {
                    listMissingResources(child, response, source, target, true);
                }
            }
        }
    }

    public static boolean containsResource(List<Resource> resourceList, Resource resource) {
        for(Resource item: resourceList) {
            if(item.getPath().equals(resource.getPath())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lists all the missing parents compared to the parents on the source
     *
     * @param startingResource Child Resource
     * @param response List of resources to which the missing parents are added to. Cannot be null
     * @param source Root of the Child
     * @param target Root of the Target. Parents of the child are added if there is not matching parent on the target.
     *               Matching means the resource with the same relative path.
     */
    public static void listMissingParents(Resource startingResource, List<Resource> response, Resource source, Resource target) {
        if(startingResource != null && source != null && target != null && response != null) {
            List<Resource> parents = listParents(source, startingResource);
            // Now we go through all parents, check if the matching parent exists on the target
            // side and if not there add it to the list
            for(Resource sourceParent : parents) {
                String relativePath = JcrUtil.relativePath(source, sourceParent);
                Resource targetResource = target.getChild(relativePath);
                if(targetResource == null && !containsResource(response, sourceParent)) {
                    response.add(sourceParent);
                }
            }
        }
    }
}
