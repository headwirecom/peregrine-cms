package com.peregrine.admin.replication;

import org.apache.sling.api.resource.Resource;

import java.util.List;

/**
 * Created by schaefa on 5/25/17.
 */
public interface Replication {

    String getName();

    /**
     * Replicates the given resource with its JCR Content and references
     * and if deep also with its children
     *
     * @param source Resource to be replicated
     * @param deep If true the entire sub tree of the resource is replicated
     * @return List of replicated resources (the copy)
     * @throws ReplicationException If the replication failed
     */
    List<Resource> replicate(Resource source, boolean deep)
        throws ReplicationException;

    /**
     * Replicates all the given resources and only them. This means JCR Content
     * nodes must be part of the given list
     *
     * @param resourceList List of resources to be replicated
     * @return List of replicated resources (the copy)
     * @throws ReplicationException If the replication failed
     */
    List<Resource> replicate(List<Resource> resourceList)
        throws ReplicationException;

    class ReplicationException
        extends Exception
    {
        public ReplicationException(String message) {
            super(message);
        }

        public ReplicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
