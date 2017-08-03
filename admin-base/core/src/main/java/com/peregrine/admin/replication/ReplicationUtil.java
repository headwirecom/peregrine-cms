package com.peregrine.admin.replication;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import java.util.ArrayList;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.PER_REPLICATION;

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
        List<String> replicationPrimaries = getReplicationPrimaryNodeTypes(sourceNode);
        try {
            if(replicationPrimaries != null) {
                return replicationPrimaries.contains(sourceNode.getPrimaryNodeType().getName());
            } else {
                NodeType[] mixins = sourceNode.getMixinNodeTypes();
                for(NodeType mixin : mixins) {
                    if(mixin.getName().equals("per:Replication")) {
                        answer = true;
                        break;
                    }
                }
                if(!answer) {
                    NodeType nodeType = sourceNode.getPrimaryNodeType();
                    NodeType[] superTypes = nodeType.getSupertypes();
                    for(NodeType mixin : superTypes) {
                        if(mixin.getName().equals(PER_REPLICATION)) {
                            answer = true;
                            break;
                        }
                    }
                }
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
}
