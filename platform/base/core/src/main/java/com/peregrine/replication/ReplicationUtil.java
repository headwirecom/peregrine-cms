package com.peregrine.replication;

import org.apache.sling.api.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;

import java.util.*;

import static com.peregrine.commons.util.PerConstants.*;
import static com.peregrine.commons.util.PerUtil.*;
import static java.util.Objects.isNull;

public class ReplicationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationUtil.class);
    private static final String SQL2_STATEMENT = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([%s]) and CONTAINS(s.*, '%s')";
    private static List<String> replicationPrimaryNodeTypes;

    /**
     * Overrides the Replication Primary Node Types which are by default taken from
     * the Sub Types of the Replication Mixin
     *
     * @param replicationPrimaryNodeTypes Replication Node Types to set which must not be null or empty
     */
    public static void setReplicationPrimaryNodeTypes(List<String> replicationPrimaryNodeTypes) {
        if(replicationPrimaryNodeTypes == null) { throw new IllegalArgumentException("Replication Primary Node Types must be provided"); }
        if(replicationPrimaryNodeTypes.isEmpty()) { throw new IllegalArgumentException("Replication Primary Node Types cannot be empty"); }
        ReplicationUtil.replicationPrimaryNodeTypes = replicationPrimaryNodeTypes;
    }

    /**
     * @param resource Resource to be checked
     * @return True if the given Resource supports Replication Mixin
     */
    public static boolean supportsReplicationProperties(Resource resource) {
        boolean answer = false;
        Node sourceNode = resource.adaptTo(Node.class);
        List<String> replicationPrimaries = getReplicationPrimaryNodeTypes(sourceNode);
        try {
            if(replicationPrimaries != null) {
                answer = replicationPrimaries.contains(sourceNode.getPrimaryNodeType().getName());
            }
        } catch(RepositoryException e) {
            LOGGER.warn("Failed to check Primary Node Type for Replication support -> ignore that", e);
        }
        return answer;
    }

    /**
     * Try to obtain all primary node types that support Replication Mixin
     * @return List of node types support Replication or null if we failed to obtain that list
     **/
    private static List<String> getReplicationPrimaryNodeTypes(Node node) {
        List<String> answer = replicationPrimaryNodeTypes;
        if(answer == null) {
            try {
                answer = replicationPrimaryNodeTypes = new ArrayList<>();
                NodeTypeManager nodeTypeManager = node.getSession().getWorkspace().getNodeTypeManager();
                NodeType replicationMixin = nodeTypeManager.getNodeType(PER_REPLICATION);
                NodeTypeIterator i = replicationMixin.getSubtypes();
                while (i.hasNext()) {
                    NodeType primary = i.nextNodeType();
                    replicationPrimaryNodeTypes.add(primary.getName());
                }
                LOGGER.debug("List of Replication Supporting Primary Types: '{}'", replicationPrimaryNodeTypes);
            } catch(NoSuchNodeTypeException e) {
                LOGGER.trace("Replication Node type not found", e);
            } catch(RepositoryException e) {
                LOGGER.trace("Failed to handle Node Type Manager", e);
                // Failed to obtain Replication Node Type -> handle them manually
            }
        }

        return answer;
    }

    /**
     * Set the replications properties if the source supports Replication Mixin
     *
     * @param source Source Resource to be updated. If null or the resource does not support Replication Mixin this call does nothing.
     * @param targetPath Replication Ref target path. If the TARGET is NULL this will set the source's Replication Ref property
     *                   or removes it if empty or null
     * @param target Target Resource to be updated with same date and reference back to the source in the replication ref. If null will be ignored
     */
    public static void updateReplicationProperties(Resource source, String targetPath, Resource target) {
        if (isNull(source) || !supportsReplicationProperties(source) || !ensureMixin(source)) {
            return;
        }

        final ModifiableValueMap sourceProperties = getModifiableProperties(source, false);
        if (isNull(sourceProperties)) {
            return;
        }

        final ResourceResolver resourceResolver = source.getResourceResolver();
        final String userId = resourceResolver.getUserID();
        sourceProperties.put(PER_REPLICATED_BY, userId);
        final Calendar replicated = Calendar.getInstance();
        sourceProperties.put(PER_REPLICATED, replicated);
        LOGGER.trace("Updated Source Replication Properties");
        if (isNull(target)) {
            if (isEmpty(targetPath)) {
                // If Target Path is empty remove the replication ref property
                sourceProperties.remove(PER_REPLICATION_REF);
                sourceProperties.put(PER_REPLICATION_LAST_ACTION, DEACTIVATED);
            } else {
                sourceProperties.put(PER_REPLICATION_REF, targetPath);
                sourceProperties.put(PER_REPLICATION_LAST_ACTION, ACTIVATED);
            }
        } else if (ensureMixin(target)) {
            sourceProperties.put(PER_REPLICATION_LAST_ACTION, ACTIVATED);
            final ModifiableValueMap targetProperties = getModifiableProperties(target, false);
            LOGGER.trace("Replication User Id: '{}' for target: '{}'", userId, target.getPath());
            targetProperties.put(PER_REPLICATED_BY, userId);
            targetProperties.put(PER_REPLICATED, replicated);
            if (isJcrContent(source)) {
                // For jcr:content nodes set the replication ref to its parent
                sourceProperties.put(PER_REPLICATION_REF, target.getParent().getPath());
                targetProperties.put(PER_REPLICATION_REF, source.getParent().getPath());
            } else {
                sourceProperties.put(PER_REPLICATION_REF, target.getPath());
                targetProperties.put(PER_REPLICATION_REF, source.getPath());
            }

            LOGGER.trace("Updated Target: '{}' Replication Properties", target.getPath());
        }

        refreshAndCommit(resourceResolver);
    }

    public static void refreshAndCommit(final ResourceResolver resourceResolver) {
        resourceResolver.refresh();
        try {
            resourceResolver.commit();
        } catch (final PersistenceException e) {
            resourceResolver.revert();
            LOGGER.error("Could not commit changes", e);
        }
    }

    /**
     * Query a search term under a resource
     */
    public static Iterator<Resource> queryContainsStringUnderResource(Resource resource, String searchTerm){
        ResourceResolver resourceResolver = resource.getResourceResolver();
        String statement = String.format(SQL2_STATEMENT, resource.getPath(), searchTerm);
        return resourceResolver.findResources(statement, Query.JCR_SQL2);
    }

    /**
     * Adds the Replication Mixin to a given resource if not already
     * there and if not part of it as super type
     *
     * @param resource Resource that should have the mixin
     * @return True if the mixin was added, false if node could not be adapted, mixin could not
     *         be added or if adding failed
     */
    private static boolean ensureMixin(Resource resource) {
        Node node = resource.adaptTo(Node.class);
        if(node != null) {
            try {
                if(node.canAddMixin(PER_REPLICATION)) {
                    node.addMixin(PER_REPLICATION);
                    return true;
                } else {
                    LOGGER.warn("Could not set Replication Mixin on resource: '{}'", resource);
                }
            } catch(RepositoryException e) {
                LOGGER.warn("Could not add Replication Mixin to node: '{}'", node);
            }
        }

        return false;
    }

    public static boolean isReplicated(final Resource resource) {
        return Optional.ofNullable(resource)
                .map(r -> r.adaptTo(PerReplicable.class))
                .map(PerReplicable::isReplicated)
                .orElse(false);
    }

    public static boolean isAnyDescendantReplicated(final Resource resource) {
        for (final Resource child : resource.getChildren()) {
            if (!isJcrContent(child) &&
                    (isReplicated(child) || isAnyDescendantReplicated(child))) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSelfOrAnyDescendantReplicated(final Resource resource) {
        return isReplicated(resource) || isAnyDescendantReplicated(resource);
    }

}