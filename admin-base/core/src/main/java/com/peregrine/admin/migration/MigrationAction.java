package com.peregrine.admin.migration;

import org.apache.sling.api.resource.ResourceResolver;

public interface MigrationAction {

    MigrationDescriptor getDescriptor();

    void execute(ResourceResolver resourceResolver) throws MigrationException;
}
