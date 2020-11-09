package com.peregrine.versions;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

public interface VersioningResourceResolver extends ResourceResolver {

    String LABEL_PROPERTY = VersioningResourceResolver.class.getName() + "-label";

    Resource wrap(Resource resource);

}
