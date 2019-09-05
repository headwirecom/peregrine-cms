package com.peregrine.optional;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Tests for handling Optional Lambdas to deal with Nulls etc
 *
 * TODO: move this to a better place like commons
 */
public class OptionalTest {

    @Test
    public void testNullStringOptional() {
        String nullString = null;
        String defaultString = "myDefault";
        String outcome = Optional.ofNullable(nullString)
            .orElse(defaultString);
        assertEquals("Wrong Optional Value for Null", defaultString, outcome);
    }

    @Test
    public void testNotNullStringOptional() {
        String notNullString = "mytest";
        String defaultString = "myDefault";
        String outcome = Optional.ofNullable(notNullString)
            .orElse(defaultString);
        assertEquals("Wrong Optional Value for Null", notNullString, outcome);
    }

    @Test
    public void testNotNullStringWithFilterMethodOptional() {
        String notNullFilteredOutString = "mytest2";
        String notNullNotFilteredOutString = "test";
        String defaultString = "myDefault";
        String outcome = Optional.ofNullable(notNullFilteredOutString)
            .filter(s -> filterMethod(s))
            .orElse(defaultString);
        assertEquals("Wrong Optional Value for Null", defaultString, outcome);
        outcome = Optional.ofNullable(notNullNotFilteredOutString)
            .filter(s -> filterMethod(s))
            .orElse(defaultString);
        assertEquals("Wrong Optional Value for Null", notNullNotFilteredOutString, outcome);
    }

    private boolean filterMethod(String text) {
        return "test".equals(text);
    }
}
