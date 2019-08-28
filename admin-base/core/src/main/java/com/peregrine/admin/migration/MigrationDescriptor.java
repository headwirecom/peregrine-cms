package com.peregrine.admin.migration;

/**
 * A Descriptor of a Migration Action
 *
 * Created by Andreas Schaefer on 8/27/2019
 */
public class MigrationDescriptor {

    private String name;
    private String description;
    private String lastUpdated;

    /**
     * Creates an Migration Descriptor
     * @param name Name of the Migration. It must be provided and cannot have spaces. Also make the name simple as it is the Action Id.
     * @param description Description of the Migration Action
     * @param lastUpdated Date of the last update
     */
    public MigrationDescriptor(String name, String description, String lastUpdated) {
        this.name = name;
        this.description = description;
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }
}
