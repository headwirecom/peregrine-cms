package com.peregrine.versions;

import org.apache.sling.api.resource.*;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;

import java.util.Iterator;

import static com.peregrine.commons.Strings.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

public final class VersionedResource extends ResourceWrapper {

    private final VersioningResourceResolver resolver;
    private final String path;
    private final String name;
    private final Resource version;
    private final ValueMap valueMap;

    private VersionedResource(
            final VersioningResourceResolver resolver,
            final Resource base,
            final String path,
            final Resource version
    ) {
        super(nonNull(base) ? base : version);
        this.resolver = resolver;
        this.path = path;
        name = defaultString(substringAfterLast(path, SLASH), SLASH);
        this.version = version;
        if (hasVersion()) {
            final VersionValueMap versionValueMap = new VersionValueMap(version.getValueMap());
            valueMap = new DeepReadValueMapDecorator(this, versionValueMap);
        } else {
            valueMap =  base.getValueMap();
        }
    }

    public VersionedResource(
            final VersioningResourceResolver resolver,
            final Resource base,
            final Resource version
    ) {
        this(resolver, base, base.getPath(), version);
    }

    public VersionedResource(final VersioningResourceResolver resolver, final Resource base) {
        this(resolver, base, null);
    }

    public VersionedResource(
            final VersioningResourceResolver resolver,
            final String parentPath,
            final Resource version
    ) {
        this(resolver, null, parentPath + SLASH + version.getName(), version);
    }

    public static VersionedResource unwrap(final Resource resource) {
        Resource result = resource;
        while (result instanceof ResourceWrapper && !(result instanceof VersionedResource)) {
            result = ((ResourceWrapper) result).getResource();
        }

        return result instanceof VersionedResource ? (VersionedResource) result : null;
    }

    public boolean hasResource() {
        return nonNull(getResource());
    }

    public Resource getVersion() {
        return version;
    }

    public boolean hasVersion() {
        return nonNull(version);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Resource getParent() {
        return resolver.getParent(this);
    }

    @Override
    public Resource getChild(final String relPath) {
        return resolver.getResource(this, relPath);
    }

    @Override
    public Iterator<Resource> listChildren() {
        return resolver.listChildren(this);
    }

    @Override
    public Iterable<Resource> getChildren() {
        return resolver.getChildren(this);
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return resolver;
    }

    @Override
    public boolean hasChildren() {
        return resolver.hasChildren(this);
    }

    @Override
    public ValueMap getValueMap() {
        return valueMap;
    }

    @Override
    public String getResourceType() {
        if (!hasVersion()) {
            return super.getResourceType();
        }

        if (valueMap.containsKey(SLING_RESOURCE_TYPE)) {
            return valueMap.get(SLING_RESOURCE_TYPE, String.class);
        }

        return  valueMap.get(JCR_PRIMARYTYPE, Resource.RESOURCE_TYPE_NON_EXISTING);
    }

    @Override
    public String getResourceSuperType() {
        if (hasVersion()) {
            return version.getResourceSuperType();
        }

        return super.getResourceSuperType();
    }

    @Override
    public ResourceMetadata getResourceMetadata() {
        if (hasVersion()) {
            return version.getResourceMetadata();
        }

        return super.getResourceMetadata();
    }

    @Override
    public <AdapterType> AdapterType adaptTo(final Class<AdapterType> type) {
        if (isNull(type)) {
            return null;
        }

        if (ValueMap.class.equals(type)) {
            return type.cast(getValueMap());
        }

        if (hasResource()) {
            return super.adaptTo(type);
        }

        return null;
    }

}
