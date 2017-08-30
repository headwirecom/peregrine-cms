package com.peregrine.admin.replication;

import com.peregrine.admin.replication.Replication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.peregrine.commons.util.PerUtil.isEmpty;

public abstract class AbstractionReplicationService
    implements Replication
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    String name;
    String description;

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
