package com.peregrine.sitemap;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.HashMap;
import java.util.Map;

import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static java.util.Objects.isNull;

public final class ResourceUtils {

    private ResourceUtils() {
        throw new UnsupportedOperationException();
    }

    public static Resource getFirstExistingAncestorOnPath(final ResourceResolver resourceResolver, final String path) {
        String existingPath = path;
        Resource resource = null;
        while (StringUtils.isNotBlank(existingPath) &&
                (resource = resourceResolver.getResource(existingPath)) == null) {
            existingPath = StringUtils.substringBeforeLast(existingPath, SLASH);
        }

        if (isNull(resource)) {
            return resourceResolver.getResource(SLASH);
        }

        return resource;
    }

    public static Resource getOrCreateResource(
            final ResourceResolver resourceResolver,
            final String path,
            final String resourceTypes)
            throws PersistenceException {
        Resource resource = getFirstExistingAncestorOnPath(resourceResolver, path);
        final String missingPath;
        if (isNull(resource)) {
            missingPath = path;
        } else {
            missingPath = StringUtils.substringAfter(path, resource.getPath());
        }

        final String[] missing = StringUtils.split(missingPath, SLASH);
        final Map<String, Object> properties = new HashMap<>();
        properties.put(JCR_PRIMARY_TYPE, resourceTypes);
        for (final String name : missing) {
            resource = resourceResolver.create(resource, name, properties);
        }

        return resourceResolver.getResource(path);
    }

}
