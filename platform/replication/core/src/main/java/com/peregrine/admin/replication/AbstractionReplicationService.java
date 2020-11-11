package com.peregrine.admin.replication;

import com.peregrine.replication.Replication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.commons.util.PerUtil.isEmpty;

/**
 * Common Properties Base Class for Replications
 */
public abstract class AbstractionReplicationService
    implements Replication
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    String name;
    String description;

    /**
     * Initializes the Replication Service with its name and description
     * @param name Name (ID) of the Replication Service. Must be provided
     * @param description Optional Description of the Service
     * @throws IllegalArgumentException If the name is null or empty
     */
    protected void init(String name, String description) {
        if(isEmpty(name)) { throw new IllegalArgumentException("Replication Name must be provided"); }
        this.name = name;
        this.description = description == null ? "" : description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
