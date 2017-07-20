package com.peregrine.admin.resource;

import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.commons.util.PerUtil;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;

/**
 * Created by schaefa on 6/22/17.
 */
@Component(
    service = ResourceRelocation.class,
    immediate = true
)
public class ResourceRelocationService
    implements ResourceRelocation
{

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
        List<com.peregrine.admin.replication.Reference> references = null;
        if(updateReferences) {
            // Look for Referenced By list before we updating
            references = referenceLister.getReferencedByList(from);
        }
        if(from == null) {
            throw new IllegalArgumentException("From Resource must be specified");
        }
        if(toParent == null) {
            throw new IllegalArgumentException("To-Parent Resource must be specified");
        }
        ResourceResolver resourceResolver = from.getResourceResolver();
//AS The catch is removed as a move of the page into a folder that already contains a node with the given name
//AS This is for sure an erroneous situation and should be be covered up
//        try {
            answer = resourceResolver.move(from.getPath(), toParent.getPath());
            if(references != null) {
                // Update the references
                for(com.peregrine.admin.replication.Reference reference : references) {
                    Resource propertyResource = reference.getPropertyResource();
                    ModifiableValueMap properties = PerUtil.getModifiableProperties(propertyResource);
                    if(properties.containsKey(reference.getPropertyName())) {
                        properties.put(reference.getPropertyName(), answer.getPath());
                    }
                }
            }
//        } catch(PersistenceException e) {
//            if(e.getCause() instanceof ItemExistsException) {
//                // Ignore and return the given from resource
//                answer = from;
//            } else {
//                throw e;
//            }
//        }
//AS TODO: Committing here is out of the control of the caller which is not a good idea
//        answer.getResourceResolver().commit();
        return answer;
    }

    @Override
    public void reorder(Resource parent, String sourceChildName, String targetChildName, boolean before) throws RepositoryException {
        if(parent == null) {
            throw new IllegalArgumentException("Parent Resource must be specified");
        }
        if(parent.getChild(sourceChildName) == null) {
            throw new IllegalArgumentException("Source Child: '" + sourceChildName + "' could not be found in Parent: '" + parent.getPath() + "'");
        }
        if(targetChildName != null && parent.getChild(targetChildName) == null) {
            throw new IllegalArgumentException("Target Child: '" + targetChildName + "' could not be found in Parent: '" + parent.getPath() + "'");
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
//AS TODO: Committing here is out of the control of the caller which is not a good idea
//        toNode.getSession().save();
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
            throw new IllegalArgumentException("From Resource must be specified");
        }
        if(newName == null || newName.isEmpty()) {
            throw new IllegalArgumentException("New Name must be specified");
        }
        List<com.peregrine.admin.replication.Reference> references = null;
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
        String newPath = parent.getPath() + "/" + newName;
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
        for(com.peregrine.admin.replication.Reference reference : references) {
            Resource propertyResource = reference.getPropertyResource();
            properties = PerUtil.getModifiableProperties(propertyResource);
            if(properties.containsKey(reference.getPropertyName())) {
                properties.put(reference.getPropertyName(), answer.getPath());
            }
        }
//AS TODO: Committing here is out of the control of the caller which is not a good idea
//        fromNode.getSession().save();
        return answer;
    }
}
