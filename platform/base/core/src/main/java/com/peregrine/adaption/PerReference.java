package com.peregrine.adaption;

import java.util.Calendar;

/**
 * Peregrine Reference Resource.
 * Resource could be a page, asset, template, object, etc
 * It is adaptable from a Sling Resource
 *
 * Provides information about the reference such as:
 * When was it last activated/decatived?
 * When was it last edited?
 *
 */
public interface PerReference extends PerBase {

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

}
