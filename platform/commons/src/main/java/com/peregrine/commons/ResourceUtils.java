package com.peregrine.commons;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import java.util.*;

import static com.peregrine.commons.Strings.COLON;
import static com.peregrine.commons.Strings._SCORE;
import static com.peregrine.commons.util.PerConstants.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.*;

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
        if (isBlank(path)) {
            return null;
        }

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

    public static String fileNameToJcrName(final String name) {
        if (startsWith(name, _SCORE)) {
            final String nameAfterUnderscore = name.substring(1);
            if (nameAfterUnderscore.contains(_SCORE)) {
                final String prefix = substringBefore(nameAfterUnderscore, _SCORE);
                final String suffix = substringAfter(nameAfterUnderscore, _SCORE);
                if (isNotBlank(suffix)) {
                    return prefix + COLON + suffix;
                }
            }
        }

        return name;
    }

    public static String jcrNameToFileName(final String name) {
        if (contains(name, COLON)) {
            final String prefix = substringBefore(name, COLON);
            final String suffix = substringAfter(name, COLON);
            return _SCORE + prefix + _SCORE + suffix;
        }

        return name;
    }

    public static boolean exists(final Resource resource) {
        return !ResourceUtil.isNonExistingResource(resource)
                && !ResourceUtil.isStarResource(resource)
                && !ResourceUtil.isSyntheticResource(resource);
    }

    public static <L extends List<Resource>> L removeDuplicates(final L list) {
        final Set<String> paths = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            final String path = list.get(i).getPath();
            if (paths.contains(path)) {
                list.remove(i--);
            } else {
                paths.add(path);
            }
        }

        return list;
    }

    public static Resource getDeepestExistingResource(final ResourceResolver resourceResolver, final String path) {
        String parentPath = path;
        Resource resource = null;
        while (isNull(resource) && isNotBlank(parentPath)) {
            resource = resourceResolver.getResource(parentPath);
            parentPath = substringBeforeLast(parentPath, SLASH);
        }

        return resource;
    }

    public static int getLevel(final Resource resource) {
        return countMatches(resource.getPath(), SLASH) - 1;
    }

    public static Resource getAbsoluteParent(final Resource resource, int level) {
        if (getLevel(resource) < level) {
            return null;
        }

        Resource parent = resource;
        while (getLevel(parent) > level) {
            parent = parent.getParent();
        }

        return parent;
    }

    public static boolean isAncestor(final Resource resource, final Resource ancestor) {
        return resource.getPath().startsWith(ancestor.getPath() + SLASH);
    }

    public static boolean equals(final Resource x, final Resource y) {
        return x.getPath().equals(y.getPath());
    }

    public static boolean isAncestorOrEqual(final Resource resource, final Resource ancestor) {
        return isAncestor(resource, ancestor) || equals(resource, ancestor);
    }

}