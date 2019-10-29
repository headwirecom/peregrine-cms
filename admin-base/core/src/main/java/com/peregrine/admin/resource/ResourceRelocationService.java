package com.peregrine.admin.resource;

import com.peregrine.admin.resource.AdminResourceHandler.ManagementException;
import com.peregrine.commons.util.PerUtil;
import com.peregrine.replication.ReferenceLister;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.SLASH;

/**
 * Created by Andreas Schaefer on 6/22/17.
 */
@Component(
    service = ResourceRelocation.class,
    immediate = true
)
public class ResourceRelocationService
    implements ResourceRelocation
{

    public static final String FROM_RESOURCE_MUST_BE_SPECIFIED = "From Resource must be specified";
    public static final String NEW_NAME_MUST_BE_SPECIFIED = "New Name must be specified";
    public static final String PARENT_RESOURCE_MUST_BE_SPECIFIED = "Parent Resource must be specified";
    public static final String FROM_RESOURCE_MUST_BE_SPECIFIED1 = "From Resource must be specified";
    public static final String TO_PARENT_RESOURCE_MUST_BE_SPECIFIED = "To-Parent Resource must be specified";

    public static final String SOURCE_NOT_FOUND = "Source Child: '%s' could not be found in Parent: '%s'";
    public static final String TARGET_NOT_FOUND = "Target Child: '%s' could not be found in Parent: '%s'";

    private static final String FROM_NOT_A_NODE = "Form Resource: '%s' could not be adapted to a Node";
    private static final String RESOURCE_HAS_NO_PARENT = "Resource: '%s' has no parent";
    private static final String FROM_PARENT_NOT_A_NODE = "From Parent: '%s' could not be adapted to a Node";
    private static final String AFTER_RESOURCE_NOT_FOUND = "After rename new resource: '%s' could not be found";

    @Reference
    private ReferenceLister referenceLister;

    @Override
    public boolean isChildOfParent(Resource child, Resource parent) {
        Resource childParent = child != null ? child.getParent() : null;
        return childParent != null && parent != null &&
            parent.getPath().equals(childParent.getPath());
    }

    @Override
    public boolean hasSameParent(Resource first, Resource second) {
        return second != null &&
            isChildOfParent(first, second.getParent());
    }

    @Override
    public Resource moveToNewParent(Resource from, Resource toParent, boolean updateReferences) throws PersistenceException {
        Resource answer = null;
        List<com.peregrine.replication.Reference> references = null;
        if(updateReferences) {
            // Look for Referenced By list before we updating
            references = referenceLister.getReferencedByList(from);
        }
        if(from == null) {
            throw new IllegalArgumentException(FROM_RESOURCE_MUST_BE_SPECIFIED1);
        }
        if(toParent == null) {
            throw new IllegalArgumentException(TO_PARENT_RESOURCE_MUST_BE_SPECIFIED);
        }
        ResourceResolver resourceResolver = from.getResourceResolver();
        answer = resourceResolver.move(from.getPath(), toParent.getPath());
        if(references != null) {
            // Update the references
            for(com.peregrine.replication.Reference reference : references) {
                Resource propertyResource = reference.getPropertyResource();
                ModifiableValueMap properties = PerUtil.getModifiableProperties(propertyResource);
                if(properties.containsKey(reference.getPropertyName())) {
                    properties.put(reference.getPropertyName(), answer.getPath());
                }
            }
        }
        return answer;
    }

    @Override
    public void reorder(@NotNull Resource parent, @NotNull String sourceChildName, @Nullable String targetChildName, boolean before) throws RepositoryException, ManagementException {
        if(parent.getChild(sourceChildName) == null) {
            throw new IllegalArgumentException(String.format(SOURCE_NOT_FOUND, sourceChildName, parent.getPath()));
        }
        if(targetChildName != null && parent.getChild(targetChildName) == null) {
            throw new IllegalArgumentException(String.format(TARGET_NOT_FOUND, sourceChildName, parent.getPath()));
        }
        Node toNode = parent.adaptTo(Node.class);
        if(toNode == null) { throw new ManagementException("Parent: '" + parent.getPath() + "' could not be adapted to a Node"); }
        if(before) {
            reorderBefore(targetChildName, toNode, sourceChildName);
        } else {
            reorderAfter(targetChildName, toNode, sourceChildName);
        }
    }

    private void reorderBefore(String targetChildName, Node toNode, String sourceChildName) throws RepositoryException {
        // No Target Child Name and before means we move it to the first place
        if(targetChildName == null) {
            Node temp = getNextNode(toNode, null);
            targetChildName = temp != null ? temp.getName() : null;
        }
        if(targetChildName != null) {
            toNode.orderBefore(sourceChildName, targetChildName);
        }
    }

    private void reorderAfter(String targetChildName, Node toNode, String sourceChildName) throws RepositoryException {
        // No Target Child Name and after means we move it to the last place
        String nextNodeName = null;
        if(targetChildName != null) {
            Node nextNode = getNextNode(toNode, targetChildName);
            nextNodeName = nextNode != null ? nextNode.getName() : null;
        }
        toNode.orderBefore(sourceChildName, nextNodeName);
    }

    private @Nullable Node getNextNode(@NotNull Node parent, @Nullable String childName) throws RepositoryException {
        Node answer = null;
        NodeIterator i = parent.getNodes();
        if(childName == null) {
            answer = i.hasNext() ? i.nextNode() : null;
        } else {
            // Look for the Node with the given Child Name and if found take the next if present
            while (i.hasNext()) {
                Node child = i.nextNode();
                if (child.getName().equals(childName)) {
                    answer = i.hasNext() ? i.nextNode() : null;
                    break;
                }
            }
        }
        return answer;
    }

    @Override
    public Resource rename(Resource from, String newName, boolean updateReferences) throws RepositoryException, ManagementException {
        if(from == null) {
            throw new IllegalArgumentException(FROM_RESOURCE_MUST_BE_SPECIFIED);
        }
        if(newName == null || newName.isEmpty()) {
            throw new IllegalArgumentException(NEW_NAME_MUST_BE_SPECIFIED);
        }
        List<com.peregrine.replication.Reference> references = null;
        if(updateReferences) {
            // Look for Referenced By list before we updating
            references = referenceLister.getReferencedByList(from);
        }
        Node fromNode = from.adaptTo(Node.class);
        if(fromNode == null) { throw new ManagementException(String.format(FROM_NOT_A_NODE, from.getPath())); }
        Resource parent = from.getParent();
        if(parent == null) { throw new ManagementException(String.format(RESOURCE_HAS_NO_PARENT, from.getPath())); }
        Node fromNodeParent = parent.adaptTo(Node.class);
        if(fromNodeParent == null) { throw new ManagementException(String.format(FROM_PARENT_NOT_A_NODE, parent.getPath())); }
        Node nextNode = getNextNode(fromNodeParent, from.getName());
        String fromPath = from.getPath();
        String fromName = from.getName();
        String newPath = parent.getPath() + SLASH + newName;
        // If the Node has a Content with a JCR Title which matches the original name then change that as well
        ModifiableValueMap properties = PerUtil.getModifiableProperties(from);
        if(properties != null) {
            String title = properties.get(JCR_TITLE, String.class);
            if(fromName.equals(title)) {
                properties.put(JCR_TITLE, newName);
            }
        }
        fromNode.getSession().move(fromPath, newPath);
        if(nextNode != null) {
            fromNodeParent.orderBefore(newName, nextNode.getName());
        }
        Resource answer = parent.getChild(newName);
        if(answer == null) { throw new ManagementException(String.format(AFTER_RESOURCE_NOT_FOUND, newName)); }
        // Update the references
        for(com.peregrine.replication.Reference reference : references) {
            Resource propertyResource = reference.getPropertyResource();
            if(propertyResource.getChild(JCR_CONTENT) != null) {
                properties = PerUtil.getModifiableProperties(propertyResource, true);
            }
            else {
                properties = PerUtil.getModifiableProperties(propertyResource, false);
            }
            if(properties != null && properties.containsKey(reference.getPropertyName())) {
                properties.put(reference.getPropertyName(), answer.getPath());
            }
        }
        return answer;
    }
}
