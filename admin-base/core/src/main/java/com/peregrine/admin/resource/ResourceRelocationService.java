package com.peregrine.admin.resource;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.SLASH;

import com.peregrine.commons.util.PerUtil;
import com.peregrine.replication.ReferenceLister;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
    public void reorder(Resource parent, String sourceChildName, String targetChildName, boolean before) throws RepositoryException {
        if(parent == null) {
            throw new IllegalArgumentException(PARENT_RESOURCE_MUST_BE_SPECIFIED);
        }
        if(parent.getChild(sourceChildName) == null) {
            throw new IllegalArgumentException(String.format(SOURCE_NOT_FOUND, sourceChildName, parent.getPath()));
        }
        if(targetChildName != null && parent.getChild(targetChildName) == null) {
            throw new IllegalArgumentException(String.format(TARGET_NOT_FOUND, sourceChildName, parent.getPath()));
        }
        Node toNode = parent.adaptTo(Node.class);
        if(before) {
            // No Target Child Name and before means we move it to the first place
            if(targetChildName == null) {
                Node temp = getNextNode(toNode, null);
                targetChildName = temp != null ? temp.getName() : null;
            }
            if(targetChildName != null) {
                toNode.orderBefore(sourceChildName, targetChildName);
            }
        } else {
            // No Target Child Name and after means we move it to the last place
            String nextNodeName = null;
            if(targetChildName != null) {
                Node nextNode = getNextNode(toNode, targetChildName);
                nextNodeName = nextNode != null ? nextNode.getName() : null;
            }
            toNode.orderBefore(sourceChildName, nextNodeName);
        }
    }

    private Node getNextNode(Node parent, String childName) throws RepositoryException {
        Node answer = null;
        NodeIterator i = parent.getNodes();
        while(i.hasNext()) {
            Node child = i.nextNode();
            if(childName == null) {
                // No Child Name means returns first
                answer = child;
                break;
            }
            if(child.getName().equals(childName)) {
                if(i.hasNext()) {
                    answer = i.nextNode();
                }
                break;
            }
        }
        return answer;
    }

    @Override
    public Resource rename(Resource from, String newName, boolean updateReferences) throws RepositoryException {
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
        Resource parent = from.getParent();
        Node fromNodeParent = parent.adaptTo(Node.class);
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
