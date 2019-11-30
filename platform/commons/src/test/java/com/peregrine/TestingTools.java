package com.peregrine;

import org.junit.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public final class TestingTools {

    private static final String UTIL_CLASS_CONSTRUCTOR_CONDITIONS = "%s should have a parameter-less private constructor that throws an UnsupportedOperationException";

    private TestingTools() {
        throw new UnsupportedOperationException();
    }

    public static void testUtilClassConstructor(final Class<?> clazz) {
        final String clazzName = clazz.getName();
        Object instance = null;
        try {
            final Constructor<?> constructor = clazz.getDeclaredConstructor();
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                Assert.fail("The constructor should be private in " + clazzName);
            }

            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (final NoSuchMethodException e) {
            Assert.fail("No parameter-less constructor available for " + clazzName);
        } catch (final IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // We are ok if this happens
        } catch (final UnsupportedOperationException e) {
            // We want this to happen
        }

        Assert.assertNull(String.format(UTIL_CLASS_CONSTRUCTOR_CONDITIONS, clazzName), instance);
    }
}
