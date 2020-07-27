package com.peregrine.adaption.impl;

import com.peregrine.adaption.PerReference;
import org.apache.sling.api.resource.Resource;

import java.util.Calendar;

public class PerReferenceImpl extends PerBaseImpl implements PerReference {

    PerReferenceImpl(Resource resource) {
        super(resource);
    }

    /**
     * @return true if the resource has been replicated
     */
    @Override
    public boolean isReplicated() {
        return false;
    }

    /**
     * @return true if the resource is has per:ReplicationStatus 'activated' and content was modified after the per:Replicated date
     */
    @Override
    public boolean isStale() {
        return false;
    }

    /**
     * @return the date for the latest replication status
     */
    @Override
    public Calendar getReplicated() {
        return null;
    }

    /**
     * @return A string property reference about the replication
     */
    @Override
    public String getReplicationRef() {
        return null;
    }
}
