package com.peregrine.commons;

import com.peregrine.TestingTools;
import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringsTest {

    private static final String X = "x";
    private static final String Y = "y";

    @Test
    public void utilClass() {
        TestingTools.testUtilClass(Strings.class);
    }

    @Test
    public void firstNotBlank() {
        assertNull(Strings.firstNotBlank());
        assertNull(Strings.firstNotBlank(EMPTY));
        assertNull(Strings.firstNotBlank((String)null));
        assertNull(Strings.firstNotBlank(" "));
        assertNull(Strings.firstNotBlank("\t"));
        assertNull(Strings.firstNotBlank("\n"));
        assertNull(Strings.firstNotBlank(EMPTY, "\n", "\t", "\n", null, " "));
        assertEquals(X, Strings.firstNotBlank(X));
        assertEquals(X, Strings.firstNotBlank(X, Y));
        assertEquals(X, Strings.firstNotBlank(X, EMPTY));
        assertEquals(X, Strings.firstNotBlank(X, " "));
        assertEquals(X, Strings.firstNotBlank(X));
    }
}