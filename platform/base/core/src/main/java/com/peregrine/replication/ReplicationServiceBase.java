package com.peregrine.replication;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.commons.util.PerUtil.isEmpty;

/**
 * Common Properties Base Class for Replications
 */
public abstract class ReplicationServiceBase
    implements Replication
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;
    private String description;

    /**
     * Initializes the Replication Service with its name and description
     * @param name Name (ID) of the Replication Service. Must be provided
     * @param description Optional Description of the Service
     * @throws IllegalArgumentException If the name is null or empty
     */
    protected void init(final String name, final String description) {
        if(isEmpty(name)) { throw new IllegalArgumentException("Replication Name must be provided"); }
        this.name = name;
        this.description = StringUtils.defaultString(description);
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
