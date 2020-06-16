package com.peregrine;

import com.peregrine.mock.ResourceMock;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.junit.Assert.*;

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
                fail("The constructor should be private in " + clazzName);
            }

            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (final NoSuchMethodException e) {
            fail("No parameter-less constructor available for " + clazzName);
        } catch (final IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // We are ok if this happens
        } catch (final UnsupportedOperationException e) {
            // We want this to happen
        }

        assertNull(String.format(UTIL_CLASS_CONSTRUCTOR_CONDITIONS, clazzName), instance);
    }

    public static void testUtilClassDeclaration(final Class<?> clazz) {
        final String clazzName = clazz.getName();
        assertTrue(clazzName + " should be declared final", Modifier.isFinal(clazz.getModifiers()));
    }

    public static void testUtilClass(final Class<?> clazz) {
        testUtilClassDeclaration(clazz);
        testUtilClassConstructor(clazz);
    }

}
