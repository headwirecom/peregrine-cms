package com.peregrine.mock;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.junit.Assert.*;

public final class MockTools {

    private MockTools() {
        throw new UnsupportedOperationException();
    }

    public static String fullName(final Object owner, final String name) {
        return owner.getClass().getSimpleName() + " " + name;
    }

    public static void setPaths(final String path, final ResourceMock... resources) {
        String currentPath = path;
        for (int i = resources.length - 1; i >= 0; i--) {
            resources[i].setPath(currentPath);
            currentPath = StringUtils.substringBeforeLast(currentPath, SLASH);
        }
    }

    public static void setParentChildRelationships(final ResourceMock... resources) {
        for (int i = 0; i < resources.length - 1; i++) {
            final ResourceMock parent = resources[i];
            final ResourceMock child = resources[i + 1];
            child.setParent(parent);
            parent.addChild(child);
        }
    }

}
