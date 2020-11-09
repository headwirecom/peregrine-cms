package com.peregrine.versions;

import com.google.common.collect.ImmutableBiMap;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.jackrabbit.JcrConstants.*;

public final class VersionValueMap extends ValueMapDecorator {

    private static final ImmutableBiMap<String, String> FROZEN_NAMES_TO_PROP_NAMES = ImmutableBiMap.of(
            JCR_FROZENPRIMARYTYPE, JCR_PRIMARYTYPE,
            JCR_FROZENUUID, JCR_UUID,
            JCR_FROZENMIXINTYPES, JCR_MIXINTYPES);
    private static final ImmutableBiMap<String, String> PROP_NAMES_TO_FROZEN_NAMES = FROZEN_NAMES_TO_PROP_NAMES.inverse();

    private final ValueMap base;

    public VersionValueMap(final ValueMap base) {
        super(base);
        this.base = base;
    }

    public static String freezePropName(final Object name) {
        return Optional.ofNullable(name)
                .map(Object::toString)
                .map(x -> PROP_NAMES_TO_FROZEN_NAMES.getOrDefault(x, x))
                .orElse(null);
    }

    public static boolean isFrozenPropName(final Object name) {
        return FROZEN_NAMES_TO_PROP_NAMES.containsKey(name);
    }

    public static boolean isNotFrozenPropName(final Object name) {
        return !isFrozenPropName(name);
    }

    public static String unfreezePropName(final Object name) {
        return Optional.ofNullable(name)
                .map(Object::toString)
                .map(x -> FROZEN_NAMES_TO_PROP_NAMES.getOrDefault(x, x))
                .orElse(null);
    }

    public static boolean isUnfrozenPropName(final Object name) {
        return PROP_NAMES_TO_FROZEN_NAMES.containsKey(name);
    }

    public static boolean isNotUnfrozenPropName(final Object name) {
        return !isUnfrozenPropName(name);
    }

    @Override
    public Object get(final Object name) {
        return Optional.ofNullable(name)
                .map(VersionValueMap::freezePropName)
                .map(super::get)
                .orElse(null);
    }

    @Override
    public <T> T get(final String name, final Class<T> type) {
        return Optional.ofNullable(name)
                .map(VersionValueMap::freezePropName)
                .map(x -> super.get(x, type))
                .orElse(null);
    }

    @Override
    public <T> T get(final String name, final T defaultValue) {
        return Optional.ofNullable(name)
                .map(VersionValueMap::freezePropName)
                .map(x -> super.get(x, defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public boolean containsKey(final Object name) {
        return Optional.ofNullable(name)
                .map(VersionValueMap::freezePropName)
                .map(super::containsKey)
                .orElse(false);
    }

    @Override
    public Set<String> keySet() {
        return super.keySet().stream()
                .filter(VersionValueMap::isNotUnfrozenPropName)
                .map(VersionValueMap::unfreezePropName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return super.entrySet().stream()
                .filter(e -> VersionValueMap.isNotUnfrozenPropName(e.getKey()))
                .map(e -> Map.entry(unfreezePropName(e.getKey()), e.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public int size() {
        return keySet().size();
    }

    @Override
    public Collection<Object> values() {
        return entrySet().stream()
                .map(Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        throwUnmodifiableException();
    }

    @Override
    public Object put(final String key, final Object value) {
        return throwUnmodifiableException();
    }

    @Override
    public Object remove(final Object key) {
        return throwUnmodifiableException();
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        return throwUnmodifiableException();
    }

    private <T> T throwUnmodifiableException() {
        throw new UnsupportedOperationException("Value Map not modifiable.");
    }

    @Override
    public String toString() {
        return entrySet().toString();
    }

}
