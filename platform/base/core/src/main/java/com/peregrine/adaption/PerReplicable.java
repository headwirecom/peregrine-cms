package com.peregrine.adaption;

import java.util.Calendar;

/**
 * Peregrine Reference Resource.
 * Resource could be a page, asset, template, object, etc
 * It is adaptable from a Sling Resource
 *
 * Provides information about the reference such as:
 * When was it last activated/deacatived?
 * When was it last edited?
 *
 */
public interface PerReplicable extends PerBase {

    /**
     * @return true if the resource has been replicated
     */
    boolean isReplicated();

    /**
     * @return true if the resource is has per:ReplicationStatus 'activated' and content was modified after the per:Replicated date
     */
    boolean isStale();

    /**
     * @return the date for the latest replication status
     */
    Calendar getReplicated();

    /**
     * @return A string property reference about the replication
     */
    String getReplicationRef();

    /**
     * @return A string property describing the last replication action (either activated, deactivated, null)
     */
    String getLastReplicationAction();

    /**
     * Call this method to mark the value of per:ReplicationLastAction as "activated"
     */
    void setLastReplicationActionAsActivated();

    /**
     * Call this method to mark the value of per:ReplicationLastAction as "deactivated"
     */
    void setLastReplicationActionAsDeactivated();
}
