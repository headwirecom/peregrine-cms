package com.peregrine.admin.replication;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATED_BY;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATION;
import static com.peregrine.commons.util.PerConstants.PER_REPLICATION_REF;
import static com.peregrine.commons.util.PerUtil.getModifiableProperties;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ReplicationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationUtil.class);

    private static List<String> replicationPrimaryNodeTypes;

    /**
     * @param resource Resource to be checked
     * @return True if the given Resource support Replication Mixins
     */
    public static boolean supportsReplicationProperties(Resource resource) {
        boolean answer = false;
        Node sourceNode = resource.adaptTo(Node.class);
        if(sourceNode == null) {
            LOGGER.warn("Resource: '{}' could not be adapted to a Node", resource);
        } else {
            List<String> replicationPrimaries = getReplicationPrimaryNodeTypes(sourceNode);
            try {
                if (replicationPrimaries != null) {
                    answer = replicationPrimaries.contains(sourceNode.getPrimaryNodeType().getName());
                }
                if (!answer) {
                    NodeType[] mixins = sourceNode.getMixinNodeTypes();
                    for (NodeType mixin : mixins) {
                        if (mixin.getName().equals(PER_REPLICATION)) {
                            answer = true;
                            break;
                        }
                    }
                    if (!answer) {
                        NodeType nodeType = sourceNode.getPrimaryNodeType();
                        NodeType[] superTypes = nodeType.getSupertypes();
                        for (NodeType mixin : superTypes) {
                            if (mixin.getName().equals(PER_REPLICATION)) {
                                answer = true;
                                break;
                            }
                        }
                    }
                }
            } catch (RepositoryException e) {
                LOGGER.warn("Failed to check Primary Node Type for Replication support -> ignore that", e);
            }
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
                NodeTypeIterator i = nodeTypeManager.getPrimaryNodeTypes();
                while(i.hasNext()) {
                    NodeType primary = i.nextNodeType();
                    NodeType[] superTypes = primary.getSupertypes();
                    for(NodeType mixin : superTypes) {
                        if(mixin.getName().equals(PER_REPLICATION)) {
                            replicationPrimaryNodeTypes.add(primary.getName());
                        }
                    }
                }
                LOGGER.debug("List of Replication Supporting Primary Types: '{}'", replicationPrimaryNodeTypes);
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
        if(source != null) {
            boolean replicationMixin = ReplicationUtil.supportsReplicationProperties(source);
            if(!replicationMixin) {
                replicationMixin = ensureMixin(source);
                if(replicationMixin && target != null) {
                    ensureMixin(target);
                }
            }
            LOGGER.trace("Is Replication Mixin: : {}, Source: '{}'", replicationMixin, source.getPath());
            if(replicationMixin) {
                ModifiableValueMap sourceProperties = getModifiableProperties(source, false);
                Calendar replicated = Calendar.getInstance();
                sourceProperties.put(PER_REPLICATED_BY, source.getResourceResolver().getUserID());
                sourceProperties.put(PER_REPLICATED, replicated);
                LOGGER.trace("Updated Source Replication Properties");
                if(target == null) {
                    // Target Path can be empty to remove the replication ref property
                    if(isEmpty(targetPath)) {
                        sourceProperties.remove(PER_REPLICATION_REF);
                    } else {
                        sourceProperties.put(PER_REPLICATION_REF, targetPath);
                    }
                } else {
                    try {
                        ModifiableValueMap targetProperties = getModifiableProperties(target, false);
                        String userId = source.getResourceResolver().getUserID();
                        LOGGER.trace("Replication User Id: '{}' for target: '{}'", userId, target.getPath());
                        targetProperties.put(PER_REPLICATED_BY, userId);
                        targetProperties.put(PER_REPLICATED, replicated);
                        if(JCR_CONTENT.equals(source.getName())) {
                            Resource targetParent = target.getParent();
                            Resource sourceParent = source.getParent();
                            if(sourceParent == null || targetParent == null) {
                                LOGGER.warn("A Content Node should always have a parent but we did not get one for: '{}' or '{}'", source, target);
                            } else {
                                sourceProperties.put(PER_REPLICATION_REF, targetParent.getPath());
                                targetProperties.put(PER_REPLICATION_REF, sourceParent.getPath());
                            }
                        } else {
                            sourceProperties.put(PER_REPLICATION_REF, target.getPath());
                            targetProperties.put(PER_REPLICATION_REF, source.getPath());
                        }
                        LOGGER.trace("Updated Target: '{}' Replication Properties", target.getPath());
                    } catch(IllegalArgumentException e) {
                        LOGGER.error("Failed to add replication properties", e);
                        throw e;
                    }
                }
            }
        }
    }

    private static boolean ensureMixin(Resource resource) {
        boolean answer = false;
        Node node = resource.adaptTo(Node.class);
        if(node != null) {
            try {
                if(node.canAddMixin(PER_REPLICATION)) {
                    node.addMixin(PER_REPLICATION);
                    answer = true;
                }
            } catch(RepositoryException e) {
                LOGGER.warn("Could not add Replication Mixin to node: '{}'", node);
            }
        }
        return answer;
    }
}
