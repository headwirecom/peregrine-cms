package com.peregrine.admin.replication;

import org.apache.sling.api.resource.Resource;

import java.util.List;

/**
 * Created by schaefa on 5/25/17.
 */
public interface ReferenceLister {

    /**
     * Provides a list of resources referenced directly or indirectly by
     * the given resource
     *
     * @param resource Resource that starts the references
     * @param deep If true it will also look for references in the resource's children
     * @return List of resources referenced which might be empty
     */
    List<Resource> getReferenceList(Resource resource, boolean deep);

    /**
     * Provides a list of resources referenced directly or indirectly by
     * the given resource and also any parents that are not available on
     * the target side
     *
     * @param resource Resource that starts the references
     * @param deep If true it will also look for references in the resource's children
     * @param source The root reference
     * @param target The target reference
     * @return List of resources referenced including all parents of the resource that are not
     *         available on the target side
     */
    List<Resource> getReferenceList(Resource resource, boolean deep, Resource source, Resource target);
}
