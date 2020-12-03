package com.peregrine.commons;

import com.peregrine.commons.util.PerConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.peregrine.commons.Strings.COLON;
import static com.peregrine.commons.Strings._SCORE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public final class ResourceUtils {

    private static final Set<String> NONCOPYABLE_PROPERTIES = Arrays.asList(
            JcrConstants.JCR_BASEVERSION,
            JcrConstants.JCR_CREATED,
            JcrConstants.JCR_ISCHECKEDOUT,
            JcrConstants.JCR_PREDECESSORS,
            JcrConstants.JCR_UUID,
            JcrConstants.JCR_VERSIONHISTORY,
            PerConstants.JCR_CREATED_BY,
            PerConstants.PER_REPLICATED,
            PerConstants.PER_REPLICATED_BY,
            PerConstants.PER_REPLICATION_LAST_ACTION,
            PerConstants.PER_REPLICATION_REF,
            PerConstants.PER_REPLICATION_STATUS
    ).stream().collect(Collectors.toSet());

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

    public static Resource getOrCreateChild(
            final Resource parent,
            final String name,
            final String resourceTypes)
            throws PersistenceException {
        if (isBlank(name)) {
            return parent;
        }

        return getOrCreateResource(
                parent.getResourceResolver(),
                parent.getPath() + SLASH + name,
                resourceTypes
        );
    }

    public static Resource tryToCreateChildOrGetNull(
            final Resource parent,
            final String name,
            final String resourceTypes) {
        try {
            return getOrCreateChild(parent, name, resourceTypes);
        } catch (final PersistenceException e) {
            return null;
        }
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

    public static boolean isPropertyCopyable(final String name) {
        return !NONCOPYABLE_PROPERTIES.contains(name);
    }

    public static boolean isPropertyAllowedOnExistingNode(final String name) {
        return !JCR_PRIMARY_TYPE.equals(name) && isPropertyCopyable(name);
    }

    public static Map<String, Object> filterCopyableProperties(final Map<String, ?> properties) {
        return Optional.ofNullable(properties)
                .orElseGet(Collections::emptyMap)
                .entrySet()
                .stream()
                .filter(e -> isPropertyCopyable(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> filterPropertiesAllowedOnExistingNode(final Map<String, ?> properties) {
        final Map<String, Object> result = filterCopyableProperties(properties);
        result.remove(JCR_PRIMARY_TYPE);
        return result;
    }

    public static Map<String, Object> getCopyableProperties(final Resource resource) {
        return Optional.ofNullable(resource)
                .map(Resource::getValueMap)
                .map(ResourceUtils::filterCopyableProperties)
                .orElseGet(Collections::emptyMap);
    }

    public static Resource performFlatSafeCopy(
            final ResourceResolver resourceResolver,
            final Resource resource,
            final Resource targetParent,
            final String name
    ) throws PersistenceException {
        return resourceResolver.create(targetParent, name, getCopyableProperties(resource));
    }

    public static Resource performDeepSafeCopy(
            final ResourceResolver resourceResolver,
            final Resource resource,
            final Resource targetParent,
            final String name
    ) throws PersistenceException {
        final Resource result = performFlatSafeCopy(resourceResolver, resource, targetParent, name);
        for (final Resource child : resource.getChildren()) {
            performDeepSafeCopy(resourceResolver, child, result, child.getName());
        }

        return result;
    }

    public static Resource performDeepSafeCopy(final Resource resource, final Resource targetParent, final String name) throws PersistenceException {
        final ResourceResolver resourceResolver = targetParent.getResourceResolver();
        return performDeepSafeCopy(resourceResolver, resource, targetParent, name);
    }

    public static Resource performDeepSafeCopy(final Resource resource, final Resource targetParent) throws PersistenceException {
        return performDeepSafeCopy(resource, targetParent, resource.getName());
    }

}