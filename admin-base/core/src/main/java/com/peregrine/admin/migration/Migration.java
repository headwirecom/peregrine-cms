package com.peregrine.admin.migration;

import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;

/**
 * Interface for Migration Service
 *
 * Created by Andreas Schaefer on 8/27/2019
 */
public interface Migration {

    /** @return List of available Migration Actions **/
    List<MigrationDescriptor> getMigrationActions();

    MigrationResponse execute(String migrationActionName, ResourceResolver resourceResolver);
}
