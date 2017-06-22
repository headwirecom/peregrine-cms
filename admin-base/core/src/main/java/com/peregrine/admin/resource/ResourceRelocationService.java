package com.peregrine.admin.resource;

import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.util.PerUtil;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.List;

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
        try {
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
        } catch(PersistenceException e) {
            if(e.getCause() instanceof ItemExistsException) {
                // Ignore and return the given from resource
                answer = from;
            } else {
                throw e;
            }
        }
        answer.getResourceResolver().commit();
        return answer;
    }

    @Override
    public void reorder(Resource parent, String sourceChildName, String targetChildName, boolean before) throws RepositoryException {
        if(parent == null) {
            throw new IllegalArgumentException("Parent Resource must be specified");
        }
        if(parent.getChild(sourceChildName) == null) {
            throw new IllegalArgumentException("Source Child: '" + sourceChildName + "' could not be found");
        }
        if(parent.getChild(targetChildName) == null) {
            throw new IllegalArgumentException("Target Child: '" + targetChildName + "' could not be found");
        }
        Node toNode = parent.adaptTo(Node.class);
        if(before) {
            toNode.orderBefore(sourceChildName, targetChildName);
        } else {
            Node nextNode = getNextNode(toNode, targetChildName);
            if(nextNode != null) {
                toNode.orderBefore(sourceChildName, nextNode.getName());
            }
        }
        toNode.getSession().save();
    }

    private Node getNextNode(Node parent, String childName) throws RepositoryException {
        Node answer = null;
        NodeIterator i = parent.getNodes();
        while(i.hasNext()) {
            Node child = i.nextNode();
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
        String newPath = parent.getPath() + "/" + newName;
        fromNode.getSession().move(fromPath, newPath);
        if(nextNode != null) {
            fromNodeParent.orderBefore(newName, nextNode.getName());
        }
        Resource answer = parent.getChild(newName);
        // Update the references
        for(com.peregrine.admin.replication.Reference reference : references) {
            Resource propertyResource = reference.getPropertyResource();
            ModifiableValueMap properties = PerUtil.getModifiableProperties(propertyResource);
            if(properties.containsKey(reference.getPropertyName())) {
                properties.put(reference.getPropertyName(), answer.getPath());
            }
        }
        fromNode.getSession().save();
        return answer;
    }
}
